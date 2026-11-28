package planningpet.app.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Component;
import java.awt.Container;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import planningpet.app.controller.AppController;
import planningpet.app.controller.DayWatcher;
import planningpet.app.model.AppData;
import planningpet.app.model.Apps;
import planningpet.app.model.DataStore;
import planningpet.app.model.LoadResult;
import planningpet.app.model.MemoryStore;
import planningpet.common.view.ThemeOn;
import planningpet.home.controller.HomeController;
import planningpet.home.view.CalendarPanel;
import planningpet.home.view.MainScreen;
import planningpet.reward.controller.RewardController;
import planningpet.schedule.model.Task;
import planningpet.schedule.model.Tasks;

/** docs/porting/app-window.md 2절의 E-TRAY-1과 docs/porting/tray.md 2절. 타이머를 켜지 않고 한 번 돈 것처럼 부른다. */
@ExtendWith(ThemeOn.class)
class TrayTest {
    private static final ZoneId ZONE = ZoneId.systemDefault();
    private static final LocalDateTime AT_1450 = LocalDateTime.of(2026, 11, 16, 14, 50);
    private static final String ALERT = "플래닝펫 통계 과제 10분 뒤 시작";
    private int heard;
    /** 운영체제 알림과 창 앞으로 가져오기의 차례. */
    private final List<String> got = new ArrayList<>();
    private AppData data;
    private AppController app;
    private AppWindow window;
    private MainScreen home;
    private Tray tray;

    static Clock at(LocalDateTime time) {
        return Clock.fixed(time.atZone(ZONE).toInstant(), ZONE);
    }

    /** 지금 11-16 14:50, tasks가 든 앱. 창 틀에 홈과 일정관리를 넣는다. */
    private void open(Task... tasks) {
        data = Apps.data("2026-11-16T14:50", tasks);
        app = new AppController(data, new MemoryStore(), e -> { });
        window = new AppWindow(data, app);
        home = new MainScreen(data, app, new HomeController(data, app), new RewardController(data, app));
        window.watch(home::refresh);
        window.addScreen("홈", "", home);
        window.addScreen("일정관리", "", new JPanel());
        tray = new Tray(app, new DayWatcher(data, app), Apps.clock("2026-11-16T14:50"), window, home, () -> got.add("앞으로"));
        tray.notify = (title, text) -> got.add(title + " " + text);
    }

    /** 11-16 15:00 할 일 "통계 과제". */
    private static Task stats() {
        return Tasks.task("통계 과제", Tasks.d(16)).at("15:00").task();
    }

    /** c 안의 홈 달력. */
    private static CalendarPanel calendar(Container c) {
        for (Component k : c.getComponents()) {
            CalendarPanel p = k instanceof CalendarPanel found ? found : k instanceof Container n ? calendar(n) : null;
            if (p != null) {
                return p;
            }
        }
        return null;
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
        Tray tray = new Tray(app, watcher, at(LocalDateTime.of(2026, 11, 17, 4, 0, 10)), null, null, () -> { });
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

    @Test
    void E_TRAY_3_alertClickPicksDayAtHome() {
        open(stats());
        home.showDay(Tasks.d(32));
        window.show("일정관리");
        tray.tick(AT_1450);
        tray.clicked();
        assertEquals(List.of(ALERT, "앞으로"), got);
        assertEquals(List.of("홈", Tasks.d(16), Tasks.d(16)), List.of(window.current(), calendar(home).selected(), data.getLastOpenDay()));
    }

    @Test
    void E_TRAY_4_nextDayStartPicksTaskDate() {
        open(Tasks.task("야식", Tasks.d(16)).at("00:30").nextDay().task());
        home.showDay(Tasks.d(17));
        tray.tick(LocalDateTime.of(2026, 11, 17, 0, 20));
        tray.clicked();
        assertEquals(List.of("플래닝펫 야식 10분 뒤 시작", "앞으로"), got);
        assertEquals(Tasks.d(16), calendar(home).selected());
    }

    @Test
    void E_TRAY_5_secondEventOpens() {
        open(stats());
        tray.tick(AT_1450);
        tray.clicked();
        window.show("일정관리");
        tray.clicked();
        assertEquals(List.of(ALERT, "앞으로", "앞으로"), got);
        assertEquals(List.of("일정관리", Tasks.d(16)), List.of(window.current(), calendar(home).selected()));
    }

    @Test
    void E_TRAY_6_noticeClearsAlertDay() {
        open(stats());
        window.show("일정관리");
        tray.tick(AT_1450);
        tray.notice();
        tray.clicked();
        assertEquals(List.of(ALERT, "플래닝펫 플래닝펫은 트레이에서 계속 알림을 보내요", "앞으로"), got);
        assertEquals("일정관리", window.current());
    }

    @Test
    void E_TRAY_7_menuOpensAndQuits() {
        open();
        app.setQuit(() -> got.add("끝냄"));
        assertEquals(List.of("열기", "끝내기"), List.copyOf(tray.menu().keySet()));
        tray.menu().get("열기").run();
        assertEquals(List.of(List.of("앞으로"), Tasks.d(16)), List.of(List.copyOf(got), data.getLastOpenDay()));
        tray.menu().get("끝내기").run();
        assertEquals(List.of("앞으로", "끝냄"), got);
    }
}
