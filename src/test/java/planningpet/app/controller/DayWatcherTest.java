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
}
