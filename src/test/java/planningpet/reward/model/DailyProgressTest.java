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
}
