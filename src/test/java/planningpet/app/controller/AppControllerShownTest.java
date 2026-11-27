package planningpet.app.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;
import planningpet.app.model.AppData;
import planningpet.app.model.DataStore;
import planningpet.app.model.LoadResult;
import planningpet.pet.model.PetKind;
import planningpet.pet.model.PetState;
import planningpet.schedule.model.Category;
import planningpet.schedule.model.Kind;
import planningpet.schedule.model.TaskDraft;

/** 06 15번과 docs/porting/app-window.md 2절의 E-BRF 경계. at은 11월 day일 hour시, 마지막으로 연 날, 연속 접속 일수다. */
class AppControllerShownTest {
    private AppData data;
    private AppController app;
    private int saves;
    private int heard;

    void at(int day, int hour, LocalDate last, int streak) {
        ZoneId zone = ZoneId.systemDefault();
        data = new AppData(Clock.fixed(LocalDateTime.of(2026, 11, day, hour, 0).atZone(zone).toInstant(), zone));
        data.addPet(new PetState(PetKind.CAT, "나비"));
        data.setActiveKind(PetKind.CAT);
        data.setLastOpenDay(last);
        data.setStreak(streak);
        data.onChange(() -> heard++);
        app = new AppController(data, new DataStore() {
            public LoadResult load() { return LoadResult.firstRun(); }
            public void save(AppData d) { saves++; }
        }, e -> { });
    }

    static LocalDate d(int day) { return LocalDate.of(2026, 11, day); }

    void task(String title, int day) {
        data.getBook().add(new TaskDraft(Kind.TASK, title, Category.OTHER, d(day), null, null, 30, "", null,
                null, false, null, null, false));
    }

    void shown(String line, LocalDate last, int streak) {
        app.onWindowShown();
        assertEquals(line, app.getBubble().getLine());
        assertEquals(last, data.getLastOpenDay());
        assertEquals(streak, data.getStreak());
    }

    @Test
    void T_BRF_1_briefOnFirstShow() {
        at(16, 10, d(15), 5);
        task("과제", 16);
        shown("오늘 할 일 1개 남았어.", d(16), 6);
        assertEquals(1, saves);
        assertEquals(1, heard);
    }

    @Test
    void T_BRF_2_sameDayUsual() {
        at(16, 10, d(15), 5);
        task("과제", 16);
        app.onWindowShown();
        shown("오늘 할 일 1개 남았어.", d(16), 6);
        assertEquals(1, saves);
        assertEquals(2, heard);
    }
}
