package planningpet.app.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import planningpet.app.model.AppData;
import planningpet.app.model.Apps;
import planningpet.app.model.MemoryStore;
import planningpet.schedule.model.Tasks;

/** 06 25번 창 닫기와 끝내기. 번호는 docs/porting/tray.md 2절. 지금은 11-16(월) 14:50이다. */
class AppControllerCloseTest {
    private final AppData data = Apps.data("2026-11-16T14:50");
    private final MemoryStore store = new MemoryStore();
    private final List<String> errors = new ArrayList<>();
    private final AppController app = new AppController(data, store, errors::add);
    private int heard;

    AppControllerCloseTest() {
        data.onChange(() -> heard++);
    }

    @Test
    void T_WIN_1_firstCloseNotices() {
        app.onWindowShown();
        int saves = store.saves;
        assertEquals(CloseAction.HIDE_WITH_NOTICE, app.onClose(true));
        assertFalse(app.isWindowVisible());
        assertTrue(data.isTrayNoticeShown());
        assertEquals(saves + 1, store.saves);
    }

    @Test
    void T_WIN_2_laterCloseHides() {
        data.setTrayNoticeShown(true);
        app.onWindowShown();
        int saves = store.saves;
        assertEquals(CloseAction.HIDE, app.onClose(true));
        assertFalse(app.isWindowVisible());
        assertEquals(saves, store.saves);
    }

    @Test
    void T_WIN_3_noTrayQuits() {
        assertEquals(CloseAction.QUIT, app.onClose(false));
        assertFalse(data.isTrayNoticeShown());
        assertEquals(0, store.saves);
    }

    @Test
    void E_CLOSE_1_noticeSaveFails() {
        store.failing = true;
        assertEquals(CloseAction.HIDE_WITH_NOTICE, app.onClose(true));
        assertEquals(CloseAction.HIDE_WITH_NOTICE, app.onClose(true));
        assertFalse(data.isTrayNoticeShown());
        String text = "처음 창 닫기 안내 저장에 실패해서 바꾸기 전으로 돌렸어요.";
        assertEquals(List.of(text, text), errors);
    }

    @Test
    void E_CLOSE_2_closedWindowWaitsForNewDay() {
        data.setLastOpenDay(Tasks.d(16));
        data.setTrayNoticeShown(true);
        DayWatcher watcher = new DayWatcher(data, app);
        app.onWindowShown();
        String line = app.getBubble().getLine();
        app.onClose(true);
        heard = 0;
        watcher.tick(LocalDateTime.of(2026, 11, 17, 4, 0, 10));
        assertEquals(line, app.getBubble().getLine());
        assertEquals(Tasks.d(16), data.getLastOpenDay());
        assertEquals(1, heard);
    }

    @Test
    void E_CLOSE_3_quitRunsGivenExit() {
        app.quit();
        int[] quits = {0};
        app.setQuit(() -> quits[0]++);
        app.quit();
        assertEquals(1, quits[0]);
    }
}
