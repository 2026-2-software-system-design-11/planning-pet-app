package planningpet.common.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

/** 06 1번과 docs/porting/day-rules.md 2절의 앱 날짜 경계. */
class AppDayTest {
    private static void day(String time, String expected) {
        assertEquals(LocalDate.parse(expected), AppDay.of(LocalDateTime.parse(time)));
    }

    @Test
    void T_DAY_1_beforeFour() {
        day("2026-11-16T03:59", "2026-11-15");
    }

    @Test
    void T_DAY_2_four() {
        day("2026-11-16T04:00", "2026-11-16");
    }

    @Test
    void T_DAY_3_lastMinute() {
        day("2026-11-16T23:59", "2026-11-16");
    }

    @Test
    void T_DAY_4_newYear() {
        day("2027-01-01T03:00", "2026-12-31");
    }

    @Test
    void E_DAY_1_midnight() {
        day("2026-11-16T00:00", "2026-11-15");
    }

    @Test
    void E_DAY_2_lastNanoBeforeFour() {
        day("2026-11-16T03:59:59.999999999", "2026-11-15");
    }

    @Test
    void E_DAY_3_newMonth() {
        day("2026-12-01T03:00", "2026-11-30");
    }

    @Test
    void E_DAY_4_leapYear() {
        day("2028-03-01T03:00", "2028-02-29");
    }

    @Test
    void E_DAY_5_null() {
        assertThrows(NullPointerException.class, () -> AppDay.of(null));
    }
}
