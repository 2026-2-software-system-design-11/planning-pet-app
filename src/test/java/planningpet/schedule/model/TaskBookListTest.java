package planningpet.schedule.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/** 06 6번과 docs/porting/task-lists.md 2절의 경계. 따로 정하지 않으면 오늘은 11-18(수)이다. */
class TaskBookListTest {
    private LocalDate today = day(11, 18);
    private final List<Task> tasks = new ArrayList<>();

    private static LocalDate day(int month, int d) {
        return LocalDate.of(2026, month, d);
    }

    /** minute은 앱 하루의 시작 분이고 null이면 시각 없음이다. due가 있으면 첫날 from인 기간 할 일, doneDay가 있으면 그날 끝냈다. */
    private Task put(Kind kind, String title, LocalDate date, Integer minute, int minutes, LocalDate from, LocalDate due,
            LocalDate doneDay) {
        LocalTime start = minute == null ? null : LocalTime.of(minute % Task.DAY / 60, minute % 60);
        Task t = new Task("id" + tasks.size(), tasks.size() + 1, kind, title, date, start, minutes, "",
                minute != null && minute >= Task.DAY, Category.OTHER, null, null, due, from, doneDay != null, doneDay, 0, 0,
                null, null);
        tasks.add(t);
        return t;
    }

    private Task put(Kind kind, String title, LocalDate date, Integer minute) {
        return put(kind, title, date, minute, 30, null, null, null);
    }

    /** date의 그날 목록을 마감, 시각, 시각 없음, 기간 묶음 순의 제목 목록으로. */
    private List<List<String>> groups(LocalDate date) {
        DayPlan p = new TaskBook(tasks, () -> 0, () -> today).dayPlan(date);
        return List.of(p.dueToday(), p.timed(), p.untimed(), p.period()).stream()
                .map(l -> l.stream().map(Task::getTitle).toList()).toList();
    }

    private static List<List<String>> g(List<String> due, List<String> timed, List<String> untimed, List<String> period) {
        return List.of(due, timed, untimed, period);
    }

    @Test
    void T_LIST_1_order() {
        put(Kind.TASK, "B", day(11, 16), null);
        put(Kind.TASK, "D", day(11, 16), 15 * 60);
        put(Kind.TASK, "A", day(11, 16), 15 * 60);
        put(Kind.TASK, "C", day(11, 16), 9 * 60);
        put(Kind.TASK, "F", day(11, 16), null, 30, null, null, day(11, 16));
        put(Kind.FIXED, "E", day(11, 16), 10 * 60 + 30);
        assertEquals(g(List.of(), List.of("C", "E", "D", "A"), List.of("B", "F"), List.of()), groups(day(11, 16)));
    }
}
