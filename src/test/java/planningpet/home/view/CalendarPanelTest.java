package planningpet.home.view;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JLabel;
import org.junit.jupiter.api.Test;
import planningpet.app.model.AppData;

/** 홈 달력의 몸. 번호는 docs/porting/home.md S3f 2절. */
class CalendarPanelTest {
    private static final ZoneId ZONE = ZoneId.systemDefault();
    private final List<LocalDate> picks = new ArrayList<>();
    private AppData data;

    private CalendarPanel panel(String now, DayOfWeek start) {
        data = new AppData(Clock.fixed(LocalDateTime.parse(now).atZone(ZONE).toInstant(), ZONE));
        data.setWeekStart(start);
        CalendarPanel c = new CalendarPanel(data, picks::add);
        c.refresh();
        return c;
    }

    /** 칸 수와 빈 자리. 빈 자리는 글도 마우스 듣는이도 없는 JLabel이다. */
    private static String layout(CalendarPanel c) {
        Component[] cs = c.grid.getComponents();
        List<Integer> empty = new ArrayList<>();
        for (int i = 0; i < cs.length; i++) {
            if (!(cs[i] instanceof DayCell)) {
                empty.add(i);
                assertEquals("JLabel '' 0", cs[i].getClass().getSimpleName() + " '" + ((JLabel) cs[i]).getText() + "' "
                        + cs[i].getMouseListeners().length);
            }
        }
        return cs.length + " empty " + empty;
    }

    private static DayCell cell(CalendarPanel c, int day) {
        for (Component k : c.grid.getComponents()) {
            if (k instanceof DayCell d && d.getText().matches("<html><center>(<b>)?" + day + "(</b>)?(<br>.*| .*)?</center></html>")) {
                return d;
            }
        }
        throw new AssertionError(day);
    }

    @Test
    void eMon1MondayStart() {
        CalendarPanel c = panel("2026-11-16T10:00", DayOfWeek.MONDAY);
        assertEquals("42 empty [0, 1, 2, 3, 4, 5, 36, 37, 38, 39, 40, 41]", layout(c));
    }

    @Test
    void eMon2SundayStart() {
        CalendarPanel c = panel("2026-11-16T10:00", DayOfWeek.SUNDAY);
        assertEquals("35 empty [30, 31, 32, 33, 34]", layout(c));
    }

    @Test
    void eMon3FourRowsAndNewYear() {
        assertEquals("28 empty []", layout(panel("2027-02-10T10:00", DayOfWeek.MONDAY)));
        assertEquals("35 empty [0, 29, 30, 31, 32, 33, 34]", layout(panel("2027-02-10T10:00", DayOfWeek.SUNDAY)));
    }

    @Test
    void eMon7TodayIsPicked() {
        CalendarPanel c = panel("2026-11-16T10:00", DayOfWeek.MONDAY);
        assertEquals(LocalDate.of(2026, 11, 16), c.selected());
        assertEquals("<html><center><b>16</b></center></html>", cell(c, 16).getText());
        assertEquals("<html><center>17</center></html>", cell(c, 17).getText());
        c = panel("2026-11-17T01:00", DayOfWeek.MONDAY);
        assertEquals(LocalDate.of(2026, 11, 16), c.selected());
        assertEquals("<html><center><b>16</b></center></html>", cell(c, 16).getText());
    }

    @Test
    void eMon9CellPressGoesToHandler() {
        DayCell c = cell(panel("2026-11-16T10:00", DayOfWeek.MONDAY), 18);
        for (MouseListener l : c.getMouseListeners()) {
            l.mouseClicked(new MouseEvent(c, MouseEvent.MOUSE_CLICKED, 0, 0, 5, 5, 1, false));
        }
        assertEquals(List.of(LocalDate.of(2026, 11, 18)), picks);
    }
}
