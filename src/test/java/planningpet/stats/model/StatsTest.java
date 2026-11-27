package planningpet.stats.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import planningpet.app.model.AppData;
import planningpet.pet.model.PetKind;
import planningpet.schedule.model.Category;
import planningpet.schedule.model.DeleteScope;
import planningpet.schedule.model.Kind;
import planningpet.schedule.model.Task;

/** 06 29번과 docs/porting/stats.md 2절의 E-STAT 경계. 따로 정하지 않으면 지금은 11-17(화) 12:00, 한 주 시작 월요일이다. */
class StatsTest {
    private final List<Task> tasks = new ArrayList<>();
    private AppData data;
    private Stats stats;

    private static LocalDate d(int day) { return LocalDate.of(2026, 11, day); }

    /** 날짜 date인 일정. done이면 그날 끝냈다. 기간 할 일이면 due와 끝낸 날 doneDay를 준다. */
    private Task put(Kind kind, LocalDate date, Category c, boolean done, LocalDate due, LocalDate doneDay) {
        Task t = new Task("t" + tasks.size(), tasks.size() + 1, kind, "t", date, null, 30, "", false, c, null, null,
                due, due == null ? null : date, done, done && doneDay == null ? date : doneDay, 0, 0, null, null);
        tasks.add(t);
        return t;
    }

    private Task task(int day, boolean done) { return put(Kind.TASK, d(day), Category.OTHER, done, null, null); }

    private Task period(int from, int due, Integer doneDay) {
        return put(Kind.TASK, d(from), Category.OTHER, doneDay != null, d(due), doneDay == null ? null : d(doneDay));
    }

    /** 넣은 일정으로 시각 now의 앱 데이터를 만든다. */
    private void open(LocalDateTime now) {
        data = new AppData(Clock.fixed(now.toInstant(ZoneOffset.UTC), ZoneOffset.UTC), tasks, List.of());
        stats = new Stats(data);
    }

    private void open() { open(LocalDateTime.of(2026, 11, 17, 12, 0)); }

    private Period week() { return Period.week(data.today(), data.getWeekStart()); }

    /** 날짜 date인 할 일을 rewardDay에 끝내고 10P를 받았다. */
    private Task rewarded(int date, int rewardDay) {
        Task t = task(date, false);
        t.finish(d(rewardDay), 10, 10, PetKind.CAT);
        return t;
    }

    /** 추이 칸마다 "첫날~끝날 끝낸 수/셀 일정 수". 날은 월-일이다. */
    private String trend(Period p) {
        return String.join(", ", stats.trend(p).stream().map(b -> b.from().getMonthValue() + "-" + b.from().getDayOfMonth()
                + "~" + b.to().getMonthValue() + "-" + b.to().getDayOfMonth() + " " + b.rate().done() + "/"
                + b.rate().total()).toList());
    }

    private void stat1() {
        put(Kind.TASK, d(16), Category.EXERCISE, true, null, null);
        task(16, false);
        task(17, false);
        task(18, false);
        open();
    }

    @Test
    void T_STAT_1_futureNotCounted() {
        stat1();
        assertEquals(new Rate(1, 2), stats.rate(week()));
    }

    @Test
    void T_STAT_6_noTasks() {
        open();
        assertEquals(new Rate(0, 0), stats.rate(week()));
    }

    @Test
    void T_STAT_8_todayOnlyDone() {
        task(17, true);
        Task open = task(17, false);
        open();
        assertEquals(new Rate(1, 1), stats.rate(week()));
        open.finish(d(17), 0, 0, null);
        assertEquals(new Rate(2, 2), stats.rate(week()));
    }

    @Test
    void T_STAT_9_periodTaskCountsOnDueDay() {
        period(9, 16, null);
        period(16, 22, null);
        period(10, 20, 16);
        open();
        assertEquals(new Rate(1, 2), stats.rate(week()));
        assertEquals(new Rate(0, 0), stats.rate(week().step(-1)));
    }

    @Test
    void E_STAT_5_appDayEndsAt4() {
        task(17, false);
        open(LocalDateTime.of(2026, 11, 18, 3, 59));
        assertEquals(new Rate(0, 0), stats.rate(week()));
        open(LocalDateTime.of(2026, 11, 18, 4, 0));
        assertEquals(new Rate(0, 1), stats.rate(week()));
    }

    @Test
    void E_STAT_11_periodTaskDoneAfterDue() {
        period(9, 12, 16);
        period(9, 12, null);
        open();
        assertEquals(new Rate(1, 1), stats.rate(week()));
        assertEquals(new Rate(0, 1), stats.rate(week().step(-1)));
    }

    @Test
    void T_STAT_2_onlyCategoriesWithTasks() {
        stat1();
        assertEquals(Map.of(Category.EXERCISE, new Rate(1, 1), Category.OTHER, new Rate(0, 1)), stats.byCategory(week()));
    }

