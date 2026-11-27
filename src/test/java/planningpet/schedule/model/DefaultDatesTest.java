package planningpet.schedule.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

/** 06 14번과 docs/porting/screen-calc.md 2절의 E-DEF 경계. */
class DefaultDatesTest {
    private static LocalDate d(String date) {
        return LocalDate.parse(date.length() == 5 ? "2026-" + date : date);
    }

    private static void schedule(String shown, String today, String expected) {
        assertEquals(d(expected), DefaultDates.forAdd(Screen.SCHEDULE, null, d(shown), d(today)));
    }

    @Test
    void T_DEF_1_main() {
        assertEquals(d("11-20"), DefaultDates.forAdd(Screen.MAIN, d("11-20"), null, d("11-16")));
    }

    @Test
    void T_DEF_2_todayInShownWeek() {
        schedule("11-16", "11-18", "11-18");
    }

    @Test
    void T_DEF_3_nextWeek() {
        schedule("11-23", "11-18", "11-23");
    }

    @Test
    void T_DEF_4_pastWeek() {
        schedule("11-09", "11-18", "11-09");
    }

    @Test
    void E_DEF_1_todayFirstAndLastDay() {
        schedule("11-16", "11-16", "11-16");
        schedule("11-16", "11-22", "11-22");
    }

    @Test
    void E_DEF_2_todayJustOutside() {
        schedule("11-16", "11-15", "11-16");
        schedule("11-16", "11-23", "11-16");
    }

    @Test
    void E_DEF_3_weekAcrossYear() {
        schedule("12-28", "2027-01-01", "2027-01-01");
    }

    @Test
    void E_DEF_4_mainPastDay() {
        assertEquals(d("11-10"), DefaultDates.forAdd(Screen.MAIN, d("11-10"), null, d("11-18")));
    }
}
