package planningpet.stats.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import planningpet.app.model.AppData;
import planningpet.schedule.model.Category;
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
}
