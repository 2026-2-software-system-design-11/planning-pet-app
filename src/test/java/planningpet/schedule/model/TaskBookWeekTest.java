package planningpet.schedule.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/** 06 7번과 docs/porting/task-lists.md 2절 주의 목록의 경계. 따로 정하지 않으면 오늘은 11-18(수), 주는 11-16(월)부터다. */
class TaskBookWeekTest {
    private static final LocalDate WEEK = LocalDate.of(2026, 11, 16);
    private LocalDate today = day(11, 18);
    private final List<Task> tasks = new ArrayList<>();

    private static LocalDate day(int month, int d) {
        return LocalDate.of(2026, month, d);
    }

    /**
     * 할 일 하나. due가 있으면 첫날 from인 기간 할 일이다. minute이 있으면 date의 그 분에 시작하고, doneDay가 있으면 그날 끝냈다.
     */
    private Task put(String title, LocalDate date, Integer minute, LocalDate from, LocalDate due, LocalDate doneDay) {
        LocalTime start = minute == null ? null : LocalTime.of(minute / 60, minute % 60);
        Task t = new Task("id" + tasks.size(), tasks.size() + 1, Kind.TASK, title, date, start, 30, "", false,
                Category.OTHER, null, null, due, from, doneDay != null, doneDay, 0, 0, null, null);
        tasks.add(t);
        return t;
    }

    /** weekStart부터의 주의 줄을 "11/18 P"처럼 줄 순서대로. */
    private List<String> rows(LocalDate weekStart) {
        return new TaskBook(tasks, () -> 0, () -> today).untimedOfWeek(weekStart).stream()
                .map(r -> r.day().getMonthValue() + "/" + r.day().getDayOfMonth() + " " + r.task().getTitle()).toList();
    }

    @Test
    void T_WEEK_1_untimedInWeek() {
        put("a", day(11, 15), null, null, null, null);
        put("b", day(11, 16), null, null, null, day(11, 16));
        put("c", day(11, 22), null, null, null, null);
        put("d", day(11, 23), null, null, null, null);
        put("e", day(11, 18), 10 * 60, null, null, null);
        assertEquals(List.of("11/16 b", "11/22 c"), rows(WEEK));
    }
}
