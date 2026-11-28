package planningpet.home.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
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
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import planningpet.app.controller.AppController;
import planningpet.app.model.AppData;
import planningpet.app.model.DataStore;
import planningpet.app.model.LoadResult;
import planningpet.common.view.CircleButton;
import planningpet.common.view.Snap;
import planningpet.common.view.Theme;
import planningpet.home.controller.HomeController;
import planningpet.pet.model.PetKind;
import planningpet.pet.model.PetState;
import planningpet.reward.controller.RewardController;
import planningpet.schedule.model.Category;
import planningpet.schedule.model.DeleteScope;
import planningpet.schedule.model.Kind;
import planningpet.schedule.model.Task;
import planningpet.schedule.model.TaskDraft;

/** 홈 달력의 몸. 번호는 docs/porting/home.md S3f 2절. 기준 그림 proto/month-*.png는 칸의 숫자와 수를 비운 날짜 칸 줄이다. */
class CalendarPanelTest {
    private static final ZoneId ZONE = ZoneId.systemDefault();
    private final List<Task> tasks = new ArrayList<>();
    private final List<LocalDate> picks = new ArrayList<>();
    private AppData data;

    @BeforeEach
    void install() {
        Theme.install();
    }

    @AfterEach
    void undoInstall() {
        for (Object key : List.copyOf(UIManager.getLookAndFeelDefaults().keySet())) {
            UIManager.put(key, null);
        }
        UIManager.put("swing.boldMetal", null);
    }

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
        assertEquals("월화수목금토일", c.weekText());
    }

    @Test
    void eMon2SundayStart() {
        CalendarPanel c = panel("2026-11-16T10:00", DayOfWeek.SUNDAY);
        assertEquals("35 empty [30, 31, 32, 33, 34]", layout(c));
        assertEquals("일월화수목금토", c.weekText());
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
    void eMon4WeekStartChanged() {
        CalendarPanel c = panel("2026-11-16T10:00", DayOfWeek.MONDAY);
        data.setWeekStart(DayOfWeek.SUNDAY);
        assertEquals("월화수목금토일", c.weekText());
        assertEquals(42, c.grid.getComponentCount());
        c.refresh();
        assertEquals("일월화수목금토", c.weekText());
        assertEquals("35 empty [30, 31, 32, 33, 34]", layout(c));
    }

    @Test
    void eMon5WeekRowAndLayout() {
        CalendarPanel c = panel("2026-11-16T10:00", DayOfWeek.MONDAY);
        Component[] days = c.week.getComponents();
        for (int i = 0; i < 7; i++) {
            JLabel l = (JLabel) days[i];
            assertEquals(i == 5 ? Theme.SATURDAY : i == 6 ? Theme.SUNDAY : Theme.INK_SUB, l.getForeground());
            assertEquals(Theme.bold(13), l.getFont());
            assertEquals(new Insets(6, 0, 2, 0), l.getInsets());
            assertEquals(SwingConstants.CENTER, l.getHorizontalAlignment());
        }
        assertEquals(25, c.week.getPreferredSize().height);
        GridLayout week = (GridLayout) c.week.getLayout();
        assertEquals("1 7 2 0", week.getRows() + " " + week.getColumns() + " " + week.getHgap() + " " + week.getVgap());
        GridLayout grid = (GridLayout) c.grid.getLayout();
        assertEquals("0 7 2 2", grid.getRows() + " " + grid.getColumns() + " " + grid.getHgap() + " " + grid.getVgap());
        assertEquals(4, ((BorderLayout) c.getLayout()).getVgap());
        assertFalse(c.isOpaque() || c.grid.isOpaque() || c.week.isOpaque());
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

    /** 패널 전체를 글 없이 기준 그림과 맞춘다. */
    private static void looksPanel(String name, CalendarPanel c, int w, int h) throws IOException {
        Snap.blank(c);
        for (Component k : c.grid.getComponents()) {
            if (k instanceof DayCell d) {
                d.blankText();
            }
        }
        Snap.assertLaidOutLooksLikePrototype("month-" + name + "-panel-" + w + "x" + h, c, w, h);
    }

    @Test
    void eHead1TitleRow() {
        CalendarPanel c = panel("2026-11-16T10:00", DayOfWeek.MONDAY);
        assertEquals("2026년 11월", c.title.getText());
        assertEquals(Theme.heavy(21), c.title.getFont());
        assertEquals("24 8", c.title.getIcon().getIconWidth() + " " + c.title.getIconTextGap());
        JPanel head = (JPanel) c.title.getParent();
        assertEquals(new Insets(0, 0, 9, 0), head.getInsets());
        JPanel arrows = (JPanel) head.getComponent(1);
        assertEquals(List.of(c.today, c.prev, c.next), List.of(arrows.getComponents()));
        FlowLayout flow = (FlowLayout) arrows.getLayout();
        assertEquals(FlowLayout.RIGHT + " 4 0", flow.getAlignment() + " " + flow.getHgap() + " " + flow.getVgap());
        assertEquals(Theme.bold(12), c.today.getFont());
        assertEquals(new Insets(5, 12, 7, 12), c.today.getInsets());
        assertEquals(new Dimension(44, 27), c.today.getPreferredSize());
        assertEquals(new Dimension(32, 32), c.prev.getPreferredSize());
        Snap.layOut(c, 328, 300);
        assertEquals(new Rectangle(0, 0, 328, 41), head.getBounds());
        assertEquals(new Rectangle(0, 41, 328, 25), c.week.getBounds());
        assertEquals(new Rectangle(0, 70, 328, 230), c.grid.getBounds());
        assertEquals("4,2 52,0 88,0", c.today.getX() + "," + c.today.getY() + " " + c.prev.getX() + "," + c.prev.getY() + " "
                + c.next.getX() + "," + c.next.getY());
    }

    @Test
    void eHead2Pictures() throws IOException {
        for (DayOfWeek start : new DayOfWeek[] {DayOfWeek.MONDAY, DayOfWeek.SUNDAY}) {
            for (int[] s : new int[][] {{328, 300}, {560, 420}, {200, 200}, {0, 0}}) {
                looksPanel(start.name().substring(0, 3).toLowerCase() + "-2026-11", panel("2026-11-16T10:00", start), s[0], s[1]);
            }
        }
    }

    @Test
    void eHead4TodayButtonDim() {
        for (String now : new String[] {"2026-11-16T10:00", "2026-11-17T01:00"}) {
            CalendarPanel c = panel(now, DayOfWeek.MONDAY);
            assertFalse(c.today.isEnabled());
            assertEquals("오늘을 보고 있어요", c.today.getToolTipText());
            assertEquals("2026년 11월", c.title.getText());
        }
    }

    @Test
    void eHead5Names() {
        CalendarPanel c = panel("2026-11-16T10:00", DayOfWeek.MONDAY);
        assertEquals("이전 달 이전 달 다음 달 다음 달 오늘", c.prev.getToolTipText() + " " + c.prev.getAccessibleContext().getAccessibleName()
                + " " + c.next.getToolTipText() + " " + c.next.getAccessibleContext().getAccessibleName() + " "
                + c.today.getAccessibleContext().getAccessibleName());
        assertEquals(Cursor.HAND_CURSOR, c.prev.getCursor().getType());
        assertEquals(1, c.today.getActionListeners().length);
    }

    /** 꺾쇠를 차례로 누를 때마다 달 제목과 칸 수를 모으고 끝에 고른 날을 붙인다. */
    private static String moves(CalendarPanel c, CircleButton... presses) {
        StringBuilder b = new StringBuilder();
        for (CircleButton p : presses) {
            p.doClick();
            b.append(c.title.getText()).append(' ').append(c.grid.getComponentCount()).append(", ");
        }
        return b + "고른 날 " + c.selected();
    }

    @Test
    void eHead3MoveAcrossYears() throws IOException {
        CalendarPanel c = panel("2026-11-16T10:00", DayOfWeek.MONDAY);
        assertEquals("2026년 12월 35, 2026년 11월 42, 2026년 10월 35, 고른 날 2026-11-16", moves(c, c.next, c.prev, c.prev));
        c = panel("2026-12-20T10:00", DayOfWeek.MONDAY);
        assertEquals("2027년 1월 35, 2027년 2월 28, 고른 날 2026-12-20", moves(c, c.next, c.next));
        c = panel("2026-01-10T10:00", DayOfWeek.MONDAY);
        assertEquals("2025년 12월 35, 고른 날 2026-01-10", moves(c, c.prev));
        c = panel("2026-11-16T10:00", DayOfWeek.MONDAY);
        c.next.doClick();
        assertTrue(c.today.isEnabled());
        assertNull(c.today.getToolTipText());
        for (int[] s : new int[][] {{328, 300}, {560, 420}, {200, 200}, {0, 0}}) {
            looksPanel("next-2026-12", c, s[0], s[1]);
        }
        c.prev.doClick();
        assertFalse(c.today.isEnabled());
        assertEquals("오늘을 보고 있어요", c.today.getToolTipText());
    }

    private static LocalDate d(int month, int day) {
        return LocalDate.of(2026, month, day);
    }

    private static void press(Component c) {
        for (MouseListener l : c.getMouseListeners()) {
            l.mouseClicked(new MouseEvent(c, MouseEvent.MOUSE_CLICKED, 0, 0, 5, 5, 1, false));
        }
    }

    /** S3h 2절의 일정(16일 "오늘", 18일 "가", "나")으로 패널을 만들고 18일 칸을 누른다. 다시 그리기는 알림 대신 refresh다. */
    private CalendarPanel picked18() {
        task(Kind.TASK, "오늘", 16, null, 0, 0, 0);
        task(Kind.TASK, "가", 18, null, 0, 0, 0);
        task(Kind.TASK, "나", 18, null, 0, 0, 0);
        CalendarPanel c = panel("2026-11-16T10:00", DayOfWeek.MONDAY);
        press(cell(c, 18));
        c.refresh();
        return c;
    }

    @Test
    void eSel1PressPicksTheDay() {
        CalendarPanel c = picked18();
        assertEquals(d(11, 18), c.selected());
        assertEquals("<html><center><b>18</b><br><font size='2' color='#28977F'>2개</font></center></html>", cell(c, 18).getText());
        assertEquals("<html><center>16<br><font size='2' color='#28977F'>1개</font></center></html>", cell(c, 16).getText());
        assertTrue(c.today.isEnabled());
        assertNull(c.today.getToolTipText());
        press(cell(c, 18));
        assertEquals(List.of(d(11, 18), d(11, 18)), picks);
    }

    @Test
    void eSel2PressToday() {
        CalendarPanel c = picked18();
        press(cell(c, 16));
        c.refresh();
        assertEquals(d(11, 16), c.selected());
        assertFalse(c.today.isEnabled());
        assertEquals("오늘을 보고 있어요", c.today.getToolTipText());
        assertEquals(List.of(d(11, 18), d(11, 16)), picks);
    }

    @Test
    void eSel3PressInOtherMonth() {
        CalendarPanel c = picked18();
        c.next.doClick();
        assertEquals(List.of(d(11, 18)), picks);
        press(cell(c, 5));
        c.refresh();
        assertEquals("2026년 12월 2026-12-05", c.title.getText() + " " + c.selected());
        assertEquals("<html><center><b>5</b></center></html>", cell(c, 5).getText());
        assertEquals(List.of(d(11, 18), d(12, 5)), picks);
    }

    @Test
    void eSel5EmptySlotDoesNothing() {
        CalendarPanel c = panel("2026-11-16T10:00", DayOfWeek.MONDAY);
        Component empty = c.grid.getComponent(0);
        press(empty);
        assertEquals(0, empty.getMouseListeners().length);
        assertEquals(d(11, 16), c.selected());
        assertEquals(List.of(), picks);
    }

    @Test
    void eSel4TodayButton() {
        CalendarPanel c = picked18();
        c.next.doClick();
        press(cell(c, 5));
        c.today.doClick();
        c.refresh();
        assertEquals("2026년 11월 42 2026-11-16", c.title.getText() + " " + c.grid.getComponentCount() + " " + c.selected());
        assertEquals("<html><center><b>16</b><br><font size='2' color='#28977F'>1개</font></center></html>", cell(c, 16).getText());
        assertEquals("<html><center>5</center></html>", cell(c, 5).getText());
        assertFalse(c.today.isEnabled());
        assertEquals("오늘을 보고 있어요", c.today.getToolTipText());
        c.today.doClick();
        assertEquals(List.of(d(11, 18), d(12, 5), d(11, 16)), picks);
        press(cell(c, 20));
        c.refresh();
        c.today.doClick();
        c.refresh();
        assertEquals(d(11, 16), c.selected());
        assertFalse(c.today.isEnabled());
        assertEquals(List.of(d(11, 18), d(12, 5), d(11, 16), d(11, 20), d(11, 16)), picks);
    }

    private AppController app;
    private int heard;

    /** 잇는 생성자로 만든 패널(S3h). 고른 고양이 나비가 있고 저장은 늘 된다. */
    private CalendarPanel wired(String now) {
        data = new AppData(Clock.fixed(LocalDateTime.parse(now).atZone(ZONE).toInstant(), ZONE), tasks, List.of());
        data.addPet(new PetState(PetKind.CAT, "나비"));
        data.setActiveKind(PetKind.CAT);
        app = new AppController(data, new DataStore() {
            public LoadResult load() { return LoadResult.firstRun(); }
            public void save(AppData d) { }
        }, e -> { });
        CalendarPanel c = new CalendarPanel(data, new HomeController(data, app), app);
        data.onChange(() -> heard++);
        c.refresh();
        return c;
    }

    private String said() {
        return app.getBubble().getLine() + " 세는 날 " + app.getBubble().getRecountDay() + " 알림 " + heard;
    }

    @Test
    void eSel1to4TalkAndRedraw() {
        task(Kind.TASK, "오늘", 16, null, 0, 0, 0);
        task(Kind.TASK, "가", 18, null, 0, 0, 0);
        task(Kind.TASK, "나", 18, null, 0, 0, 0);
        CalendarPanel c = wired("2026-11-16T10:00");
        press(cell(c, 18));
        assertEquals("11월 18일 할 일은 2개야. 세는 날 2026-11-18 알림 1", said());
        assertEquals("<html><center><b>18</b><br><font size='2' color='#28977F'>2개</font></center></html>", cell(c, 18).getText());
        press(cell(c, 18));
        assertEquals("11월 18일 할 일은 2개야. 세는 날 2026-11-18 알림 2", said());
        press(cell(c, 16));
        assertEquals("오늘 할 일 1개 남았어. 세는 날 2026-11-16 알림 3", said());
        assertFalse(c.today.isEnabled());
        c.next.doClick();
        press(cell(c, 5));
        assertEquals("12월 5일은 할 일이 없어. 세는 날 2026-12-05 알림 4", said());
        c.today.doClick();
        c.today.doClick();
        assertEquals("오늘 할 일 1개 남았어. 세는 날 2026-11-16 알림 5 2026년 11월", said() + " " + c.title.getText());
    }

    @Test
    void eSel6RedrawOnNotice() {
        CalendarPanel c = wired("2026-11-16T10:00");
        app.change("일정 추가", () -> data.getBook().add(new TaskDraft(Kind.TASK, "다", Category.OTHER, d(11, 19), null, null, 30, "",
                null, null, false, null, null, false)));
        assertEquals("<html><center>19<br><font size='2' color='#28977F'>1개</font></center></html>", cell(c, 19).getText());
        assertEquals(1, heard);
    }

    @Test
    void eSel7PickedDayGoesToApp() {
        task(Kind.TASK, "오늘", 16, null, 0, 0, 0);
        task(Kind.TASK, "가", 18, null, 0, 0, 0);
        task(Kind.TASK, "나", 18, null, 0, 0, 0);
        CalendarPanel c = wired("2026-11-16T10:00");
        press(cell(c, 18));
        new RewardController(data, app).complete(data.getBook().getTasks().get(0));
        assertEquals("오늘 할 일을 다 끝냈어! 푹 쉬어. 세는 날 null 알림 2", said());
        app.change("일정 삭제", () -> data.getBook().delete(data.getBook().getTasks().get(2), DeleteScope.ONLY_THIS));
        assertEquals("11월 18일 할 일은 1개야. 세는 날 2026-11-18 알림 3", said());
    }
}
