package planningpet.app.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

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
import planningpet.schedule.model.DeleteScope;
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

    @Test
    void T_BRF_3_notShownSaysNothing() {
        at(16, 10, d(15), 5);
        assertFalse(app.isWindowVisible());
        assertEquals("", app.getBubble().getLine());
        shown("오늘은 할 일이 없어. 아래 칸에 하나 적어 볼까?", d(16), 6);
    }

    @Test
    void T_BRF_4_eightDaysWelcomeBack() {
        at(16, 10, d(8), 4);
        task("과제", 16);
        shown("오랜만이야! 다시 와서 반가워. 오늘 할 일은 1개야.", d(16), 1);
    }

    @Test
    void T_BRF_5_sevenDaysBrief() {
        at(16, 10, d(9), 4);
        shown("오늘은 할 일이 없어. 아래 칸에 하나 적어 볼까?", d(16), 1);
    }

    @Test
    void T_BRF_6_firstRun() {
        at(16, 10, null, 0);
        data.addPet(new PetState(PetKind.DOG, "바둑이"));
        data.setActiveKind(PetKind.DOG);
        shown("처음 만났네! 나는 바둑이야. 수업부터 넣어 볼까?", d(16), 1);
    }

    @Test
    void E_BRF_1_appDayEnd() {
        at(17, 3, d(16), 2);
        shown("오늘은 할 일이 없어.", d(16), 2);
        at(17, 4, d(16), 2);
        shown("오늘은 할 일이 없어. 아래 칸에 하나 적어 볼까?", d(17), 3);
    }

    @Test
    void E_BRF_2_lastOpenAfterToday() {
        at(16, 10, d(20), 3);
        shown("오늘은 할 일이 없어. 아래 칸에 하나 적어 볼까?", d(16), 1);
    }

    @Test
    void E_BRF_3_eightDaysOverMonth() {
        at(8, 10, LocalDate.of(2026, 10, 31), 3);
        shown("오랜만이야! 다시 와서 반가워.", d(8), 1);
    }

    @Test
    void E_BRF_4_firstRunDayAgain() {
        at(16, 10, null, 0);
        app.onWindowShown();
        shown("오늘은 할 일이 없어.", d(16), 1);
        assertEquals(2, heard);
    }

    @Test
    void E_BRF_6_briefRecountsToday() {
        at(16, 10, d(15), 5);
        task("과제", 16);
        task("빨래", 17);
        shown("오늘 할 일 1개 남았어. 내일 '빨래'가 있어.", d(16), 6);
        app.change("일정 삭제", () -> data.getBook().delete(data.getBook().getTasks().get(0), DeleteScope.ONLY_THIS));
        assertEquals("오늘은 할 일이 없어. 내일 '빨래'가 있어.", app.getBubble().getLine());
    }
}
