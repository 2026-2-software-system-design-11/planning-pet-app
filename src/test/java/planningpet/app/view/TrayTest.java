package planningpet.app.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import planningpet.app.controller.AppController;
import planningpet.app.controller.DayWatcher;
import planningpet.app.model.AppData;
import planningpet.app.model.Apps;
import planningpet.app.model.DataStore;
import planningpet.app.model.LoadResult;
import planningpet.app.model.MemoryStore;
import planningpet.schedule.model.Task;
import planningpet.schedule.model.Tasks;

/** docs/porting/app-window.md 2절의 E-TRAY-1과 docs/porting/tray.md 2절. 타이머를 켜지 않고 한 번 돈 것처럼 부른다. */
class TrayTest {
    private static final ZoneId ZONE = ZoneId.systemDefault();
    private static final LocalDateTime AT_1450 = LocalDateTime.of(2026, 11, 16, 14, 50);
    private static final String ALERT = "플래닝펫 통계 과제 10분 뒤 시작";
    private int heard;
    /** 운영체제 알림의 차례. */
    private final List<String> got = new ArrayList<>();
    private AppData data;
    private AppController app;
    private Tray tray;

    static Clock at(LocalDateTime time) {
        return Clock.fixed(time.atZone(ZONE).toInstant(), ZONE);
    }

    /** 지금 11-16 14:50, tasks가 든 앱. */
    private void open(Task... tasks) {
        data = Apps.data("2026-11-16T14:50", tasks);
        app = new AppController(data, new MemoryStore(), e -> { });
        tray = new Tray(app, new DayWatcher(data, app), Apps.clock("2026-11-16T14:50"));
        tray.notify = (title, text) -> got.add(title + " " + text);
    }

    /** 11-16 15:00 할 일 "통계 과제". */
    private static Task stats() {
        return Tasks.task("통계 과제", Tasks.d(16)).at("15:00").task();
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
        Tray tray = new Tray(app, watcher, at(LocalDateTime.of(2026, 11, 17, 4, 0, 10)));
        assertEquals(30_000, tray.timer().getDelay());
        assertTrue(tray.timer().isRepeats());
        tray.timer().getActionListeners()[0].actionPerformed(null);
        assertEquals(1, heard);
    }

    @Test
    void E_TRAY_2_alertOnce() {
        open(stats());
        tray.tick(AT_1450);
        tray.tick(AT_1450.plusSeconds(30));
        assertEquals(List.of(ALERT), got);
    }
}
