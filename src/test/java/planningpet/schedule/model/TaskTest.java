package planningpet.schedule.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

/** 06 데이터 절의 Task 규칙과 docs/porting/task.md 2절의 경계. 오늘은 11-18(수)이다. */
class TaskTest {
    private static Task make(Kind kind, LocalDate date, LocalTime start, boolean nextDay, int minutes) {
        return new Task("id", 1, kind, "일", date, start, minutes, "", nextDay, Category.OTHER, null, null, null,
                false, null, 0, 0, null, null);
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
}
