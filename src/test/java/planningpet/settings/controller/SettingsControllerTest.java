package planningpet.settings.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import planningpet.app.controller.AppController;
import planningpet.app.model.AppData;
import planningpet.app.model.Apps;
import planningpet.app.model.MemoryStore;
import planningpet.settings.model.Autostart;

/** 설정 controller. 번호는 docs/porting/settings.md S10a 2절. */
class SettingsControllerTest {
    private final AppData data = Apps.data("2026-11-18T10:00");
    private final MemoryStore store = new MemoryStore();
    private final List<String> errors = new ArrayList<>();
    private final Os os = new Os();
    private final SettingsController settings = new SettingsController(data, new AppController(data, store, errors::add), os);
    private int changes;

    /** 등록을 기억하는 운영체제 대역. refuse면 거절한다. */
    private static final class Os implements Autostart {
        boolean on;
        IOException refuse;

        public boolean available() { return true; }
        public boolean registered() { return on; }
        public void set(boolean on) throws IOException {
            if (refuse != null) {
                throw refuse;
            }
            this.on = on;
        }
    }

    @Test
    void eSet1CalendarFixed() {
        data.onChange(() -> changes++);
        settings.setCalendarFixed(true);
        assertTrue(data.isCalendarFixed());
        settings.setCalendarFixed(true);
        assertEquals(2, changes);
        assertEquals(2, store.saves);
    }

    @Test
    void eSet2WeekStart() {
        settings.setWeekStart(DayOfWeek.SUNDAY);
        assertEquals(DayOfWeek.SUNDAY, data.getWeekStart());
        settings.setWeekStart(DayOfWeek.MONDAY);
        assertEquals(DayOfWeek.MONDAY, data.getWeekStart());
        assertEquals(2, store.saves);
    }

    @Test
    void eSet3SaveFailsGoesBack() {
        store.failing = true;
        settings.setWeekStart(DayOfWeek.SUNDAY);
        settings.setCalendarFixed(true);
        assertEquals(DayOfWeek.MONDAY, data.getWeekStart());
        assertFalse(data.isCalendarFixed());
        assertEquals(List.of("한 주 시작 저장에 실패해서 바꾸기 전으로 돌렸어요.",
                "달력에 고정 일정 보이기 저장에 실패해서 바꾸기 전으로 돌렸어요."), errors);
    }

    @Test
    void tWin4AutostartRegistersWithoutSaving() throws IOException {
        data.onChange(() -> changes++);
        settings.setAutostart(true);
        assertTrue(os.registered());
        settings.setAutostart(false);
        assertFalse(os.registered());
        assertEquals(0, store.saves);
        assertEquals(0, changes);
    }

    @Test
    void eSet4AutostartRefusedReachesScreen() {
        os.refuse = new IOException("액세스가 거부되었습니다.");
        IOException e = assertThrows(IOException.class, () -> settings.setAutostart(true));
        assertSame(os.refuse, e);
        assertFalse(os.registered());
        assertEquals(0, store.saves);
    }
}
