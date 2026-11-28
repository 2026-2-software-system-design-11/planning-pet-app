package planningpet.schedule.view;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.IllegalComponentStateException;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.junit.jupiter.api.Test;
import planningpet.app.controller.AppController;
import planningpet.app.model.AppData;
import planningpet.app.model.DataStore;
import planningpet.app.model.LoadResult;
import planningpet.common.view.CircleButton;
import planningpet.common.view.Icons;
import planningpet.common.view.Snap;
import planningpet.common.view.Theme;
import planningpet.pet.model.PetKind;
import planningpet.pet.model.PetState;
import planningpet.reward.controller.RewardController;
import planningpet.schedule.model.Category;
import planningpet.schedule.model.Kind;
import planningpet.schedule.model.Task;

/** 홈 목록의 할 일 한 줄. 번호는 docs/porting/task-rows.md S3v 2절. 기준 그림 proto/row-*.png는 글을 비우고 제목 칸을 숨긴 줄이다. */
class TaskRowTest {
    private static final LocalDate TODAY = LocalDate.of(2026, 11, 18);
    /** 앱 데이터의 일정은 끝내지 않은 "토익 공부"와 "보고서"다. */
    private final AppData data = new AppData(Clock.fixed(TODAY.atTime(10, 0).atZone(ZoneId.systemDefault()).toInstant(),
            ZoneId.systemDefault()), List.of(task(Kind.TASK, "토익 공부", null, 30, false),
            task(Kind.TASK, "보고서", LocalTime.of(10, 0), 60, false)), List.of());
    private final AppController app = new AppController(data, new DataStore() {
        public LoadResult load() { return LoadResult.firstRun(); }
        public void save(AppData d) { }
    }, e -> { });
    /** 수정, 삭제, 시각 함수가 받은 것과 시각 함수가 받은 알약. */
    private final List<String> heard = new ArrayList<>();
    private final List<Component> anchors = new ArrayList<>();
    private final TaskRows rows = new TaskRows(data, new RewardController(data, app), t -> heard.add("수정 " + t.getTitle()),
            t -> heard.add("삭제 " + t.getTitle()), (t, day, at) -> {
                heard.add("시각 " + t.getTitle() + " " + day);
                anchors.add(at);
            });

    TaskRowTest() {
        data.addPet(new PetState(PetKind.CAT, "나비"));
        data.setActiveKind(PetKind.CAT);
    }

    private static Task task(Kind kind, String title, LocalTime start, int minutes, boolean done) {
        return new Task(title, 1, kind, title, TODAY, start, minutes, "", false, Category.OTHER, null, null, null, null, done,
                done ? TODAY : null, 0, 0, null, null);
    }

    /** 11-16부터 11-22까지의 시각 없는 기간 할 일 "지원서" 60분. */
    private static Task period(boolean done) {
        return new Task("p", 2, Kind.TASK, "지원서", LocalDate.of(2026, 11, 16), null, 60, "", false, Category.OTHER, null, null,
                LocalDate.of(2026, 11, 22), LocalDate.of(2026, 11, 16), done, done ? TODAY : null, 0, 0, null, null);
    }

    private static TimePill pill(JPanel row) {
        return (TimePill) ((JPanel) row.getComponent(2)).getComponent(0);
    }

    /** 줄을 만들어 앱처럼 Theme.clear를 건 뒤 폭 w, 알맞은 높이로 배치한다. */
    private JPanel row(Task t, String time, LocalDate list, int w) {
        JPanel p = rows.row(t, time, list);
        Theme.clear(p);
        Snap.layOut(p, w, p.getPreferredSize().height);
        return p;
    }

    /** 자식의 종류와 자리. 칸(JPanel) 안의 자식은 [ ] 안에 잇는다. */
    private static String bounds(Container p) {
        List<String> parts = new ArrayList<>();
        for (Component c : p.getComponents()) {
            Class<?> k = c.getClass().isAnonymousClass() ? c.getClass().getSuperclass() : c.getClass();
            parts.add(k.getSimpleName() + " " + c.getX() + "," + c.getY() + " " + c.getWidth() + "x" + c.getHeight()
                    + (c instanceof JPanel in ? " [" + bounds(in) + "]" : ""));
        }
        return String.join(", ", parts);
    }

