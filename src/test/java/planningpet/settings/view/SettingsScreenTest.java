package planningpet.settings.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Component;
import java.io.IOException;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JLabel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import planningpet.app.controller.AppController;
import planningpet.app.model.AppData;
import planningpet.app.model.Apps;
import planningpet.app.model.MemoryStore;
import planningpet.common.view.ThemeOn;
import planningpet.settings.controller.SettingsController;
import planningpet.settings.model.Autostart;

/** 설정 화면. 번호는 docs/porting/settings.md S10b 2절. */
@ExtendWith(ThemeOn.class)
class SettingsScreenTest {
    private final AppData data = Apps.data("2026-11-18T10:00");
    private final MemoryStore store = new MemoryStore();
    private final List<String> errors = new ArrayList<>();
    private final Os os = new Os();
    @TempDir
    Path folder;

    /** 등록을 기억하는 운영체제 대역. installed가 아니면 쓸 수 없고, refuse면 거절한다. */
    private static final class Os implements Autostart {
        boolean installed = true;
        boolean on;
        IOException refuse;

        public boolean available() { return installed; }
        public boolean registered() { return on; }
        public void set(boolean on) throws IOException {
            if (refuse != null) {
                throw refuse;
            }
            this.on = on;
        }
    }

    /** Main처럼 변경 알림에 refresh를 건 화면. */
    private SettingsScreen screen() {
        SettingsScreen s = new SettingsScreen(data, new SettingsController(data, new AppController(data, store, errors::add), os), os, folder);
        data.onChange(s::refresh);
        return s;
    }

    @Test
    void tWin5ReadsOsAgain() {
        os.on = true;
        SettingsScreen s = screen();
        assertTrue(s.autostart.isSelected());
        os.on = false;
        s.refresh();
        assertFalse(s.autostart.isSelected());
    }

    @Test
    void tWin6UnavailableIsDisabledWithText() {
        os.installed = false;
        SettingsScreen s = screen();
        assertFalse(s.autostart.isEnabled());
        assertTrue(s.autostartHint.isVisible());
        assertEquals("설치 파일로 설치했을 때만 쓸 수 있어요", s.autostartHint.getText());
    }

    @Test
    void eSet6InstalledHasNoText() {
        assertFalse(screen().autostartHint.isVisible());
    }

    @Test
    void eSet10CalendarFixed() {
        SettingsScreen s = screen();
        s.calendarFixed.doClick();
        assertEquals(List.of(true, 1), List.of(data.isCalendarFixed(), store.saves));
        store.failing = true;
        s.calendarFixed.doClick();
        assertTrue(s.calendarFixed.isSelected());
        assertEquals(List.of("달력에 고정 일정 보이기 저장에 실패해서 바꾸기 전으로 돌렸어요."), errors);
    }

    @Test
    void eSet11WeekStart() {
        SettingsScreen s = screen();
        s.sunday.doClick();
        assertEquals(List.of(DayOfWeek.SUNDAY, 1), List.of(data.getWeekStart(), store.saves));
        data.setWeekStart(DayOfWeek.MONDAY);
        data.changed();
        assertTrue(s.monday.isSelected());
        store.failing = true;
        s.sunday.doClick();
        assertEquals(List.of(DayOfWeek.MONDAY, true, false), List.of(data.getWeekStart(), s.monday.isSelected(), s.sunday.isSelected()));
        assertEquals(List.of("한 주 시작 저장에 실패해서 바꾸기 전으로 돌렸어요."), errors);
    }

    @Test
    void eSet12Samples() {
        SettingsScreen s = screen();
        assertEquals(List.of(false, "예시 일정이 없어요"), List.of(s.sampleButton.isEnabled(), s.sampleCount.getText()));
        int[] pressed = {0};
        s.samples = () -> 3;
        s.clearSamples = () -> pressed[0]++;
        s.refresh();
        s.sampleButton.doClick();
        assertEquals(List.of(true, "예시 일정 3개가 남아 있어요", 1), List.of(s.sampleButton.isEnabled(), s.sampleCount.getText(), pressed[0]));
    }

    @Test
    void eSet13RowsAndVersion() {
        List<String> labels = new ArrayList<>();
        for (Component c : screen().getComponents()) {
            if (c instanceof JLabel l && l.isVisible() && !l.getText().isEmpty()) {
                labels.add(l.getText().replaceAll("<[^>]+>", ""));
            }
        }
        assertEquals(List.of("시작", "데이터 위치", "달력", "켜면 수업 같은 고정 일정도 홈 달력 칸의 수에 들어가요",
                "한 주 시작", "달력, 일정관리, 통계의 한 주가 이 요일부터 시작해요", "예시 일정", "버전", "0.1.0"), labels);
    }
}
