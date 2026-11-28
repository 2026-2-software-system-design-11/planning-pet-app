package planningpet.home.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static planningpet.schedule.model.Tasks.d;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import planningpet.app.controller.AppController;
import planningpet.app.model.AppData;
import planningpet.app.model.Apps;
import planningpet.app.model.MemoryStore;
import planningpet.schedule.model.DeleteScope;
import planningpet.schedule.model.Kind;
import planningpet.schedule.model.Tasks;

/** UC-09 달력 날짜 고르기, docs/porting/home.md S3b 2절의 경계. 오늘은 11-{today} 10:00이고 고른 고양이 나비가 있다. */
class HomeControllerTest {
    private AppData data;
    private AppController app;
    private HomeController home;
    private final MemoryStore store = new MemoryStore();
    private int heard;

    private void at(int today) {
        data = Apps.data("2026-11-" + today + "T10:00");
        app = new AppController(data, store, error -> { });
        home = new HomeController(data, app);
        data.onChange(() -> heard++);
    }

    /** 일정 하나. start가 있으면 고정 일정(60분), 없으면 할 일(30분), due가 있으면 기간 할 일. */
    private void add(String title, LocalDate day, LocalTime start, LocalDate due) {
        Tasks.Spec task = Tasks.task(title, day).at(start).period(day, due);
        data.getBook().add((start != null ? task.kind(Kind.FIXED).minutes(60) : task).draft());
    }

    private String said() {
        return app.getBubble().getLine() + " 새 말 " + app.getBubble().getNewLines() + " 세는 날 "
                + app.getBubble().getRecountDay() + " 알림 " + heard + " 저장 " + store.saves;
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

    @Test
    void T_TALK_26_periodOnlyDay() {
        at(18);
        add("E사 지원서", d(18), null, d(22));
        home.pick(d(19));
        String first = app.getBubble().getLine();
        home.pick(d(18));
        assertEquals("11월 19일은 할 일이 없어. 기간 할 일 1개가 있어.|오늘은 할 일이 없어. E사 지원서는 22일까지야, 4일 남았어. 새 말 2"
                + " 세는 날 2026-11-18 알림 2 저장 0", first + "|" + said());
    }

    @Test
    void E_HOME_9_recountAfterPick() {
        at(14);
        add("가", d(16), null, null);
        add("나", d(16), null, null);
        home.pick(d(16));
        app.change("일정 삭제", () -> data.getBook().delete(data.getBook().getTasks().get(1), DeleteScope.ONLY_THIS));
        assertEquals("11월 16일 할 일은 1개야. 새 말 1 세는 날 2026-11-16 알림 2 저장 1", said());
    }

    @Test
    void E_HOME_10_monthAndYearChange() {
        at(16);
        home.pick(LocalDate.of(2026, 12, 1));
        String lines = app.getBubble().getLine();
        home.pick(LocalDate.of(2026, 12, 31));
        lines += "|" + app.getBubble().getLine();
        home.pick(LocalDate.of(2027, 1, 1));
        assertEquals("12월 1일은 할 일이 없어.|12월 31일은 할 일이 없어.|1월 1일은 할 일이 없어. 새 말 3 세는 날 2027-01-01 알림 3 저장 0",
                lines + "|" + said());
    }
}
