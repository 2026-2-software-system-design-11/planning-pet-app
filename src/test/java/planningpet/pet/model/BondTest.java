package planningpet.pet.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import planningpet.schedule.model.Category;
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
}
