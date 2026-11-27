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

    @Test
    void T_AUTO_6_shortBeforeMidnight() {
        task("E", 16, 30);
        task("F", 16, 20);
        assertEquals("F 11-16 23:40 / 못 놓음 E / 대상 2", text(run(16, 16, "23:40")));
    }

    @Test
    void T_AUTO_7_afterMidnight() {
        task("G", 16, 30);
        assertEquals(" / 못 놓음 G / 대상 1", text(run(16, 17, "01:00")));
    }

    @Test
    void T_AUTO_8_doneIsNotTarget() {
        add(Kind.TASK, "끝냄", day(16), null, 30, null, null, true);
        assertEquals(" / 못 놓음  / 대상 0", text(run(16, 16, "15:23")));
    }

    @Test
    void T_AUTO_9_sameLengthBySeq() {
        task("H", 16, 30);
        task("I", 16, 30);
        assertEquals("H 11-16 09:00, I 11-16 09:30 / 못 놓음  / 대상 2", text(run(16, 16, "08:00")));
    }

    @Test
    void T_AUTO_10_nextWeek() {
        task("J", 23, 30);
        assertEquals("J 11-23 09:00 / 못 놓음  / 대상 1", text(run(23, 16, "15:23")));
    }

    @Test
    void T_AUTO_11_periodEarliestDayWithRoom() {
        period("P", 14, 18, 120);
        assertEquals("P 11-17 09:00 / 못 놓음  / 대상 1", text(run(16, 16, "22:30")));
    }

    @Test
    void T_AUTO_12_periodFromFirstDay() {
        period("Q", 19, 22, 30);
        assertEquals("Q 11-19 09:00 / 못 놓음  / 대상 1", text(run(16, 16, "15:23")));
    }

    @Test
    void T_AUTO_13_onlyShownWeek() {
        period("S", 16, 25, 30);
        assertEquals("S 11-23 09:00 / 못 놓음  / 대상 1", text(run(23, 16, "15:23")));
    }

    @Test
    void T_AUTO_14_missedPlanAgain() {
        add(Kind.TASK, "P", day(17), "09:00", 30, day(16), day(22), false);
        assertEquals("P 11-18 10:00 / 못 놓음  / 대상 1", text(run(16, 18, "10:00")));
    }

    @Test
    void T_AUTO_16_weekStartSetting() {
        task("Q", 22, 30);
        assertEquals("Q 11-22 09:00 / 못 놓음  / 대상 1", text(run(16, 16, "15:23")));
        assertEquals(" / 못 놓음  / 대상 0", text(run(15, 16, "15:23")));
    }

    @Test
    void T_TT_9_placedForTimetable() {
        task("A", 18, 30);
        task("B", 20, 60);
        assertEquals("A 11-18 09:00, B 11-20 09:00 / 못 놓음  / 대상 2", text(run(16, 18, "09:00")));
    }

    @Test
    void E_AUTO_1_seconds() {
        task("A", 16, 30);
        assertEquals("A 11-16 15:25 / 못 놓음  / 대상 1", text(run(day(16), LocalDateTime.of(2026, 11, 16, 15, 20, 30))));
        assertEquals("A 11-16 15:25 / 못 놓음  / 대상 1", text(run(16, 16, "15:25")));
    }

    @Test
    void E_AUTO_2_aroundNine() {
        task("A", 16, 30);
        assertEquals("A 11-16 09:00 / 못 놓음  / 대상 1", text(run(16, 16, "08:59")));
        assertEquals("A 11-16 09:05 / 못 놓음  / 대상 1", text(run(16, 16, "09:01")));
    }

    @Test
    void E_AUTO_3_endsAtMidnight() {
        task("A", 16, 60);
        task("B", 17, 65);
        assertEquals("A 11-16 23:00, B 11-17 09:00 / 못 놓음  / 대상 2", text(run(16, 16, "23:00")));
        tasks.clear();
        task("A", 16, 65);
        assertEquals(" / 못 놓음 A / 대상 1", text(run(16, 16, "23:00")));
    }

    @Test
    void E_AUTO_4_longest() {
        task("A", 16, 720);
        assertEquals("A 11-16 12:00 / 못 놓음  / 대상 1", text(run(16, 16, "12:00")));
        assertEquals(" / 못 놓음 A / 대상 1", text(run(16, 16, "12:01")));
    }

    @Test
    void E_AUTO_5_shortestInGap() {
        fixed("수업", 16, "09:00", 60);
        fixed("실습", 16, "10:10", 50);
        task("A", 16, 10);
        task("B", 16, 15);
        assertEquals("B 11-16 11:00, A 11-16 10:00 / 못 놓음  / 대상 2", text(run(16, 16, "08:00")));
    }

    @Test
    void E_AUTO_6_weekEdges() {
        task("L", 22, 30);
        task("N", 23, 30);
        task("O", 15, 30);
        assertEquals("L 11-22 09:00 / 못 놓음  / 대상 1", text(run(16, 16, "08:00")));
    }

    @Test
    void E_AUTO_7_yearChange() {
        LocalDate first = LocalDate.of(2027, 1, 1);
        add(Kind.TASK, "Y", first, null, 30, null, null, false);
        add(Kind.TASK, "Z", LocalDate.of(2026, 12, 30), null, 30, LocalDate.of(2026, 12, 30), first.plusDays(3), false);
        assertEquals("Z 12-30 09:00, Y 1-1 09:00 / 못 놓음  / 대상 2",
                text(run(LocalDate.of(2026, 12, 28), LocalDateTime.of(2026, 12, 28, 10, 0))));
    }

    @Test
    void E_AUTO_8_dueBeforeDate() {
        add(Kind.TASK, "R", day(18), null, 30, day(16), day(17), false);
        assertEquals("R 11-18 09:00 / 못 놓음  / 대상 1", text(run(16, 16, "10:00")));
    }

    @Test
    void E_AUTO_9_pastDue() {
        period("V", 10, 15, 30);
        assertEquals(" / 못 놓음  / 대상 0", text(run(16, 16, "10:00")));
    }

    @Test
    void E_AUTO_10_donePeriodLeavesSlot() {
        add(Kind.TASK, "P", day(16), "09:00", 60, day(16), day(22), true);
        task("A", 16, 60);
        assertEquals("A 11-16 09:00 / 못 놓음  / 대상 1", text(run(16, 16, "08:00")));
    }

    @Test
    void E_AUTO_11_afterMidnightTaskOfDay() {
        add(Kind.TASK, "N", day(16), "+00:00", 60, null, null, false);
        task("A", 16, 60);
        assertEquals("A 11-16 23:00 / 못 놓음  / 대상 1", text(run(16, 16, "23:00")));
    }

    @Test
    void E_AUTO_12_dateBeforeLength() {
        task("A", 17, 120);
        period("P", 16, 17, 30);
        assertEquals("P 11-17 09:00, A 11-17 09:30 / 못 놓음  / 대상 2", text(run(16, 16, "23:40")));
    }

    @Test
    void E_AUTO_13_pastWeek() {
        task("A", 16, 30);
        assertEquals(" / 못 놓음  / 대상 0", text(run(9, 16, "08:00")));
    }

    private static final String NEXT_YES = "이번 주엔 자리가 없어요. 마감 %s까지 시간이 있으니 다음 주에서 자동 배치하면 놓을 수 있어요";
    private static final String NEXT_NO = "이번 주엔 자리가 없어요. 다음 주에도 자리가 없어요. 걸리는 시간을 나누거나 다른 일을 옮겨 보세요";

    /** 놓지 못한 일마다 "제목 날짜 글: 까닭"을 대상 순서대로 " | "로 잇는다. */
    private String reasons(LocalDate weekStart, LocalDateTime now) {
        TaskBook book = new TaskBook(tasks, () -> tasks.size() + 1L, () -> AppDay.of(now));
        return AutoPlacer.place(book, weekStart, now).unplaced().stream()
                .map(t -> t.getTitle() + " " + AutoPlacer.dateText(t) + ": " + AutoPlacer.reason(t, book, weekStart, now))
                .collect(Collectors.joining(" | "));
    }

    private String reasons(int weekStart, int date, String time) {
        return reasons(day(weekStart), day(date).atTime(LocalTime.parse(time)));
    }

    @Test
    void T_AUTO_15_threeReasons() {
        task("E", 22, 30);
        period("P", 16, 22, 30);
        period("Q", 16, 29, 30);
        assertEquals("P 마감 11/22(일): 마감까지 빈 시간이 모자라요 | Q 마감 11/29(일): " + NEXT_YES.formatted("11/29(일)")
                + " | E 날짜 11/22(일): 그날 빈 시간이 모자라요", reasons(16, 22, "23:40"));
    }

    @Test
    void T_AUTO_17_noRoomNextWeek() {
        period("Q", 16, 29, 720);
        assertEquals("Q 마감 11/29(일): " + NEXT_YES.formatted("11/29(일)"), reasons(16, 22, "23:40"));
        for (int d = 23; d <= 29; d++) {
            fixed("알바", d, "09:00", 720);
        }
        assertEquals("Q 마감 11/29(일): " + NEXT_NO, reasons(16, 22, "23:40"));
    }

    @Test
    void E_AUTO_14_dueOnWeekEdges() {
        period("Q", 16, 23, 30);
        period("U", 16, 22, 30);
        assertEquals("Q 마감 11/23(월): " + NEXT_YES.formatted("11/23(월)") + " | U 마감 11/22(일): 마감까지 빈 시간이 모자라요",
                reasons(16, 22, "23:40"));
    }

    @Test
    void E_AUTO_15_nextWeekOnlyToDue() {
        fixed("알바", 23, "09:00", 720);
        period("Q", 16, 23, 720);
        assertEquals("Q 마감 11/23(월): " + NEXT_NO, reasons(16, 22, "23:40"));
    }

    @Test
    void E_AUTO_16_missedPlan() {
        add(Kind.TASK, "P", day(17), "09:00", 30, day(16), day(22), false);
        assertEquals("P 마감 11/22(일): 마감까지 빈 시간이 모자라요", reasons(16, 22, "23:40"));
    }

    @Test
    void E_AUTO_17_monthChange() {
        add(Kind.TASK, "M", day(23), null, 30, day(23), LocalDate.of(2026, 12, 1), false);
        task("K", 29, 30);
        assertEquals("M 마감 12/1(화): " + NEXT_YES.formatted("12/1(화)") + " | K 날짜 11/29(일): 그날 빈 시간이 모자라요",
                reasons(23, 29, "23:40"));
    }

    @Test
    void E_AUTO_18_lastDayAfterMidnight() {
        period("Q", 16, 29, 30);
        assertEquals("Q 마감 11/29(일): " + NEXT_YES.formatted("11/29(일)"), reasons(16, 23, "01:00"));
    }
}
