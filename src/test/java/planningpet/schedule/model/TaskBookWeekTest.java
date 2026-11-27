package planningpet.schedule.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    private Task put(String title, LocalDate date) {
        return put(title, date, null, null, null, null);
    }

    /** 시각을 정하지 않은 기간 할 일. */
    private Task period(String title, LocalDate from, LocalDate due) {
        return put(title, from, null, from, due, null);
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

    @Test
    void T_WEEK_2_periodOneRow() {
        period("P", day(11, 12), day(11, 20));
        period("Q", day(11, 10), day(11, 17));
        assertEquals(List.of("11/17 Q", "11/18 P", "11/20 P"), rows(WEEK));
        today = day(11, 20);
        assertEquals(List.of("11/17 Q", "11/20 P"), rows(WEEK));
    }

    @Test
    void T_WEEK_3_firstAndDueDay() {
        today = day(11, 17);
        period("P", day(11, 16), day(11, 20));
        period("Q", day(11, 16), day(11, 29));
        put("R", day(11, 16), null, day(11, 16), day(11, 19), today);
        assertEquals(List.of("11/17 P", "11/17 Q", "11/17 R", "11/20 P"), rows(WEEK));
    }

    @Test
    void T_WEEK_4_pastRepeatCopies() {
        for (int d = 16; d <= 22; d++) {
            tasks.add(new Task("r" + d, tasks.size() + 1, Kind.TASK, "운동", day(11, d), null, 30, "", false, Category.OTHER,
                    "s", Repeat.DAILY, null, null, false, null, 0, 0, null, null));
        }
        put("과제", day(11, 16));
        assertEquals(List.of("11/16 운동", "11/16 과제", "11/17 운동", "11/18 운동", "11/19 운동", "11/20 운동", "11/21 운동",
                "11/22 운동"), rows(WEEK));
        assertEquals(List.of(16, 17), tasks.stream().filter(t -> t.pastCopy(today)).map(t -> t.getDate().getDayOfMonth())
                .toList());
    }

    @Test
    void T_WEEK_5_doneTask() {
        put("과제", day(11, 16), null, null, null, day(11, 16));
        put("빨래", day(11, 16));
        assertEquals(List.of("11/16 과제", "11/16 빨래"), rows(WEEK));
    }

    @Test
    void T_EDIT_46_periodOnTodayRow() {
        period("P", day(11, 12), day(11, 27));
        period("S", day(11, 20), day(11, 22));
        put("빨래", today);
        assertEquals(List.of("11/18 P", "11/18 빨래", "11/20 S", "11/22 S"), rows(WEEK));
        assertEquals(List.of("11/15 P"), rows(day(11, 9)));
        assertEquals(List.of("11/23 P", "11/27 P"), rows(day(11, 23)));
        today = day(11, 19);
        assertEquals(List.of("11/18 빨래", "11/19 P", "11/20 S", "11/22 S"), rows(WEEK));
    }

    @Test
    void T_EDIT_47_pastDueNotMissed() {
        Task m = put("M", day(11, 17), 10 * 60, day(11, 16), day(11, 20), null);
        today = day(11, 20);
        assertTrue(m.missed(today));
        assertEquals(List.of("11/20 M"), rows(WEEK));
        today = day(11, 22);
        assertEquals(List.of(false, false), List.of(m.missed(today), m.onTimetable(today)));
        assertEquals(List.of("11/20 M"), rows(WEEK));
    }

    @Test
    void E_WEEK_1_sundayWeek() {
        put("14", day(11, 14));
        put("15", day(11, 15));
        put("21", day(11, 21));
        put("22", day(11, 22));
        assertEquals(List.of("11/15 15", "11/21 21"), rows(day(11, 15)));
    }

    @Test
    void E_WEEK_2_periodStartsLater() {
        period("P", day(11, 20), day(11, 25));
        assertEquals(List.of("11/20 P"), rows(WEEK));
        assertEquals(List.of("11/23 P", "11/25 P"), rows(day(11, 23)));
    }

    @Test
    void E_WEEK_3_dueOnWeekEdge() {
        period("P", day(11, 16), day(11, 22));
        period("Q", day(11, 16), day(11, 23));
        assertEquals(List.of("11/18 P", "11/18 Q", "11/22 P"), rows(WEEK));
    }

    @Test
    void E_WEEK_4_plannedLaterThenDone() {
        put("P", day(11, 19), 10 * 60, day(11, 16), day(11, 20), today);
        assertEquals(List.of("11/18 P"), rows(WEEK));
    }

    @Test
    void E_WEEK_5_missedPlan() {
        put("M", day(11, 17), 10 * 60, day(11, 16), day(11, 22), null);
        assertEquals(List.of("11/18 M", "11/22 M"), rows(WEEK));
    }

    @Test
    void E_WEEK_6_onTimetable() {
        put("시각", today, 10 * 60, null, null, null);
        put("정한 계획", day(11, 19), 10 * 60, day(11, 16), day(11, 22), null);
        assertEquals(List.of(), rows(WEEK));
    }

    @Test
    void E_WEEK_7_emptyWeek() {
        assertEquals(List.of(), rows(WEEK));
    }

    @Test
    void E_WEEK_8_newYearWeek() {
        period("P", day(12, 30), LocalDate.of(2027, 1, 2));
        put("새해", LocalDate.of(2027, 1, 3));
        put("다음", LocalDate.of(2027, 1, 4));
        assertEquals(List.of("12/30 P", "1/2 P", "1/3 새해"), rows(day(12, 28)));
    }
}
