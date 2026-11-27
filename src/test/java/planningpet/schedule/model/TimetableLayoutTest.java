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

    @Test
    void T_TT_3_touching() {
        assertEquals("A 0/1, B 0/1", layout(task("A", 1, "09:00", 60), task("B", 2, "10:00", 60)));
    }

    @Test
    void T_TT_4_freedColumn() {
        assertEquals("A 0/2, B 1/2, C 1/2",
                layout(task("A", 1, "09:00", 120), task("B", 2, "09:30", 30), task("C", 3, "10:00", 30)));
    }

    @Test
    void T_TT_5_longerFirst() {
        assertEquals("B 0/2, A 1/2", layout(task("A", 1, "09:00", 60), task("B", 2, "09:00", 90)));
    }

    @Test
    void T_TT_6_pastMidnight() {
        assertEquals("A 0/2, B 1/2", layout(task("A", 1, "23:30", 90), task("B", 2, "00:30", 30, true)));
    }

    @Test
    void E_TT_1_empty() {
        assertEquals("", layout());
    }

    @Test
    void E_TT_2_threeAtOnce() {
        assertEquals("A 0/3, B 1/3, C 2/3",
                layout(task("A", 1, "09:00", 60), task("B", 2, "09:00", 60), task("C", 3, "09:00", 60)));
    }

    @Test
    void E_TT_3_chainedGroup() {
        assertEquals("A 0/2, B 1/2, C 0/2",
                layout(task("A", 1, "09:00", 60), task("B", 2, "09:30", 60), task("C", 3, "10:00", 60)));
    }

    @Test
    void E_TT_4_afterGroupEnds() {
        assertEquals("A 0/2, B 1/2, C 0/1",
                layout(task("A", 1, "09:00", 60), task("B", 2, "09:30", 60), task("C", 3, "10:30", 30)));
    }

    @Test
    void E_TT_5_givenOrderIgnored() {
        assertEquals("A 0/2, B 1/2, C 1/2",
                layout(task("C", 3, "10:00", 30), task("B", 2, "09:30", 30), task("A", 1, "09:00", 120)));
    }

    @Test
    void E_TT_6_sameStartAndLength() {
        assertEquals("B 0/2, A 1/2", layout(task("A", 2, "09:00", 60), task("B", 1, "09:00", 60)));
    }

    @Test
    void E_TT_7_dayEdges() {
        assertEquals("A 0/1, B 0/1", layout(task("A", 1, "00:00", 60), task("B", 2, "03:00", 60, true)));
        assertEquals("A 0/2, B 1/2", layout(task("A", 1, "23:00", 300), task("B", 2, "03:30", 30, true)));
    }

    @Test
    void E_TT_8_longBesideThree() {
        assertEquals("A 0/2, B 1/2, C 1/2, D 1/2", layout(task("A", 1, "09:00", 180), task("B", 2, "09:00", 60),
                task("C", 3, "10:00", 60), task("D", 4, "11:00", 60)));
    }
}
