package planningpet.app.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;
import planningpet.app.model.AppData;
import planningpet.app.model.DataStore;
import planningpet.app.model.LoadResult;
import planningpet.pet.model.PetKind;
import planningpet.pet.model.PetState;

/** 06 23번과 docs/porting/app-window.md 2절의 E-DAYW 경계. 시계는 tick이 옮기고, 고양이 "나비"가 있다. */
class DayWatcherTest {
    private static final ZoneId ZONE = ZoneId.systemDefault();
    private LocalDateTime now;
    private AppData data;
    private AppController app;
    private DayWatcher watcher;
    private int heard;

    /** at에 앱을 만든다. 마지막으로 연 날은 last, 연속 접속은 1일이다. */
    void start(LocalDateTime at, LocalDate last) {
        now = at;
        data = new AppData(new Clock() {
            public ZoneId getZone() { return ZONE; }
            public Clock withZone(ZoneId zone) { return this; }
            public Instant instant() { return now.atZone(ZONE).toInstant(); }
        });
        data.addPet(new PetState(PetKind.CAT, "나비"));
        data.setActiveKind(PetKind.CAT);
        data.setLastOpenDay(last);
        data.setStreak(1);
        app = new AppController(data, new DataStore() {
            public LoadResult load() { return LoadResult.firstRun(); }
            public void save(AppData d) { }
        }, e -> { });
        watcher = new DayWatcher(data, app);
        data.onChange(() -> heard++);
    }

    /** 시계를 at으로 옮기고 확인한다. */
    void tick(LocalDateTime at) {
        now = at;
        watcher.tick(at);
    }

    static LocalDateTime t(int day, int hour, int minute, int second) {
        return LocalDateTime.of(2026, 11, day, hour, minute, second);
    }

    static LocalDate d(int day) { return LocalDate.of(2026, 11, day); }

    @Test
    void T_DAYW_1_onceAfterFour() {
        start(t(17, 3, 59, 40), d(16));
        tick(t(17, 4, 0, 10));
        assertEquals(1, heard);
        tick(t(17, 4, 0, 40));
        assertEquals(1, heard);
    }

    @Test
    void T_DAYW_2_shownWindowBriefs() {
        start(t(17, 3, 59, 40), d(16));
        app.onWindowShown();
        heard = 0;
        tick(t(17, 4, 0, 10));
        assertEquals("오늘은 할 일이 없어. 아래 칸에 하나 적어 볼까?", app.getBubble().getLine());
        assertEquals(d(17), data.getLastOpenDay());
        assertEquals(2, data.getStreak());
        assertEquals(1, heard);
    }

    @Test
    void T_DAYW_3_hiddenWindowWaits() {
        start(t(17, 3, 59, 40), d(16));
        tick(t(17, 4, 0, 10));
        assertEquals("", app.getBubble().getLine());
        assertEquals(d(16), data.getLastOpenDay());
        app.onWindowShown();
        assertEquals("오늘은 할 일이 없어. 아래 칸에 하나 적어 볼까?", app.getBubble().getLine());
        assertEquals(2, data.getStreak());
    }

    @Test
    void T_DAYW_5_wakeAfterSleep() {
        start(t(16, 23, 0, 0), d(16));
        tick(t(18, 10, 0, 0));
        tick(t(18, 10, 0, 0));
        assertEquals(1, heard);
    }

    @Test
    void E_DAYW_1_sameDay() {
        start(t(16, 10, 0, 0), d(16));
        tick(t(16, 10, 0, 0));
        assertEquals(0, heard);
    }

    @Test
    void E_DAYW_2_midnight() {
        start(t(16, 23, 59, 50), d(16));
        tick(t(17, 0, 0, 20));
        assertEquals(0, heard);
    }

    @Test
    void E_DAYW_3_clockBackBeforeFour() {
        start(t(17, 4, 0, 10), d(17));
        app.onWindowShown();
        heard = 0;
        tick(t(17, 3, 59, 50));
        assertEquals(1, heard);
        assertEquals("오늘은 할 일이 없어. 아래 칸에 하나 적어 볼까?", app.getBubble().getLine());
        assertEquals(d(16), data.getLastOpenDay());
        assertEquals(1, data.getStreak());
    }
}
