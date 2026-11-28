package planningpet.common.view;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.awt.Component;
import java.awt.DefaultKeyboardFocusManager;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.event.FocusEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import javax.swing.JComponent;
import javax.swing.RepaintManager;
import org.junit.jupiter.api.Test;

/**
 * 둥근 입력칸. 번호는 docs/porting/pick.md 2절. 기준 그림은 글이 없는 칸만 두고(예시 글 없음, 빈 글), 글이 든 그림은
 * 운영체제마다 픽셀이 달라 테스트에 두지 않는다.
 */
class RoundFieldTest {
    private static RoundField pencil() {
        return new RoundField(Icons.Kind.PENCIL, null);
    }

    private static void looks(String name, RoundField f, int w, int h) throws IOException {
        Snap.assertLooksLikePrototype("field-" + name, f, w, h);
    }

    /** text를 친 칸을 300x40으로 그린 픽셀. */
    private static int[] pixels(RoundField f, String text) {
        f.setText(text);
        BufferedImage img = Snap.paint(f, 300, 40);
        return img.getRGB(0, 0, img.getWidth(), img.getHeight(), null, 0, img.getWidth());
    }

    /** 창 없이 칸에 마우스가 들어오고 나가는 알림. */
    private static void mouse(RoundField f, int id) {
        f.dispatchEvent(new MouseEvent(f, id, 0, 0, 5, 5, 0, false));
    }

    /** 창 없이 초점을 주려고 setGlobalFocusOwner를 연다(RoundButtonUITest와 같다). 검사 동안만 건다. */
    private static final class Focus extends DefaultKeyboardFocusManager {
        void own(Component c) {
            setGlobalFocusOwner(c);
        }
    }

    /** 다시 그리라고 한 자리를 모은다. */
    private static final class Dirty extends RepaintManager {
        private Rectangle all;

        @Override
        public void addDirtyRegion(JComponent c, int x, int y, int w, int h) {
            Rectangle r = new Rectangle(x, y, w, h);
            all = all == null ? r : all.union(r);
        }

        Rectangle after(Runnable action) {
            all = null;
            action.run();
            return all;
        }
    }

    @Test
    void eRound1Plain() throws IOException {
        RoundField f = pencil();
        assertEquals(new Insets(8, 40, 8, 16), f.getInsets());
        looks("pencil", f, 300, 40);
    }

    @Test
    void eRound2NoIcon() throws IOException {
        RoundField f = new RoundField(null, null);
        assertEquals(new Insets(8, 18, 8, 16), f.getInsets());
        assertFalse(f.isOpaque());
        assertEquals(Theme.font(14), f.getFont());
        looks("none", f, 300, 40);
    }

    @Test
    void eRound3MouseOver() throws IOException {
        RoundField f = pencil();
        mouse(f, MouseEvent.MOUSE_ENTERED);
        looks("over", f, 300, 40);
        mouse(f, MouseEvent.MOUSE_EXITED);
        looks("pencil", f, 300, 40);
    }

    @Test
    void eRound4Focus() throws IOException {
        Focus focus = new Focus();
        KeyboardFocusManager.setCurrentKeyboardFocusManager(focus);
        try {
            RoundField f = pencil();
            focus.own(f);
            looks("focus", f, 300, 40);
            mouse(f, MouseEvent.MOUSE_ENTERED);
            looks("focus", f, 300, 40);
            f = new RoundField(null, null);
            focus.own(f);
            looks("focus-none", f, 300, 40);
            f = pencil();
            f.setEnabled(false);
            focus.own(f);
            looks("focus-disabled", f, 300, 40);
            RoundField p = new RoundField(Icons.Kind.PENCIL, "할 일을 쓰고 Enter");
            focus.own(p);
            int[] withPlaceholder = pixels(p, "");
            RoundField plain = pencil();
            focus.own(plain);
            assertFalse(Arrays.equals(pixels(plain, ""), withPlaceholder), "초점이 있어도 예시 글을 그린다");
        } finally {
            KeyboardFocusManager.setCurrentKeyboardFocusManager(null);
        }
    }

    @Test
    void eRound5Disabled() throws IOException {
        RoundField f = pencil();
        f.setEnabled(false);
        looks("disabled", f, 300, 40);
        mouse(f, MouseEvent.MOUSE_ENTERED);
        looks("disabled-over", f, 300, 40);
    }

    @Test
    void eRound6Sizes() throws IOException {
        looks("300x60", pencil(), 300, 60);
        looks("120x40", pencil(), 120, 40);
        looks("30x40", pencil(), 30, 40);
        looks("4x4", pencil(), 4, 4);
        looks("0x0", pencil(), 0, 0);
        assertEquals(new Dimension(57, 40), pencil().getPreferredSize());
        RoundField wide = new RoundField(null, null);
        wide.setColumns(12);
        assertEquals(new Dimension(178, 40), wide.getPreferredSize());
        RoundField big = pencil();
        big.setFont(Theme.font(30));
        assertEquals(new Dimension(57, 53), big.getPreferredSize());
    }

    @Test
    void eRound7Placeholder() {
        RoundField p = new RoundField(Icons.Kind.PENCIL, "할 일을 쓰고 Enter");
        assertFalse(Arrays.equals(pixels(pencil(), ""), pixels(p, "")), "빈 칸은 예시 글을 그린다");
        assertArrayEquals(pixels(pencil(), " "), pixels(p, " "), "공백 하나면 그리지 않는다");
        assertFalse(Arrays.equals(pixels(pencil(), ""), pixels(p, "")), "비우면 다시 그린다");
        assertArrayEquals(pixels(pencil(), ""), pixels(new RoundField(Icons.Kind.PENCIL, ""), ""));
    }

    @Test
    void eRound11Repaint() {
        Dirty d = new Dirty();
        RepaintManager.setCurrentManager(d);
        try {
            RoundField f = pencil();
            f.setSize(300, 40);
            Rectangle all = new Rectangle(0, 0, 300, 40);
            assertEquals(all, d.after(() -> mouse(f, MouseEvent.MOUSE_ENTERED)));
            assertEquals(all, d.after(() -> mouse(f, MouseEvent.MOUSE_EXITED)));
            FocusEvent gained = new FocusEvent(f, FocusEvent.FOCUS_GAINED);
            assertEquals(all, d.after(() -> Arrays.stream(f.getFocusListeners()).forEach(l -> l.focusGained(gained))));
            FocusEvent lost = new FocusEvent(f, FocusEvent.FOCUS_LOST);
            assertEquals(all, d.after(() -> Arrays.stream(f.getFocusListeners()).forEach(l -> l.focusLost(lost))));
            assertEquals(all, d.after(() -> f.replaceSelection("가")));
        } finally {
            RepaintManager.setCurrentManager(null);
        }
    }
}
