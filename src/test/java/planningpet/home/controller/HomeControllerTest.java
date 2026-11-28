package planningpet.home.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;
import planningpet.app.controller.AppController;
import planningpet.app.model.AppData;
import planningpet.app.model.DataStore;
import planningpet.app.model.LoadResult;
import planningpet.pet.model.PetKind;
import planningpet.pet.model.PetState;
import planningpet.schedule.model.Category;
import planningpet.schedule.model.Kind;
import planningpet.schedule.model.TaskDraft;

/** UC-09 달력 날짜 고르기, docs/porting/home.md S3b 2절의 경계. 오늘은 11-{today} 10:00이고 고른 고양이 나비가 있다. */
class HomeControllerTest {
    private static final ZoneId ZONE = ZoneId.systemDefault();
    private AppData data;
    private AppController app;
    private HomeController home;
    private int saves;
    private int heard;

    private void at(int today) {
        data = new AppData(Clock.fixed(LocalDateTime.of(2026, 11, today, 10, 0).atZone(ZONE).toInstant(), ZONE));
        data.addPet(new PetState(PetKind.CAT, "나비"));
        data.setActiveKind(PetKind.CAT);
        app = new AppController(data, new DataStore() {
            public LoadResult load() { return LoadResult.firstRun(); }
            public void save(AppData d) { saves++; }
        }, error -> { });
        home = new HomeController(data, app);
        data.onChange(() -> heard++);
    }

    static LocalDate d(int day) { return LocalDate.of(2026, 11, day); }

    /** 일정 하나. start가 있으면 고정 일정(60분), 없으면 할 일(30분), due가 있으면 기간 할 일. */
    private void add(String title, LocalDate day, LocalTime start, LocalDate due) {
        data.getBook().add(new TaskDraft(start != null ? Kind.FIXED : Kind.TASK, title, Category.OTHER, day, due, start,
                start != null ? 60 : 30, "", null, null, false, null, null, false));
    }

    private String said() {
        return app.getBubble().getLine() + " 새 말 " + app.getBubble().getNewLines() + " 세는 날 "
                + app.getBubble().getRecountDay() + " 알림 " + heard + " 저장 " + saves;
    }

    @Test
    void T_TALK_9_otherDateCountsFixed() {
        at(14);
        add("x", d(16), LocalTime.of(9, 0), null);
        add("y", d(16), LocalTime.of(9, 0), null);
        home.pick(d(16));
        assertEquals("11월 16일 할 일은 2개야. 새 말 1 세는 날 2026-11-16 알림 1 저장 0", said());
    }

    @Test
    void E_HOME_8_pickToday() {
        at(16);
        add("가", d(16), null, null);
        home.pick(d(16));
        assertEquals("오늘 할 일 1개 남았어. 새 말 1 세는 날 2026-11-16 알림 1 저장 0", said());
    }
}
