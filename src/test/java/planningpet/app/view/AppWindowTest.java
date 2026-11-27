package planningpet.app.view;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.IOException;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import planningpet.common.view.RoundButtonUI;
import planningpet.common.view.Snap;
import planningpet.common.view.Theme;

/**
 * 앱의 창 틀. 번호는 docs/porting/frame.md 2절. 기준 그림은 프로토타입 App을 Theme.install 뒤에 만들어 글을 모두 비우고
 * 그렸다. 창(JFrame)은 headless 검사에서 만들 수 없어 창 내용과 창 아이콘을 그려 맞춘다.
 */
class AppWindowTest {
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
    private static Component at(Component c, int... path) {
        for (int i : path) {
            c = ((Container) c).getComponent(i);
        }
        return c;
    }

    @Test
    void eWin1Bounds() {
        AppWindow w = new AppWindow();
        assertEquals(new Dimension(1000, 680), w.root().getPreferredSize());
        w.root().setSize(1000, 680);
        w.root().doLayout();
        assertEquals(new Rectangle(10, 10, 140, 660), at(w.root(), 0).getBounds());
        assertEquals(new Rectangle(162, 10, 828, 660), at(w.root(), 1).getBounds());
        assertEquals(new Dimension(0, 64), at(w.root(), 0, 0, 0).getPreferredSize());
    }

    @Test
    void eWin1UsualWindow() throws IOException {
        looksLikePrototype("frame-1000x680", new AppWindow(), 1000, 680);
    }

    @Test
    void eWin2LargerAndSmallerWindow() throws IOException {
        looksLikePrototype("frame-1366x768", new AppWindow(), 1366, 768);
        looksLikePrototype("frame-2560x1440", new AppWindow(), 2560, 1440);
        looksLikePrototype("frame-600x400", new AppWindow(), 600, 400);
    }

    @Test
    void eWin3ZeroSize() throws IOException {
        looksLikePrototype("frame-0x0", new AppWindow(), 0, 0);
    }

    @Test
    void eWin12LogoWordsAndAttendButton() {
        AppWindow w = new AppWindow();
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
