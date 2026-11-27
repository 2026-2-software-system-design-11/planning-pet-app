package planningpet.schedule.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;

/** 06 19번과 docs/porting/screen-calc.md 2절의 E-TT 경계. */
class TimetableLayoutTest {
    private static final LocalDate DAY = LocalDate.of(2026, 11, 16);

    /** 11-16의 할 일. start는 "09:00", nextDay면 다음 날 시각이다. */
    private static Task task(String title, long seq, String start, int minutes, boolean nextDay) {
        return new Task(title, seq, Kind.TASK, title, DAY, LocalTime.parse(start), minutes, "", nextDay,
                Category.OTHER, null, null, null, null, false, null, 0, 0, null, null);
    }

    private static Task task(String title, long seq, String start, int minutes) {
        return task(title, seq, start, minutes, false);
    }

    /** "A 0/2, B 1/2"처럼 돌려준 순서대로 적는다. */
    private static String layout(Task... tasks) {
        return String.join(", ", TimetableLayout.of(List.of(tasks)).stream()
                .map(s -> s.task().getTitle() + " " + s.column() + "/" + s.columns()).toList());
    }

    @Test
    void T_TT_1_single() {
        assertEquals("A 0/1", layout(task("A", 1, "09:00", 60)));
    }

    @Test
    void T_TT_2_overlap() {
        assertEquals("A 0/2, B 1/2", layout(task("A", 1, "09:00", 60), task("B", 2, "09:30", 60)));
    }
}
