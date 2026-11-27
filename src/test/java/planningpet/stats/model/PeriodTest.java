package planningpet.stats.model;

import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.SUNDAY;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

/** 06 29번 입력의 기간과 docs/porting/stats.md 2절의 E-STAT 경계. */
class PeriodTest {
    private static LocalDate d(int y, int m, int day) { return LocalDate.of(y, m, day); }
    private static LocalDate d(int day) { return d(2026, 11, day); }

    @Test
    void E_STAT_2_firstAndLastDayOfWeek() {
        Period monday = new Period(d(16), d(22), false);
        assertEquals(monday, Period.week(d(16), MONDAY));
        assertEquals(monday, Period.week(d(22), MONDAY));
        Period sunday = new Period(d(15), d(21), false);
        assertEquals(sunday, Period.week(d(15), SUNDAY));
        assertEquals(sunday, Period.week(d(21), SUNDAY));
    }

    @Test
    void E_STAT_3_stepAcrossMonthAndYear() {
        assertEquals(new Period(d(2027, 1, 4), d(2027, 1, 10), false), Period.week(d(2026, 12, 28), MONDAY).step(1));
        assertEquals(new Period(d(2027, 1, 1), d(2027, 1, 31), true), Period.month(d(2026, 12, 5)).step(1));
        assertEquals(new Period(d(2026, 12, 1), d(2026, 12, 31), true), Period.month(d(2027, 1, 9)).step(-1));
        assertEquals(new Period(d(2028, 2, 1), d(2028, 2, 29), true), Period.month(d(2028, 2, 10)));
        assertEquals(new Period(d(2027, 2, 1), d(2027, 2, 28), true), Period.month(d(30)).step(3));
    }
}
