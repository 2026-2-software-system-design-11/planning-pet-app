package planningpet.schedule.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

/** 06 데이터 절의 Task 규칙과 docs/porting/task.md 2절의 경계. 오늘은 11-18(수)이다. */
class TaskTest {
    private static final LocalDate TODAY = LocalDate.of(2026, 11, 18);
    private static final LocalTime TEN = LocalTime.of(10, 0);

    private static Task make(Kind kind, LocalDate date, LocalTime start, boolean nextDay, int minutes) {
        return new Task("id", 1, kind, "일", date, start, minutes, "", nextDay, Category.OTHER, null, null, null,
                false, null, 0, 0, null, null);
    }

    /** 30분짜리 할 일이나 고정 일정. due가 있으면 기간 첫날이 date인 기간 할 일이다. */
    private static Task full(Kind kind, LocalDate date, LocalTime start, String seriesId, LocalDate due,
            boolean done, LocalDate doneDay) {
        return new Task("id", 1, kind, "일", date, start, 30, "", false, Category.OTHER, seriesId, due,
                due == null ? null : date, done, doneDay, 0, 0, null, null);
    }

    private static Task period(LocalDate date, LocalTime start, LocalDate due) {
        return full(Kind.TASK, date, start, null, due, false, null);
    }

    private static Task finished(LocalDate date, LocalTime start, LocalDate due, LocalDate doneDay) {
        return full(Kind.TASK, date, start, null, due, true, doneDay);
    }

    private static LocalDate day(int d) {
        return LocalDate.of(2026, 11, d);
    }

    @Test
    void E_TASK_1_firstMinute() {
        assertEquals(0, make(Kind.TASK, day(18), LocalTime.MIDNIGHT, false, 30).startMinute());
    }

    @Test
    void E_TASK_2_nextDayMidnight() {
        assertEquals(1440, make(Kind.TASK, day(18), LocalTime.MIDNIGHT, true, 30).startMinute());
    }

    @Test
    void E_TASK_3_latestStart() {
        assertEquals(1675, make(Kind.TASK, day(18), LocalTime.of(3, 55), true, 10).startMinute());
    }

    @Test
    void E_TASK_4_noStart() {
        Task t = make(Kind.TASK, day(18), null, false, 30);
        assertFalse(t.timed());
        assertThrows(NullPointerException.class, t::startMinute);
    }

    @Test
    void E_TASK_5_latestEnd() {
        assertEquals(1680, make(Kind.TASK, day(18), LocalTime.of(16, 0), false, 720).endMinute());
    }

    @Test
    void E_TASK_6_plainDoneOtherDay() {
        assertEquals(day(16), finished(day(16), null, null, day(17)).countDay());
    }

    @Test
    void E_TASK_7_periodNotDone() {
        assertEquals(day(20), period(day(16), null, day(20)).countDay());
    }

    @Test
    void E_TASK_8_periodDone() {
        assertEquals(day(17), finished(day(16), null, day(20), day(17)).countDay());
    }

    @Test
    void E_TASK_9_periodDoneWithoutDoneDay() {
        assertEquals(day(16), finished(day(16), null, day(20), null).countDay());
    }

    @Test
    void E_TASK_10_plannedYesterday() {
        assertTrue(period(day(17), TEN, day(20)).missed(TODAY));
    }

    @Test
    void E_TASK_11_plannedToday() {
        assertFalse(period(day(18), TEN, day(20)).missed(TODAY));
    }

    @Test
    void E_TASK_12_dueToday() {
        assertTrue(period(day(17), TEN, day(18)).missed(TODAY));
    }

    @Test
    void E_TASK_13_dueYesterday() {
        Task t = period(day(16), TEN, day(17));
        assertFalse(t.missed(TODAY));
        assertTrue(t.pastDue(TODAY));
    }

    @Test
    void E_TASK_14_noTimeSet() {
        assertFalse(period(day(16), null, day(20)).missed(TODAY));
    }

    @Test
    void E_TASK_15_doneIsNotMissed() {
        assertFalse(finished(day(17), TEN, day(20), day(17)).missed(TODAY));
    }

    @Test
    void E_TASK_16_doneIsNotPastDue() {
        assertFalse(finished(day(16), null, day(17), day(16)).pastDue(TODAY));
    }
}
