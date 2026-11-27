package planningpet.common.model;

import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.SUNDAY;
import static java.time.DayOfWeek.WEDNESDAY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.DayOfWeek;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

/** 06 데이터 절의 한 주와 docs/porting/day-rules.md 2절의 경계. */
class RulesTest {
    private static void week(String date, DayOfWeek start, String expected) {
        assertEquals(LocalDate.parse(expected), Rules.weekStart(LocalDate.parse(date), start));
    }

    @Test
    void E_WEEK_1_mondayFirstDay() {
        week("2026-11-16", MONDAY, "2026-11-16");
    }

    @Test
    void E_WEEK_2_mondayLastDay() {
        week("2026-11-22", MONDAY, "2026-11-16");
    }

    @Test
    void E_WEEK_3_sundayFirstDay() {
        week("2026-11-22", SUNDAY, "2026-11-22");
    }

    @Test
    void E_WEEK_4_sundayLastDay() {
        week("2026-11-21", SUNDAY, "2026-11-15");
    }

    @Test
    void E_WEEK_5_sundayStartOnMonday() {
        week("2026-11-16", SUNDAY, "2026-11-15");
    }

    @Test
    void E_WEEK_6_newYear() {
        week("2027-01-01", MONDAY, "2026-12-28");
    }

    @Test
    void E_WEEK_7_newMonth() {
        week("2026-12-01", SUNDAY, "2026-11-29");
    }

    @Test
    void E_WEEK_8_otherStartDay() {
        week("2026-11-16", WEDNESDAY, "2026-11-11");
    }

    @Test
    void E_WEEK_9_nullDate() {
        assertThrows(NullPointerException.class, () -> Rules.weekStart(null, MONDAY));
    }

    @Test
    void E_WEEK_10_nullStartDay() {
        assertThrows(NullPointerException.class, () -> Rules.weekStart(LocalDate.parse("2026-11-16"), null));
    }
}
