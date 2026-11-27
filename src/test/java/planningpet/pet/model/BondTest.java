package planningpet.pet.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import planningpet.app.model.AppData;
import planningpet.reward.model.Rewards;
import planningpet.schedule.model.Category;
import planningpet.schedule.model.DeleteScope;
import planningpet.schedule.model.Kind;
import planningpet.schedule.model.Task;

/** 06 27번과 docs/porting/pet.md 2절의 친밀도 경계. 일정마다 경험치 10을 받았다. */
class BondTest {
    private final List<Task> tasks = new ArrayList<>();

    private static LocalDate d(int day) { return LocalDate.of(2026, 11, 1).plusDays(day - 1); }

    /** kind 펫이 day에 경험치 10을 받고 끝낸 할 일을 넣는다. */
    private void rewarded(PetKind kind, LocalDate day) {
        tasks.add(new Task("t" + tasks.size(), tasks.size() + 1, Kind.TASK, "t", day, null, 30, "", false, Category.OTHER,
                null, null, null, null, true, day, 10, 10, day, kind));
    }

    /** 고양이가 11-01부터 n일 동안 날마다 받는다. */
    private void catDays(int n) {
        for (int i = 1; i <= n; i++) {
            rewarded(PetKind.CAT, d(i));
        }
    }

    private String cat() { return Bond.days(tasks, PetKind.CAT) + " " + Bond.percent(tasks, PetKind.CAT); }

    @Test
    void T_BOND_1_percent() {
        catDays(15);
        assertEquals(50, Bond.percent(tasks, PetKind.CAT));
        tasks.clear();
        catDays(40);
        assertEquals(100, Bond.percent(tasks, PetKind.CAT));
        assertEquals(0, Bond.percent(tasks, PetKind.DOG));
    }

    @Test
    void T_BOND_2_sameDayTwice() {
        rewarded(PetKind.CAT, d(16));
        rewarded(PetKind.CAT, d(16));
        assertEquals("1 3", cat());
    }

    @Test
    void E_BOND_1_noTasks() {
        assertEquals("0 0", cat());
    }

    @Test
    void E_BOND_2_oneDayRoundsDown() {
        catDays(1);
        assertEquals("1 3", cat());
    }

    @Test
    void E_BOND_3_twoDaysRoundUp() {
        catDays(2);
        assertEquals("2 7", cat());
    }

    @Test
    void E_BOND_4_dayBeforeFull() {
        catDays(29);
        assertEquals("29 97", cat());
    }

    @Test
    void E_BOND_5_full() {
        catDays(30);
        assertEquals("30 100", cat());
    }

    @Test
    void E_BOND_6_overFullStops() {
        catDays(31);
        assertEquals("31 100", cat());
    }

    /** 11-16의 앱 데이터에 고른 고양이와 할 일 a를 두고 Rewards로 끝낸다. */
    private AppData catApp(Task a) {
        AppData data = new AppData(Clock.fixed(Instant.parse("2026-11-16T10:00:00Z"), ZoneOffset.UTC), List.of(a), List.of());
        data.addPet(new PetState(PetKind.CAT, "나비"));
        data.setActiveKind(PetKind.CAT);
        new Rewards(data).complete(a, d(16));
        return data;
    }

    private static Task open() {
        return new Task("a", 1, Kind.TASK, "a", d(16), null, 30, "", false, Category.OTHER, null, null, null, null, false,
                null, 0, 0, null, null);
    }

    private static String cat(List<Task> history) {
        return Bond.days(history, PetKind.CAT) + " " + Bond.percent(history, PetKind.CAT);
    }

    @Test
    void T_BOND_3_deletedKeepsDay() {
        Task a = open();
        AppData data = catApp(a);
        data.getBook().delete(a, DeleteScope.ONLY_THIS);
        assertEquals(0, data.getBook().getTasks().size());
        assertEquals("1 3", cat(data.getBook().rewardHistory()));
    }

    @Test
    void E_BOND_7_cappedNotCounted() {
        Task a = open();
        AppData data = new AppData(Clock.systemUTC(), List.of(), List.of());
        data.addPet(new PetState(PetKind.CAT, "나비"));
        data.setRewardCount(d(16), 20);
        new Rewards(data).complete(a, d(16));
        assertEquals("0 0", cat(List.of(a)));
    }

    @Test
    void E_BOND_8_uncompleteDropsDay() {
        Task a = open();
        AppData data = catApp(a);
        assertEquals("1 3", cat(data.getBook().rewardHistory()));
        new Rewards(data).uncomplete(a, d(16));
        assertEquals("0 0", cat(data.getBook().rewardHistory()));
    }

    @Test
    void E_BOND_9_otherPetNotCounted() {
        rewarded(PetKind.CAT, d(16));
        rewarded(PetKind.CAT, d(17));
        rewarded(PetKind.DOG, d(16));
        assertEquals("2 7", cat());
        assertEquals(1, Bond.days(tasks, PetKind.DOG));
        assertEquals(3, Bond.percent(tasks, PetKind.DOG));
    }

    @Test
    void E_BOND_10_monthAndYearChange() {
        rewarded(PetKind.CAT, LocalDate.of(2026, 11, 30));
        rewarded(PetKind.CAT, LocalDate.of(2026, 12, 1));
        rewarded(PetKind.CAT, LocalDate.of(2027, 1, 1));
        assertEquals("3 10", cat());
    }
}
