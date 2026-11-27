package planningpet.schedule.model;

import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.THURSDAY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

/** 06 데이터 절 repeat과 docs/porting/repeat.md 2절의 Repeat 경계. */
class RepeatTest {
    private static LocalDate day(int month, int d) {
        return LocalDate.of(2026, month, d);
    }

    @Test
    void E_REP_1_daily() {
        assertTrue(Repeat.DAILY.has(day(11, 16)));
        assertTrue(Repeat.DAILY.has(day(11, 22)));
    }

    @Test
    void E_REP_2_pickedDays() {
        Repeat r = Repeat.weekly(MONDAY, THURSDAY);
        assertTrue(r.has(day(11, 16)));
        assertFalse(r.has(day(11, 17)));
        assertTrue(r.has(day(11, 19)));
    }

    @Test
    void E_REP_3_noDays() {
        assertFalse(Repeat.weekly().has(day(11, 16)));
    }

    @Test
    void E_REP_4_sameDayTwice() {
        assertThrows(IllegalArgumentException.class, () -> Repeat.weekly(MONDAY, MONDAY));
    }

    @Test
    void E_REP_5_orderDoesNotMatter() {
        assertEquals(Repeat.weekly(MONDAY, THURSDAY), Repeat.weekly(THURSDAY, MONDAY));
    }
}
