package planningpet.reward.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.Test;
import planningpet.app.model.AppData;
import planningpet.pet.model.PetKind;
import planningpet.pet.model.PetState;
import planningpet.schedule.model.Category;
import planningpet.schedule.model.Kind;
import planningpet.schedule.model.Task;

/** 06 13번과 docs/porting/rewards.md 2절. at은 시계와 일정, 고른 고양이를 정하고 progress는 "끝낸 수 / 할 일 수"다. */
class DailyProgressTest {
    private static final ZoneId ZONE = ZoneId.systemDefault();
    private AppData data;

    private void at(String now, Task... tasks) {
        data = new AppData(Clock.fixed(LocalDateTime.parse(now).atZone(ZONE).toInstant(), ZONE), List.of(tasks),
                List.of());
        data.addPet(new PetState(PetKind.CAT, "나비"));
        data.setActiveKind(PetKind.CAT);
    }

    private static Task make(Kind kind, String title, int day, LocalTime start, boolean nextDay, LocalDate due) {
        return new Task(title, 1, kind, title, LocalDate.of(2026, 11, day), start, 30, "", nextDay, Category.OTHER,
                null, null, due, due == null ? null : LocalDate.of(2026, 11, 16), false, null, 0, 0, null, null);
    }

    private static Task task(String title, int day) {
        return make(Kind.TASK, title, day, null, false, null);
    }

    private String progress() {
        DailyProgress p = DailyProgress.of(data.getBook(), data.today());
        return p.getDone() + " / " + p.getTotal();
    }

    private void complete(Task t) {
        new Rewards(data).complete(t, data.today());
    }

    @Test
    void T_PRG_1_someDone() {
        Task first = task("T0", 16);
        at("2026-11-16T10:00", first, task("T1", 16), task("T2", 16), task("T3", 16));
        complete(first);
        assertEquals("1 / 4", progress());
    }

    @Test
    void T_PRG_2_empty() {
        at("2026-11-16T10:00");
        DailyProgress p = DailyProgress.of(data.getBook(), data.today());
        assertEquals("0 / 0 0.0 true", progress() + " " + p.ratio() + " " + p.isEmpty());
    }

    @Test
    void T_PRG_3_fixedCount() {
        Task f0 = make(Kind.FIXED, "F0", 16, LocalTime.of(9, 0), false, null);
        Task t0 = task("T0", 16);
        at("2026-11-16T10:00", f0, make(Kind.FIXED, "F1", 16, LocalTime.of(10, 0), false, null), t0, task("T1", 16));
        complete(f0);
        complete(t0);
        assertEquals("2 / 4", progress());
    }

    @Test
    void T_PRG_4_yesterdayNotCounted() {
        Task y = task("Y", 15);
        at("2026-11-16T10:00", y, task("T", 16));
        complete(y);
        assertEquals("0 / 1", progress());
    }

    @Test
    void T_PRG_5_cappedStillCounted() {
        Task t0 = task("T0", 16);
        Task t1 = task("T1", 16);
        at("2026-11-16T10:00", t0, t1);
        data.setRewardCount(LocalDate.of(2026, 11, 16), 20);
        complete(t0);
        complete(t1);
        assertEquals("2 / 2 0", progress() + " " + data.getPoints());
    }

    @Test
    void T_PRG_6_periodNotCounted() {
        Task t = task("T", 17);
        at("2026-11-17T10:00", t, make(Kind.TASK, "P", 16, null, false, LocalDate.of(2026, 11, 22)));
        complete(t);
        assertEquals("1 / 1 기간 할 일 1개 (~11/22)", progress() + " " + data.getBook().periodNote(data.today()));
        Task last = task("T", 22);
        at("2026-11-22T10:00", last, make(Kind.TASK, "P", 16, null, false, LocalDate.of(2026, 11, 22)));
        complete(last);
        assertEquals("1 / 2", progress());
    }

    @Test
    void T_PRG_7_afterMidnightCountsForDate() {
        Task x = make(Kind.TASK, "복습", 20, LocalTime.of(1, 30), true, null);
        at("2026-11-21T01:45", x);
        complete(x);
        assertEquals("1 / 1 2026-11-20 2026-11-20 1", progress() + " " + x.getRewardDay() + " "
                + data.getRewardCountDay() + " " + data.getRewardCount());
    }

    @Test
    void T_LIST_3_onlyFixed() {
        at("2026-11-16T10:00", make(Kind.FIXED, "F0", 16, LocalTime.of(9, 0), false, null),
                make(Kind.FIXED, "F1", 16, LocalTime.of(10, 0), false, null));
        assertEquals("0 / 2", progress());
    }

    @Test
    void T_LIST_7_nightOfDate() {
        at("2026-11-21T01:30", make(Kind.TASK, "A", 20, LocalTime.of(23, 40), false, null),
                make(Kind.TASK, "B", 20, LocalTime.of(8, 0), false, null), task("C", 20),
                make(Kind.TASK, "복습", 20, LocalTime.of(1, 30), true, null));
        assertEquals("0 / 4", progress());
    }
}
