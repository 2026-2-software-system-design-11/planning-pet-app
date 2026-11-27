package planningpet.schedule.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    /** 첫날 from, 마감 due인 기간 할 일 "P"를 date의 minute에 정해 둔 것. minute이 null이면 시각을 정하지 않았다. */
    private Task plan(LocalDate from, LocalDate due, LocalDate date, Integer minute) {
        return put(Kind.TASK, "P", date, minute, 30, from, due, null);
    }

    /** 11월 from일부터 to일까지 날마다 일정이 든 묶음(마감, 시각, 없음, 기간). 아무 묶음에도 없으면 "-". */
    private String spots(int from, int to) {
        List<String> names = List.of("마감", "시각", "없음", "기간");
        StringBuilder b = new StringBuilder();
        for (int d = from; d <= to; d++) {
            List<List<String>> gs = groups(day(11, d));
            int i = 0;
            while (i < 4 && gs.get(i).isEmpty()) {
                i++;
            }
            b.append(b.isEmpty() ? "" : " ").append(d).append(i < 4 ? names.get(i) : "-");
        }
        return b.toString();
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

    @Test
    void T_LIST_3_fixedOnly() {
        put(Kind.FIXED, "수업", day(11, 16), 9 * 60);
        put(Kind.FIXED, "알바", day(11, 16), 18 * 60);
        assertEquals(g(List.of(), List.of("수업", "알바"), List.of(), List.of()), groups(day(11, 16)));
    }

    @Test
    void E_LIST_1_emptyDay() {
        put(Kind.TASK, "P", day(11, 16), null, 30, day(11, 16), day(11, 22), null);
        TaskBook book = new TaskBook(tasks, () -> 0, () -> today);
        assertTrue(book.dayPlan(day(11, 10)).isEmpty());
        assertEquals(today, book.dayPlan(today).date());
        assertFalse(book.dayPlan(today).isEmpty());
    }

    @Test
    void T_LIST_5_missedPlan() {
        plan(day(11, 16), day(11, 22), day(11, 17), 9 * 60);
        assertEquals("17- 18기간 19기간 20기간 21기간 22마감", spots(17, 22));
        today = day(11, 23);
        assertEquals("17- 18- 19- 20- 21- 22마감", spots(17, 22));
        today = day(11, 18);
        tasks.clear();
        plan(day(11, 16), day(11, 22), today, 14 * 60);
        assertEquals("17- 18시각 19- 20- 21- 22-", spots(17, 22));
    }

    @Test
    void T_LIST_6_plannedTodayCounts() {
        put(Kind.TASK, "과제", today, null, 30, null, null, today);
        put(Kind.TASK, "빨래", today, null);
        plan(day(11, 16), day(11, 22), day(11, 16), null);
        assertEquals(g(List.of(), List.of(), List.of("과제", "빨래"), List.of("P")), groups(today));
        tasks.remove(2);
        plan(day(11, 16), day(11, 22), today, 14 * 60);
        assertEquals(g(List.of(), List.of("P"), List.of("과제", "빨래"), List.of()), groups(today));
    }

    @Test
    void T_LIST_7_afterMidnightLast() {
        today = day(11, 20);
        put(Kind.TASK, "A", today, 23 * 60 + 40);
        put(Kind.TASK, "B", today, 8 * 60, 60, null, null, null);
        put(Kind.TASK, "C", today, null);
        put(Kind.TASK, "복습", today, Task.DAY + 90);
        assertEquals(g(List.of(), List.of("B", "A", "복습"), List.of("C"), List.of()), groups(today));
        assertTrue(new TaskBook(tasks, () -> 0, () -> today).dayPlan(day(11, 21)).isEmpty());
    }

    @Test
    void E_LIST_2_plannedLaterThenDone() {
        put(Kind.TASK, "P", day(11, 19), 10 * 60, 30, day(11, 16), day(11, 20), today);
        assertEquals("18없음 19- 20-", spots(18, 20));
    }

    @Test
    void E_LIST_3_pastDue() {
        plan(day(11, 15), day(11, 17), day(11, 15), null);
        assertEquals("15- 16- 17마감 18-", spots(15, 18));
    }

    @Test
    void E_LIST_4_plannedOnDueDay() {
        plan(day(11, 16), day(11, 20), day(11, 20), 10 * 60);
        assertEquals("19- 20시각", spots(19, 20));
    }

    @Test
    void E_LIST_5_midnightAndNextDay() {
        put(Kind.TASK, "다음 날 00:00", today, Task.DAY);
        put(Kind.TASK, "23:30", today, 23 * 60 + 30);
        put(Kind.TASK, "00:00", today, 0);
        put(Kind.TASK, "다음 날 03:55", today, Task.DAY + 235, 5, null, null, null);
        assertEquals(List.of("00:00", "23:30", "다음 날 00:00", "다음 날 03:55"), groups(today).get(1));
    }

    @Test
    void E_LIST_6_dueBeforeDate() {
        plan(day(11, 20), day(11, 19), day(11, 20), null);
        assertEquals("19- 20없음", spots(19, 20));
    }

    @Test
    void E_LIST_7_missedPlanDueToday() {
        plan(day(11, 16), day(11, 18), day(11, 17), 9 * 60);
        assertEquals("17- 18마감", spots(17, 18));
    }

    @Test
    void E_LIST_8_pastRepeatCopy() {
        tasks.add(new Task("s", 1, Kind.TASK, "운동", day(11, 17), null, 30, "", false, Category.OTHER, "s", Repeat.DAILY,
                null, null, false, null, 0, 0, null, null));
        assertEquals(g(List.of(), List.of(), List.of("운동"), List.of()), groups(day(11, 17)));
    }
}
