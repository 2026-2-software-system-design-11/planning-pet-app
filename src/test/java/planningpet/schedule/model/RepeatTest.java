package planningpet.schedule.model;

import static java.time.DayOfWeek.FRIDAY;
import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.THURSDAY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
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

    @Test
    void E_REP_6_oneWeek() {
        assertEquals(7, Repeat.DAILY.dates(day(11, 16), day(11, 22), 120).size());
    }

    @Test
    void E_REP_7_endIsStart() {
        assertEquals(List.of(day(11, 16)), Repeat.weekly(MONDAY).dates(day(11, 16), day(11, 16), 120));
    }

    @Test
    void E_REP_8_endBeforeStart() {
        assertEquals(List.of(), Repeat.DAILY.dates(day(11, 16), day(11, 15), 120));
    }

    @Test
    void E_REP_9_oneOverLimit() {
        List<LocalDate> d = Repeat.DAILY.dates(day(11, 16), LocalDate.of(2027, 3, 16), 120);
        assertEquals(121, d.size());
        assertEquals(LocalDate.of(2027, 3, 16), d.get(120));
    }

    @Test
    void E_REP_10_farOverLimit() {
        List<LocalDate> d = Repeat.DAILY.dates(day(11, 16), LocalDate.of(2027, 12, 31), 120);
        assertEquals(121, d.size());
        assertEquals(LocalDate.of(2027, 3, 16), d.get(120));
    }

    @Test
    void E_REP_11_newYear() {
        assertEquals(List.of(day(12, 25), LocalDate.of(2027, 1, 1), LocalDate.of(2027, 1, 8)),
                Repeat.weekly(FRIDAY).dates(day(12, 25), LocalDate.of(2027, 1, 8), 120));
    }

    @Test
    void E_REP_12_noDaysNoDates() {
        assertEquals(List.of(), Repeat.weekly().dates(day(11, 16), day(11, 30), 120));
    }

    @Test
    void E_REP_13_leapYear() {
        assertEquals(List.of(LocalDate.of(2028, 2, 28), LocalDate.of(2028, 2, 29), LocalDate.of(2028, 3, 1)),
                Repeat.DAILY.dates(LocalDate.of(2028, 2, 28), LocalDate.of(2028, 3, 1), 120));
    }
}
