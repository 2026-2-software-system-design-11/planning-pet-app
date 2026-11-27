package planningpet.app.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;
import planningpet.app.controller.AppController;
import planningpet.app.controller.DayWatcher;
import planningpet.app.model.AppData;
import planningpet.app.model.DataStore;
import planningpet.app.model.LoadResult;

/** docs/porting/app-window.md 2절의 E-TRAY-1. 타이머를 켜지 않고 한 번 돈 것처럼 부른다. */
class TrayTest {
    private static final ZoneId ZONE = ZoneId.systemDefault();
    private int heard;

    static Clock at(LocalDateTime time) {
        return Clock.fixed(time.atZone(ZONE).toInstant(), ZONE);
    }

    @Test
    void E_TRAY_1_everyThirtySecondsTicksNow() {
        AppData data = new AppData(at(LocalDateTime.of(2026, 11, 16, 10, 0)));
        AppController app = new AppController(data, new DataStore() {
            public LoadResult load() { return LoadResult.firstRun(); }
            public void save(AppData d) { }
        }, e -> { });
        DayWatcher watcher = new DayWatcher(data, app);
        data.onChange(() -> heard++);
        Tray tray = new Tray(watcher, at(LocalDateTime.of(2026, 11, 17, 4, 0, 10)));
        assertEquals(30_000, tray.timer().getDelay());
        assertTrue(tray.timer().isRepeats());
        tray.timer().getActionListeners()[0].actionPerformed(null);
        assertEquals(1, heard);
    }
}
