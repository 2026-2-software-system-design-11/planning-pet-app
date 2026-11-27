package planningpet.app.view;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import planningpet.app.controller.AppController;
import planningpet.app.model.AppData;
import planningpet.app.model.DataStore;
import planningpet.app.model.LoadResult;
import planningpet.common.view.RoundButtonUI;
import planningpet.common.view.Snap;
import planningpet.common.view.Theme;

/**
 * 앱의 창 틀. 번호는 docs/porting/frame.md 2절. 기준 그림은 프로토타입 App을 Theme.install 뒤에 만들어 글을 모두 비우고
 * 그렸다. 창(JFrame)은 headless 검사에서 만들 수 없어 창 내용과 창 아이콘을 그려 맞춘다.
 */
class AppWindowTest {
    private static final ZoneId ZONE = ZoneId.systemDefault();
    private LocalDateTime now = LocalDateTime.of(2026, 11, 16, 10, 0);
    private final AppData data = new AppData(new Clock() {
        public ZoneId getZone() { return ZONE; }
        public Clock withZone(ZoneId zone) { return this; }
        public Instant instant() { return now.atZone(ZONE).toInstant(); }
    });

    private final List<String> errors = new ArrayList<>();
    private boolean failing;
    private final AppController app = new AppController(data, new DataStore() {
        public LoadResult load() { return LoadResult.firstRun(); }
        public void save(AppData d) throws IOException {
            if (failing) {
                throw new IOException("디스크가 찼다");
            }
        }
    }, errors::add);

    private AppWindow window() {
        return new AppWindow(data, app);
    }

    private static List<Object> shown(AppWindow w) {
        return List.of(w.streak.getText(), w.points.getText(), w.attend.getText(), w.attend.isEnabled());
    }

    private static List<Object> shown(String streak, String points, String attend, boolean enabled) {
        return List.of("<html>연속 접속 <b>" + streak + "</b></html>", "<html>보유 <b>" + points + "</b></html>", attend, enabled);
    }
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

    /** 프로토타입 기준 그림처럼 Main.create의 clear를 걸고 모든 글을 비운 뒤 그린다. install은 검사마다 되돌린다. */
    private static void looksLikePrototype(String name, AppWindow w, int width, int height) throws IOException {
        Theme.clear(w.root());
        w.streak.set("", "");
        w.points.set("", "");
        blank(w.root());
        Snap.assertLaidOutLooksLikePrototype(name, w.root(), width, height);
    }

    private static void blank(Component c) {
        if (c instanceof JLabel l) {
            l.setText("");
        }
        if (c instanceof AbstractButton b) {
            b.setText("");
        }
        if (c instanceof Container k) {
            for (Component child : k.getComponents()) {
                blank(child);
            }
        }
    }
    @Test
    void eWin4BeforeFirstNotice() {
        assertEquals(List.of("", "", "", true), shown(window()));
    }

    @Test
    void eWin5ValueTexts() {
        AppWindow w = window();
        int[][] cases = {{0, 0}, {1, 1}, {365, 1234567}, {Integer.MAX_VALUE, Integer.MAX_VALUE}, {1, -10}, {1, Integer.MIN_VALUE}};
        String[][] want = {{"0일", "0P"}, {"1일", "1P"}, {"365일", "1,234,567P"}, {"2147483647일", "2,147,483,647P"},
            {"1일", "-10P"}, {"1일", "-2,147,483,648P"}};
        for (int i = 0; i < cases.length; i++) {
            data.setStreak(cases[i][0]);
            data.setPoints(cases[i][1]);
            data.changed();
            assertEquals(shown(want[i][0], want[i][1], "출석 +20P", true), shown(w));
        }
    }

    @Test
    void eWin6AttendanceDay() throws IOException {
        AppWindow w = window();
        data.setAttendanceDay(LocalDate.of(2026, 11, 15));
        data.changed();
        assertEquals(shown("0일", "0P", "출석 +20P", true), shown(w));
        data.setAttendanceDay(LocalDate.of(2026, 11, 17));
        data.changed();
        assertEquals(shown("0일", "0P", "출석 +20P", true), shown(w));
        data.setAttendanceDay(LocalDate.of(2026, 11, 16));
        data.changed();
        assertEquals(shown("0일", "0P", "출석 완료", false), shown(w));
        looksLikePrototype("frame-attended", w, 1000, 680);
    }

