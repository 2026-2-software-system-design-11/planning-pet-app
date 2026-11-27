package planningpet.schedule.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import planningpet.common.model.AppDay;

/** 06 18번과 docs/porting/auto-place.md 2절의 E-AUTO 경계. */
class AutoPlacerTest {
    private final List<Task> tasks = new ArrayList<>();

    private static LocalDate day(int d) {
        return LocalDate.of(2026, 11, d);
    }

    /** start가 null이면 시각 없음, "+"로 시작하면 다음 날 새벽 시각이다. due가 있으면 첫날 from의 기간 할 일이다. */
    private Task add(Kind kind, String title, LocalDate date, String start, int minutes, LocalDate from, LocalDate due,
            boolean done) {
        boolean next = start != null && start.startsWith("+");
        Task t = new Task("id" + tasks.size(), tasks.size() + 1, kind, title, date,
                start == null ? null : LocalTime.parse(next ? start.substring(1) : start), minutes, "", next,
                Category.OTHER, null, null, due, from, done, done ? date : null, 0, 0, null, null);
        tasks.add(t);
        return t;
    }

    private Task task(String title, int date, int minutes) {
        return add(Kind.TASK, title, day(date), null, minutes, null, null, false);
    }

    private Task fixed(String title, int date, String start, int minutes) {
        return add(Kind.FIXED, title, day(date), start, minutes, null, null, false);
    }

    private Task period(String title, int from, int due, int minutes) {
        return add(Kind.TASK, title, day(from), null, minutes, day(from), day(due), false);
    }

    private Placement run(LocalDate weekStart, LocalDateTime now) {
        TaskBook book = new TaskBook(tasks, () -> tasks.size() + 1L, () -> AppDay.of(now));
        return AutoPlacer.place(book, weekStart, now);
    }

    /** 보이는 주 11월 weekStart일, 지금 11월 date일 time. */
    private Placement run(int weekStart, int date, String time) {
        return run(day(weekStart), day(date).atTime(LocalTime.parse(time)));
    }

    /** "A 11-16 15:25, B 11-17 09:00 / 못 놓음 E / 대상 3"처럼 놓은 일, 놓지 못한 일, 대상 수. */
    private static String text(Placement p) {
        String placed = p.placed().stream()
                .map(x -> x.task().getTitle() + " " + x.day().getMonthValue() + "-" + x.day().getDayOfMonth() + " "
                        + x.start())
                .collect(Collectors.joining(", "));
        String unplaced = p.unplaced().stream().map(Task::getTitle).collect(Collectors.joining(", "));
        return placed + " / 못 놓음 " + unplaced + " / 대상 " + p.count();
    }

    @Test
    void T_AUTO_1_fromNow() {
        task("A", 16, 60);
        assertEquals("A 11-16 15:25 / 못 놓음  / 대상 1", text(run(16, 16, "15:23")));
    }

    @Test
    void T_AUTO_2_beforeNine() {
        task("A", 16, 60);
        assertEquals("A 11-16 09:00 / 못 놓음  / 대상 1", text(run(16, 16, "08:00")));
    }

    @Test
    void T_AUTO_3_busyAndLongerFirst() {
        fixed("고정", 16, "16:00", 90);
        add(Kind.TASK, "끝냄", day(16), "18:00", 60, null, null, true);
        task("A", 16, 60);
        task("B", 16, 120);
        assertEquals("B 11-16 19:00, A 11-16 21:00 / 못 놓음  / 대상 2", text(run(16, 16, "15:23")));
    }

    @Test
    void T_AUTO_4_laterDay() {
        task("C", 17, 30);
        assertEquals("C 11-17 09:00 / 못 놓음  / 대상 1", text(run(16, 16, "15:23")));
    }

    @Test
    void T_AUTO_5_pastDay() {
        task("D", 17, 30);
        assertEquals(" / 못 놓음  / 대상 0", text(run(16, 18, "10:00")));
    }
}
