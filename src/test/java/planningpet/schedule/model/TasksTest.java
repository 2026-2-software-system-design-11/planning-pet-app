package planningpet.schedule.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static planningpet.schedule.model.Tasks.d;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

/** 공용 일정 만들기 Tasks(#518)의 처음 값과 고친 값. */
class TasksTest {
    @Test
    void days() {
        assertEquals(LocalDate.of(2026, 11, 18), d(18));
        assertEquals(LocalDate.of(2026, 12, 1), d(31));
        assertEquals(LocalDate.of(2026, 1, 2), d(1, 2));
    }
}
