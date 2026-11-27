package planningpet.common.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JToggleButton;
import org.junit.jupiter.api.Test;

/** 디자인 시스템 5절의 버튼. 모양은 빈 글 단추를 160x40으로 그려 프로토타입 그림과 맞춘다. 번호는 docs/porting/theme.md 2절. */
class RoundButtonUITest {
    /** 프로토타입은 Theme.install 뒤에 만든다. install은 #397이라 UI와 단추 글꼴을 따로 건다. */
    private static <B extends AbstractButton> B round(B b) {
        b.setUI(new RoundButtonUI());
        b.setFont(Theme.bold(14));
        return b;
    }

    private static JButton button(Object... marks) {
        JButton b = round(new JButton(""));
        for (Object mark : marks) {
            b.putClientProperty(mark, true);
        }
        return b;
    }

    private static void press(AbstractButton b) {
        b.getModel().setArmed(true);
        b.getModel().setPressed(true);
    }

    private static void looks(String name, AbstractButton b) throws IOException {
        Snap.assertLooksLikePrototype("btn-" + name, b, 160, 40);
    }

    /** 단추가 글을 그릴 때 쓰는 색. */
    private static Color ink(AbstractButton b) {
        Graphics2D g = new BufferedImage(160, 40, BufferedImage.TYPE_INT_ARGB).createGraphics();
        ((RoundButtonUI) b.getUI()).paintText(g, b, new Rectangle(0, 0, 160, 40), "저장");
        g.dispose();
        return g.getColor();
    }

    @Test
    void eBtn1Plain() throws IOException {
        JButton b = round(new JButton("저장"));
        assertEquals(new Insets(9, 12, 9, 12), b.getInsets());
        assertFalse(b.isOpaque());
        assertEquals("48x36", b.getPreferredSize().width + "x" + b.getPreferredSize().height);
        assertEquals(Theme.INK, ink(b));
        looks("plain", button());
    }

    @Test
    void eBtn4MouseOver() throws IOException {
        JButton b = button();
        b.getModel().setRollover(true);
        looks("over", b);
    }

    @Test
    void eBtn5Pressed() throws IOException {
        JButton b = button();
        press(b);
        looks("pressed", b);
        b = button();
        b.getModel().setRollover(true);
        press(b);
        looks("over-pressed", b);
    }

    @Test
    void eBtn6PressedAndDraggedOut() throws IOException {
        JButton b = button();
        b.getModel().setPressed(true);
        looks("pressed-not-armed", b);
    }

    @Test
    void eBtn7Disabled() throws IOException {
        JButton b = button();
        b.setEnabled(false);
        b.getModel().setRollover(true);
        looks("disabled", b);
        assertEquals(Theme.INK_DIM, ink(b));
    }

    @Test
    void eBtn8Primary() throws IOException {
        JButton b = button(RoundButtonUI.PRIMARY);
        looks("primary", b);
        assertEquals(Color.WHITE, ink(b));
    }

    @Test
    void eBtn9PrimaryOverAndPressed() throws IOException {
        JButton b = button(RoundButtonUI.PRIMARY);
        b.getModel().setRollover(true);
        looks("primary-over", b);
        b = button(RoundButtonUI.PRIMARY);
        press(b);
        looks("primary-pressed", b);
    }

    @Test
    void eBtn10PrimaryDisabled() throws IOException {
        JButton b = button(RoundButtonUI.PRIMARY);
        b.setEnabled(false);
        looks("primary-disabled", b);
        assertEquals(Theme.INK_DIM, ink(b));
    }

    @Test
    void eBtn11Toggle() throws IOException {
        JToggleButton on = round(new JToggleButton("", true));
        JToggleButton off = round(new JToggleButton("", false));
        looks("toggle-on", on);
        looks("toggle-off", off);
        assertEquals(List.of(Color.WHITE, Theme.INK), List.of(ink(on), ink(off)));
    }
}
