package planningpet.reward.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import planningpet.app.model.AppData;
import planningpet.pet.model.PetKind;
import planningpet.pet.model.PetState;
import planningpet.schedule.model.Category;
import planningpet.schedule.model.Kind;
import planningpet.schedule.model.Task;

/** 06 8번과 docs/porting/rewards.md 2절의 E-RWD 경계. 펫은 고른 고양이 하나이고 complete와 done은 값을 한 줄로 잇는다. */
class RewardsTest {
    private final Task a = task("A", d(16));
    private final AppData data = new AppData(Clock.systemDefaultZone(), List.of(a), List.of());

    RewardsTest() {
        data.addPet(new PetState(PetKind.CAT, "나비"));
        data.setActiveKind(PetKind.CAT);
    }

    private static LocalDate d(int day) { return LocalDate.of(2026, 11, day); }

    private static Task task(String title, LocalDate date) {
        return make(Kind.TASK, title, date, null, null);
    }

    /** 60분 일정. due가 있으면 11-16부터 due까지인 기간 할 일이다. */
    private static Task make(Kind kind, String title, LocalDate date, LocalTime start, LocalDate due) {
        return new Task(title, 1, kind, title, date, start, 60, "", false, Category.OTHER, null, null, due,
                due == null ? null : d(16), false, null, 0, 0, null, null);
    }

    private String complete(Task t, LocalDate today) {
        RewardResult r = new Rewards(data).complete(t, today);
        return r.outcome() + " " + r.points() + " " + r.exp() + ", " + data.getPoints() + "P "
                + data.pet(PetKind.CAT).getExp() + " " + data.getRewardCountDay() + " " + data.getRewardCount();
    }

    private static String done(Task t) {
        return t.isDone() + " " + t.getDoneDay() + " " + t.getRewardPoints() + " " + t.getRewardExp() + " "
                + t.getRewardDay() + " " + t.getRewardPet();
    }

    @Test
    void T_RWD_1_today() {
        assertEquals("REWARDED 10 10, 10P 10 2026-11-16 1", complete(a, d(16)));
        assertEquals("true 2026-11-16 10 10 2026-11-16 CAT", done(a));
    }

    @Test
    void T_RWD_2_pastDay() {
        Task b = task("B", d(14));
        assertEquals("REWARDED 10 10, 10P 10 2026-11-16 1", complete(b, d(16)));
        assertEquals("true 2026-11-16 10 10 2026-11-16 CAT 2026-11-14", done(b) + " " + b.getDate());
    }

    @Test
    void T_RWD_3_laterDay() {
        Task b = task("B", d(30));
        assertEquals("REWARDED 10 10, 10P 10 2026-11-16 1", complete(b, d(16)));
        assertEquals("true 2026-11-16 10 10 2026-11-16 CAT 2026-11-30", done(b) + " " + b.getDate());
    }

    @Test
    void T_RWD_4_lastRewardOfDay() {
        data.setRewardCount(d(16), 19);
        assertEquals("REWARDED 10 10, 10P 10 2026-11-16 20", complete(a, d(16)));
    }

    @Test
    void T_RWD_5_capped() {
        data.setRewardCount(d(16), 20);
        data.setPoints(50);
        assertEquals("CAPPED 0 0, 50P 0 2026-11-16 20", complete(a, d(16)));
        assertEquals("true 2026-11-16 0 0 null null", done(a));
    }

    @Test
    void T_RWD_6_capResetsOnNewDay() {
        data.setRewardCount(d(15), 20);
        assertEquals("REWARDED 10 10, 10P 10 2026-11-16 1", complete(a, d(16)));
    }

    @Test
    void T_RWD_7_fixed() {
        Task f = make(Kind.FIXED, "F", d(16), LocalTime.of(9, 0), null);
        assertEquals("REWARDED 10 10, 10P 10 2026-11-16 1", complete(f, d(16)));
        assertEquals("true 2026-11-16 10 10 2026-11-16 CAT", done(f));
    }
}