    @Test
    void eWin7PressAttend() {
        AppWindow w = window();
        int[] heard = {0};
        data.onChange(() -> heard[0]++);
        w.attend.doClick();
        assertEquals(List.of(20, LocalDate.of(2026, 11, 16), "출석했어! +20P", 1),
                List.of(data.getPoints(), data.getAttendanceDay(), app.getBubble().getLine(), heard[0]));
        assertEquals(shown("0일", "20P", "출석 완료", false), shown(w));
    }

    @Test
    void eWin10AttendNotSaved() {
        AppWindow w = window();
        data.setPoints(30);
        data.changed();
        failing = true;
        w.attend.doClick();
        assertEquals(List.of(30, List.of("출석 저장에 실패해서 바꾸기 전으로 돌렸어요.")), List.of(data.getPoints(), errors));
        assertEquals(null, data.getAttendanceDay());
        assertEquals(shown("0일", "30P", "출석 +20P", true), shown(w));
    }

    @Test
    void eWin8EndOfAppDay() {
        AppWindow w = window();
        data.setAttendanceDay(LocalDate.of(2026, 11, 16));
        now = LocalDateTime.of(2026, 11, 17, 3, 59);
        data.changed();
        assertEquals(shown("0일", "0P", "출석 완료", false), shown(w));
        now = LocalDateTime.of(2026, 11, 17, 4, 0);
        data.changed();
        assertEquals(shown("0일", "0P", "출석 +20P", true), shown(w));
    }

    @Test
    void eWin9RedrawWithoutNotice() {
        AppWindow w = window();
        int[] heard = {0};
        data.onChange(() -> heard[0]++);
        data.setStreak(3);
        w.redraw();
        assertEquals(List.of(shown("3일", "0P", "출석 +20P", true), 0), List.of(shown(w), heard[0]));
    }

    private static Component at(Component c, int... path) {
        for (int i : path) {
            c = ((Container) c).getComponent(i);
        }
        return c;
    }

    @Test
    void eWin1Bounds() {
        AppWindow w = window();
        assertEquals(new Dimension(1000, 680), w.root().getPreferredSize());
        w.root().setSize(1000, 680);
        w.root().doLayout();
        assertEquals(new Rectangle(10, 10, 140, 660), at(w.root(), 0).getBounds());
        assertEquals(new Rectangle(162, 10, 828, 660), at(w.root(), 1).getBounds());
        assertEquals(new Dimension(0, 64), at(w.root(), 0, 0, 0).getPreferredSize());
    }

    @Test
    void eWin1UsualWindow() throws IOException {
        looksLikePrototype("frame-1000x680", window(), 1000, 680);
    }

    @Test
    void eWin2LargerAndSmallerWindow() throws IOException {
        looksLikePrototype("frame-1366x768", window(), 1366, 768);
        looksLikePrototype("frame-2560x1440", window(), 2560, 1440);
        looksLikePrototype("frame-600x400", window(), 600, 400);
    }

    @Test
    void eWin3ZeroSize() throws IOException {
        looksLikePrototype("frame-0x0", window(), 0, 0);
    }

    @Test
    void eWin12LogoWordsAndAttendButton() {
        AppWindow w = window();
        JLabel name = (JLabel) at(w.root(), 0, 0, 1, 0);
        JLabel en = (JLabel) at(w.root(), 0, 0, 1, 1);
        assertEquals(List.of("플래닝펫", Theme.heavy(22), Theme.INK, SwingConstants.CENTER),
                List.of(name.getText(), name.getFont(), name.getForeground(), name.getHorizontalAlignment()));
        assertEquals(List.of("Planning Pet", Theme.font(11), Theme.INK_SUB, SwingConstants.CENTER),
                List.of(en.getText(), en.getFont(), en.getForeground(), en.getHorizontalAlignment()));
        assertEquals(List.of(Boolean.TRUE, Theme.bold(13), new java.awt.Insets(7, 14, 9, 14)),
                List.of(w.attend.getClientProperty(RoundButtonUI.PRIMARY), w.attend.getFont(), w.attend.getInsets()));
    }

    @Test
    void eWin11WindowIcon() throws IOException {
        Snap.assertLooksLikePrototype("frame-icon", AppWindow.icon());
    }
}