    @Test
    void eRow1MiddleCells() {
        JPanel left = TaskRows.middle(RoundCheck.style(new JCheckBox()));
        Snap.layOut(left, 24, 28);
        assertEquals("JCheckBox 0,2 24x24", bounds(left));
        assertTrue(left.isOpaque());
        JLabel minutes = new JLabel("30분");
        minutes.setPreferredSize(new Dimension(34, 20));
        JPanel east = TaskRows.middle(new TimePill("", false, false, null), minutes,
                new CircleButton(Icons.Kind.PENCIL, 28, "수정", true), new CircleButton(Icons.Kind.TRASH, 28, "삭제", true));
        assertEquals(new Dimension(217, 28), east.getPreferredSize());
        Snap.layOut(east, 217, 28);
        assertEquals("TimePill 0,0 115x28, JLabel 119,4 34x20, CircleButton 157,0 28x28, CircleButton 189,0 28x28", bounds(east));
    }

    /** 글을 비우고 제목 칸을 숨겨 그린 그림을 기준 그림과 견준다. */
    private static void looks(String name, JPanel p) throws IOException {
        Snap.assertLooksLikePrototype("row-" + name, blankPaint(p));
    }

    private static BufferedImage blankPaint(JPanel p) {
        Snap.blank(p);
        p.getComponent(1).setVisible(false);
        BufferedImage img = new BufferedImage(p.getWidth() + 4, p.getHeight() + 4, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        p.paint(g);
        g.dispose();
        return img;
    }

    private static int[] pixels(BufferedImage img) {
        return img.getRGB(0, 0, img.getWidth(), img.getHeight(), null, 0, img.getWidth());
    }

    private static void mouse(Component c, int id) {
        c.dispatchEvent(new MouseEvent(c, id, 0, 0, 1, 1, 0, false));
    }

    /** 줄에 들어가면 기준 그림 row-{name}-over와 같고, 나가면 들어가기 전과 픽셀이 같다. */
    private void overAndBack(String name, Task t, String time) throws IOException {
        JPanel p = row(t, time, TODAY, 480);
        int[] before = pixels(blankPaint(p));
        mouse(p, MouseEvent.MOUSE_ENTERED);
        looks(name + "-over", p);
        mouse(p, MouseEvent.MOUSE_EXITED);
        assertArrayEquals(before, pixels(blankPaint(p)));
    }

    @Test
    void eRow1Open() throws IOException {
        JPanel p = row(task(Kind.TASK, "토익 공부", null, 30, false), "", TODAY, 480);
        assertEquals(new Dimension(303, 39), p.getPreferredSize());
        assertEquals(44, p.getMaximumSize().height);
        assertEquals(new Insets(5, 6, 6, 4), p.getInsets());
        assertEquals(0f, p.getAlignmentX());
        assertFalse(p.isOpaque());
        assertEquals("JPanel 6,5 24x28 [JCheckBox 0,2 24x24], TitleLabel 36,5 217x28, JPanel 259,5 217x28 [TimePill 0,0 115x28, "
                + "JLabel 119,4 34x20, CircleButton 157,0 28x28, CircleButton 189,0 28x28]", bounds(p));
        looks("open", p);
    }

    @Test
    void eRow5Widths() throws IOException {
        Task t = task(Kind.TASK, "토익 공부", null, 30, false);
        int[][] want = {{200, -63, -21}, {0, -263, -221}, {900, 637, 679}};
        for (int[] w : want) {
            JPanel p = row(t, "", TODAY, w[0]);
            assertEquals(w[1], p.getComponent(1).getWidth());
            assertEquals(w[2], p.getComponent(2).getX());
        }
        looks("narrow", row(t, "", TODAY, 200));
    }

    @Test
    void eRow2Done() throws IOException {
        JPanel p = row(task(Kind.TASK, "빨래", LocalTime.of(10, 0), 60, true), "10:00~11:00", TODAY, 480);
        assertTrue(((AbstractButton) ((JPanel) p.getComponent(0)).getComponent(0)).isSelected());
        JPanel east = (JPanel) p.getComponent(2);
        TimePill pill = (TimePill) east.getComponent(0);
        assertEquals("10:00~11:00", pill.getText());
        assertFalse(pill.isEnabled());
        assertNull(pill.getToolTipText());
        assertEquals(Theme.INK_DIM, east.getComponent(1).getForeground());
        assertEquals(new Dimension(303, 39), p.getPreferredSize());
        looks("done", p);
    }

    @Test
    void eRow6Minutes() {
        int[][] want = {{5, 34, 217}, {100, 34, 217}, {300, 35, 216}, {1440, 41, 210}};
        for (int[] w : want) {
            JPanel p = row(task(Kind.TASK, "분", null, w[0], false), "", TODAY, 480);
            JLabel m = (JLabel) ((JPanel) p.getComponent(2)).getComponent(1);
            assertEquals(w[0] + "분", m.getText());
            assertEquals(Theme.font(13), m.getFont());
            assertEquals(JLabel.RIGHT, m.getHorizontalAlignment());
            assertEquals(Theme.INK_SUB, m.getForeground());
            assertEquals(new Dimension(w[1], 20), m.getPreferredSize());
            assertEquals(w[2], p.getComponent(1).getWidth());
        }
    }

    @Test
    void eRow7Check() {
        Task t = data.getBook().getTasks().get(0);
        AbstractButton box = (AbstractButton) ((JPanel) row(t, "", TODAY, 480).getComponent(0)).getComponent(0);
        assertEquals("끝냄: 토익 공부", box.getAccessibleContext().getAccessibleName());
        assertSame(t, box.getClientProperty(Task.class));
        box.doClick();
        assertTrue(t.isDone());
        assertEquals(10, data.getPoints());
        assertEquals("잘했어! +10P", app.getBubble().getLine());
        box = (AbstractButton) ((JPanel) row(t, "", TODAY, 480).getComponent(0)).getComponent(0);
        assertEquals("끝냄 취소: 토익 공부", box.getAccessibleContext().getAccessibleName());
        box.doClick();
        assertFalse(t.isDone());
        assertEquals(0, data.getPoints());
        assertEquals("체크를 풀었어. -10P", app.getBubble().getLine());
    }

    @Test
    void eRow8EditAndDelete() {
        JPanel east = (JPanel) row(task(Kind.TASK, "보고서", LocalTime.of(10, 0), 60, false), "10:00~11:00", TODAY, 480).getComponent(2);
        for (int i = 2; i < 4; i++) {
            CircleButton b = (CircleButton) east.getComponent(i);
            assertEquals(new Dimension(28, 28), b.getPreferredSize());
            assertEquals(i == 2 ? "수정" : "삭제", b.getToolTipText());
            assertEquals(b.getToolTipText(), b.getAccessibleContext().getAccessibleName());
            b.doClick();
        }
        assertEquals(List.of("수정 보고서", "삭제 보고서"), heard);
    }

    @Test
    void eRow9PillOpens() {
        TimePill timed = pill(row(task(Kind.TASK, "보고서", LocalTime.of(10, 0), 60, false), "10:00~11:00", TODAY, 480));
        assertEquals("10:00~11:00", timed.getText());
        assertEquals("눌러서 시각을 바꿔요", timed.getToolTipText());
        timed.doClick();
        TimePill empty = pill(row(period(false), "", LocalDate.of(2026, 11, 20), 480));
        assertEquals("시각 정하기", empty.getText());
        empty.doClick();
        assertEquals(List.of("시각 보고서 2026-11-18", "시각 지원서 2026-11-20"), heard);
        assertEquals(List.of(timed, empty), anchors);
    }

    @Test
    void eRow10PillLock() {
        LocalDate past = LocalDate.of(2026, 11, 17);
        String why = "지난 날에는 시각을 정할 수 없어요";
        String open = "눌러서 시각을 바꿔요";
        Object[][] cases = {{task(Kind.TASK, "빨래", null, 30, true), TODAY, null}, {period(false), past, why}, {period(true), past, null},
            {period(false), TODAY, open}, {period(false), LocalDate.of(2026, 11, 20), open}, {task(Kind.TASK, "빨래", null, 30, false), past, open}};
        for (Object[] c : cases) {
            TimePill p = pill(row((Task) c[0], "", (LocalDate) c[1], 480));
            assertEquals(open.equals(c[2]), p.isEnabled());
            assertEquals(c[2], p.getToolTipText());
            p.doClick();
        }
        assertEquals(List.of("시각 지원서 2026-11-18", "시각 지원서 2026-11-20", "시각 빨래 2026-11-17"), heard);
    }

    @Test
    void eRow3Marks() throws IOException {
        JPanel p = row(period(false), "", LocalDate.of(2026, 11, 17), 480);
        assertEquals(new Dimension(303, 54), p.getPreferredSize());
        assertEquals(62, p.getMaximumSize().height);
        JLabel marks = (JLabel) p.getComponent(3);
        assertEquals("<html><font color='#8A96A0'>~11/22까지</font></html>", marks.getText());
        assertEquals("6,33 470x15", marks.getX() + "," + marks.getY() + " " + marks.getWidth() + "x" + marks.getHeight());
        looks("period-past", p);
    }

    @Test
    void eRow4Fixed() throws IOException {
        JPanel p = row(task(Kind.FIXED, "수업", LocalTime.of(9, 0), 75, false), "09:00~10:15", TODAY, 480);
        assertEquals(new Dimension(341, 39), p.getPreferredSize());
        assertEquals("JPanel 6,5 62x28 [JCheckBox 0,2 24x24, JLabel 28,5 34x18], TitleLabel 74,5 179x28, JPanel 259,5 217x28 "
                + "[TimePill 0,0 115x28, JLabel 119,4 34x20, CircleButton 157,0 28x28, CircleButton 189,0 28x28]", bounds(p));
        assertEquals("고정", ((JLabel) ((JPanel) p.getComponent(0)).getComponent(1)).getText());
        looks("fixed", p);
    }

    @Test
    void eHover1Open() throws IOException {
        overAndBack("open", task(Kind.TASK, "토익 공부", null, 30, false), "");
    }

    @Test
    void eHover2Done() throws IOException {
        overAndBack("done", task(Kind.TASK, "빨래", LocalTime.of(10, 0), 60, true), "10:00~11:00");
    }

    @Test
    void eHover3Marks() throws IOException {
        overAndBack("period", period(false), "");
    }

    /** 글 그대로 그린 줄의 왼쪽 끝 가운데(3, 높이의 반) 픽셀. 바탕이 없으면 0이다. */
    private static int edge(JPanel p) {
        BufferedImage img = new BufferedImage(p.getWidth() + 4, p.getHeight() + 4, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        p.paint(g);
        g.dispose();
        return img.getRGB(3, p.getHeight() / 2);
    }

    @Test
    void eHover4Children() {
        JPanel p = row(task(Kind.TASK, "토익 공부", null, 30, false), "", TODAY, 480);
        JPanel left = (JPanel) p.getComponent(0);
        JPanel east = (JPanel) p.getComponent(2);
        Component[] kids = {p.getComponent(1), left.getComponent(0), east.getComponent(0), east.getComponent(2), east.getComponent(3),
            east.getComponent(1), left, east};
        for (int i = 0; i < kids.length; i++) {
            mouse(kids[i], MouseEvent.MOUSE_ENTERED);
            assertEquals(i < 5 ? Theme.DAY.getRGB() : 0, edge(p), "부품 " + i);
            mouse(kids[i], MouseEvent.MOUSE_EXITED);
            assertEquals(0, edge(p));
        }
        mouse(p, MouseEvent.MOUSE_ENTERED);
        mouse(p, MouseEvent.MOUSE_EXITED);
        mouse(kids[0], MouseEvent.MOUSE_ENTERED);
        assertEquals(Theme.DAY.getRGB(), edge(p));
    }

    private static final String DIV = "<html><div style='width:280px'>";
    private static final String END = "</div></html>";

    /** 메모 m을 든 할 일의 memoTip이 want이고, 그 줄과 제목 칸에 같은 툴팁이 걸린다. */
    private void tip(String m, String want) {
        Task t = new Task("m", 1, Kind.TASK, "메모 일", TODAY, null, 30, m, false, Category.OTHER, null, null, null, null, false, null, 0,
                0, null, null);
        assertEquals(want, TaskRows.memoTip(t));
        JPanel p = row(t, "", TODAY, 480);
        assertEquals(want, p.getToolTipText());
        assertEquals(want, ((JLabel) p.getComponent(1)).getToolTipText());
    }

    @Test
    void eMemo1None() {
        tip("", null);
        tip("   ", null);
    }

    @Test
    void eMemo2Short() {
        tip("단어 50개", DIV + "단어 50개" + END);
        tip("첫 줄\n둘째 <b>A&B</b>", DIV + "첫 줄<br>둘째 &lt;b&gt;A&amp;B&lt;/b&gt;" + END);
    }

    @Test
    void eMemo3Limit() {
        tip("가".repeat(200), DIV + "가".repeat(200) + END);
        tip("가".repeat(201), DIV + "가".repeat(200) + "…" + END);
    }

    @Test
    void eMemo4Emoji() {
        String e = "\uD83D\uDE00";
        tip("가".repeat(199) + e + "나", DIV + "가".repeat(199) + e + "…" + END);
        tip(e.repeat(100), DIV + e.repeat(100) + END);
        tip(e.repeat(101), DIV + e.repeat(101) + END);
        tip(e.repeat(201), DIV + e.repeat(200) + "…" + END);
    }

    /** 부품에 단추(1, 2, 3), 누름 수, 팝업 깃발의 마우스 사건을 보낸다. */
    private static void press(Component c, int id, int button, int count, boolean popup) {
        c.dispatchEvent(new MouseEvent(c, id, 0, 0, 10, 12, count, popup, button));
    }

    /** 줄 안의 왼쪽 칸, 체크, 오른쪽 칸, 알약, 걸리는 시간, 연필, 휴지통. */
    private static List<Component> inner(JPanel p) {
        JPanel left = (JPanel) p.getComponent(0);
        JPanel east = (JPanel) p.getComponent(2);
        return List.of(left, left.getComponent(0), east, east.getComponent(0), east.getComponent(1), east.getComponent(2),
                east.getComponent(3));
    }

    /** 창 없는 줄은 화면에 보이지 않아 메뉴를 띄우려 하면 show가 예외를 던진다(task-rows.md S3y). */
    @Test
    void ePop1RightButton() {
        JPanel p = row(task(Kind.TASK, "보고서", LocalTime.of(10, 0), 60, false), "10:00~11:00", TODAY, 480);
        int[][] events = {{MouseEvent.MOUSE_PRESSED, 3}, {MouseEvent.MOUSE_RELEASED, 3}, {MouseEvent.MOUSE_PRESSED, 1}};
        for (Component c : List.of(p, p.getComponent(1))) {
            for (int[] e : events) {
                IllegalComponentStateException x = assertThrows(IllegalComponentStateException.class, () -> press(c, e[0], e[1], 1, true));
                assertEquals("component must be showing on the screen to determine its location", x.getMessage());
            }
        }
        assertEquals(List.of(), heard);
    }

    @Test
    void ePop2NoMenu() {
        JPanel p = row(task(Kind.TASK, "보고서", LocalTime.of(10, 0), 60, false), "10:00~11:00", TODAY, 480);
        press(p, MouseEvent.MOUSE_PRESSED, 3, 1, false);
        press(p, MouseEvent.MOUSE_RELEASED, 3, 1, false);
        press(p, MouseEvent.MOUSE_CLICKED, 3, 1, true);
        for (Component c : inner(p)) {
            press(c, MouseEvent.MOUSE_PRESSED, 3, 1, true);
            press(c, MouseEvent.MOUSE_RELEASED, 3, 1, true);
        }
        assertEquals(List.of(), heard);
    }
}
