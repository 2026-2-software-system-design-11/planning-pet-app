package planningpet.home.view;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JLabel;
import org.junit.jupiter.api.Test;
import planningpet.app.model.AppData;
import planningpet.common.view.Snap;
import planningpet.common.view.Theme;
import planningpet.schedule.model.Category;
import planningpet.schedule.model.Kind;
import planningpet.schedule.model.Task;

/** 홈 달력의 몸. 번호는 docs/porting/home.md S3f 2절. 기준 그림 proto/month-*.png는 칸의 숫자와 수를 비운 날짜 칸 줄이다. */
class CalendarPanelTest {
    private static final ZoneId ZONE = ZoneId.systemDefault();
    private final List<Task> tasks = new ArrayList<>();
    private final List<LocalDate> picks = new ArrayList<>();
    private AppData data;

    private CalendarPanel panel(String now, DayOfWeek start, boolean fixed) {
        data = new AppData(Clock.fixed(LocalDateTime.parse(now).atZone(ZONE).toInstant(), ZONE), tasks, List.of());
        data.setWeekStart(start);
        data.setCalendarFixed(fixed);
        CalendarPanel c = new CalendarPanel(data, picks::add);
        c.refresh();
        Theme.clear(c);
        return c;
    }

    private CalendarPanel panel(String now, DayOfWeek start) {
        return panel(now, start, false);
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

    /** 칸 줄을 글 없이 기준 그림과 맞춘다. */
    private static void looksGrid(String name, CalendarPanel c, int w, int h) throws IOException {
        for (Component k : c.grid.getComponents()) {
            if (k instanceof DayCell d) {
                d.blankText();
            }
        }
        Snap.assertLaidOutLooksLikePrototype("month-" + name + "-grid-" + w + "x" + h, c.grid, w, h);
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

    @Test
    void eMon6Pictures() throws IOException {
        String[][] months = {{"mon-2026-11", "2026-11-16T10:00", "MONDAY"}, {"sun-2026-11", "2026-11-16T10:00", "SUNDAY"},
            {"mon-2027-02", "2027-02-10T10:00", "MONDAY"}, {"due-plain", "2026-11-16T10:00", "MONDAY"}};
        for (String[] m : months) {
            tasks.clear();
            if (m[0].equals("due-plain")) {
                p4();
            }
            for (int[] s : new int[][] {{328, 260}, {560, 330}, {140, 90}, {0, 0}}) {
                looksGrid(m[0], panel(m[1], DayOfWeek.valueOf(m[2])), s[0], s[1]);
            }
        }
    }

    private void task(Kind kind, String title, int date, String start, int from, int due, int doneDay) {
        tasks.add(new Task("id" + tasks.size(), tasks.size() + 1, kind, title, LocalDate.of(2026, 11, date),
                start == null ? null : LocalTime.parse(start), 30, "", false, Category.OTHER, null, null,
                due > 0 ? LocalDate.of(2026, 11, due) : null, due > 0 ? LocalDate.of(2026, 11, from) : null,
                doneDay > 0, doneDay > 0 ? LocalDate.of(2026, 11, doneDay) : null, 0, 0, null, null));
    }

    /** 2절의 P4 일정. */
    private void p4() {
        tasks.clear();
        task(Kind.TASK, "a", 18, null, 0, 0, 0);
        task(Kind.TASK, "b", 18, null, 0, 0, 0);
        task(Kind.TASK, "c", 13, null, 0, 0, 13);
        task(Kind.TASK, "E사 지원서", 10, null, 10, 20, 0);
        task(Kind.TASK, "F사 서류", 12, null, 12, 20, 0);
        task(Kind.TASK, "P", 18, "09:00", 16, 22, 0);
        task(Kind.FIXED, "수업", 19, "09:00", 0, 0, 0);
    }

    @Test
    void eMon8FixedSetting() {
        p4();
        for (boolean fixed : new boolean[] {false, true}) {
            CalendarPanel c = panel("2026-11-16T10:00", DayOfWeek.MONDAY, fixed);
            assertEquals(fixed ? "<html><center>19<br><font size='2' color='#28977F'>1개</font></center></html>"
                    : "<html><center>19</center></html>", cell(c, 19).getText());
            assertEquals("<html><center>18<br><font size='2' color='#28977F'>3개</font></center></html>", cell(c, 18).getText());
            assertEquals("<html><center>13<br><font size='2' color='#8A96A0'>1개</font></center></html>", cell(c, 13).getText());
            assertEquals("<html><center>20<br><font size='2' color='#28977F'>2개</font> <font size='2' color='#C0392B'>마감"
                    + "</font></center></html>", cell(c, 20).getText());
            assertEquals("<html><center>22 <font size='2' color='#C0392B'>마감</font></center></html>", cell(c, 22).getText());
            assertEquals("E사 지원서 마감, F사 서류 마감", cell(c, 20).getToolTipText());
        }
    }
}