    @Test
    void E_STAT_6_fixedCounts() {
        put(Kind.FIXED, d(16), Category.STUDY, false, null, null);
        put(Kind.FIXED, d(17), Category.STUDY, true, null, null);
        open();
        assertEquals(new Rate(1, 2), stats.rate(week()));
        assertEquals(Map.of(Category.STUDY, new Rate(1, 2)), stats.byCategory(week()));
    }

    @Test
    void E_STAT_9_categoryOrder() {
        List<Category> added = List.of(Category.OTHER, Category.PERSONAL, Category.PROJECT, Category.EXERCISE,
                Category.STUDY);
        added.forEach(c -> put(Kind.TASK, d(16), c, false, null, null));
        open();
        assertEquals(List.of(Category.values()), List.copyOf(stats.byCategory(week()).keySet()));
    }

    @Test
    void T_STAT_3_weekHasSevenDays() {
        stat1();
        assertEquals("11-16~11-16 1/2, 11-17~11-17 0/0, 11-18~11-18 0/0, 11-19~11-19 0/0, 11-20~11-20 0/0, "
                + "11-21~11-21 0/0, 11-22~11-22 0/0", trend(week()));
    }

    @Test
    void T_STAT_4_monthCutAtWeekEnd() {
        open();
        assertEquals("11-1~11-1 0/0, 11-2~11-8 0/0, 11-9~11-15 0/0, 11-16~11-22 0/0, 11-23~11-29 0/0, 11-30~11-30 0/0",
                trend(Period.month(d(17))));
    }

    @Test
    void T_STAT_10_weekStartsSunday() {
        open();
        data.setWeekStart(DayOfWeek.SUNDAY);
        assertEquals(new Period(d(15), d(21), false), week());
        assertEquals("11-1~11-7 0/0, 11-8~11-14 0/0, 11-15~11-21 0/0, 11-22~11-28 0/0, 11-29~11-30 0/0",
                trend(Period.month(d(17))));
    }

    @Test
    void E_STAT_4_monthEdgesOnWeekEdges() {
        open();
        assertEquals("2-1~2-7 0/0, 2-8~2-14 0/0, 2-15~2-21 0/0, 2-22~2-28 0/0",
                trend(Period.month(LocalDate.of(2027, 2, 1))));
        assertEquals("5-1~5-3 0/0, 5-4~5-10 0/0, 5-11~5-17 0/0, 5-18~5-24 0/0, 5-25~5-31 0/0",
                trend(Period.month(LocalDate.of(2026, 5, 1))));
    }

    @Test
    void E_STAT_8_futurePeriod() {
        stat1();
        Period next = week().step(1);
        assertEquals(new Rate(0, 0), stats.rate(next));
        assertEquals("11-23~11-23 0/0, 11-24~11-24 0/0, 11-25~11-25 0/0, 11-26~11-26 0/0, 11-27~11-27 0/0, "
                + "11-28~11-28 0/0, 11-29~11-29 0/0", trend(next));
        assertEquals(Map.of(), stats.byCategory(next));
    }

    @Test
    void E_STAT_12_todayInsideMonthBucket() {
        task(16, false);
        task(17, true);
        task(17, false);
        open();
        assertEquals("11-1~11-1 0/0, 11-2~11-8 0/0, 11-9~11-15 0/0, 11-16~11-22 1/2, 11-23~11-29 0/0, 11-30~11-30 0/0",
                trend(Period.month(d(17))));
        assertEquals(new Rate(1, 2), stats.rate(Period.month(d(17))));
    }

    @Test
    void T_STAT_5_T_STAT_7_pointsKeptAfterDelete() {
        Task first = rewarded(17, 17);
        rewarded(17, 17);
        rewarded(16, 17);
        open();
        data.setAttendDays(Set.of(d(16), d(17)));
        assertEquals(70, stats.points(week()));
        data.getBook().delete(first, DeleteScope.ONLY_THIS);
        assertEquals(70, stats.points(week()));
    }

    @Test
    void E_STAT_7_futureTaskDoneToday() {
        rewarded(19, 17);
        open();
        assertEquals(new Rate(0, 0), stats.rate(week()));
        assertEquals(List.of(), stats.trend(week()).stream().filter(b -> b.rate().total() > 0).toList());
        assertEquals(10, stats.points(week()));
    }

    @Test
    void E_STAT_10_pointsAtPeriodEnds() {
        rewarded(15, 15);
        rewarded(16, 16);
        rewarded(22, 22);
        task(16, false).finish(d(16), 0, 0, null);
        open();
        data.setAttendDays(Set.of(d(15), d(16), d(22), d(23)));
        assertEquals(60, stats.points(week()));
        assertEquals(30, stats.points(week().step(-1)));
        assertEquals(110, stats.points(Period.month(d(17))));
    }
}
