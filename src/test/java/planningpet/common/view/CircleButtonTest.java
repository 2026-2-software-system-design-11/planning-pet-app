package planningpet.common.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.DefaultKeyboardFocusManager;
import java.awt.Dimension;
import java.awt.KeyboardFocusManager;
import java.io.IOException;
import org.junit.jupiter.api.Test;

/** 원 아이콘 버튼. 번호는 docs/porting/home.md S3c 2절. 기준 그림은 proto/cbtn-*.png이고 글이 없다. */
class CircleButtonTest {
    private static CircleButton button(Icons.Kind kind, int size, boolean plain) {
        return new CircleButton(kind, size, "팁", plain);
    }

    private static void looks(String name, CircleButton b, int w, int h) throws IOException {
        Snap.assertLooksLikePrototype("cbtn-" + name, b, w, h);
    }

    @Test
    void eCbtn1WhiteButton() throws IOException {
        looks("left-32", button(Icons.Kind.LEFT, 32, false), 32, 32);
        looks("right-32", button(Icons.Kind.RIGHT, 32, false), 32, 32);
        looks("paw-40", button(Icons.Kind.PAW, 40, false), 40, 40);
    }

    @Test
    void eCbtn2OverAndDown() throws IOException {
        CircleButton b = button(Icons.Kind.RIGHT, 32, false);
        b.getModel().setRollover(true);
        looks("over", b, 32, 32);
        b.getModel().setArmed(true);
        b.getModel().setPressed(true);
        looks("down", b, 32, 32);
    }

    @Test
    void eCbtn3PressedOutside() throws IOException {
        CircleButton b = button(Icons.Kind.RIGHT, 32, false);
        b.getModel().setPressed(true);
        assertFalse(b.getModel().isArmed());
        looks("pressed-off", b, 32, 32);
        looks("right-32", b, 32, 32);
    }

    @Test
    void eCbtn4Disabled() throws IOException {
        CircleButton b = button(Icons.Kind.LEFT, 32, false);
        b.setEnabled(false);
        b.getModel().setRollover(true);
        assertFalse(b.getModel().isRollover());
        looks("disabled", b, 32, 32);
    }

    @Test
    void eCbtn7NotSquare() throws IOException {
        looks("60x32", button(Icons.Kind.LEFT, 32, false), 60, 32);
        looks("32x60", button(Icons.Kind.LEFT, 32, false), 32, 60);
        looks("size24-64x64", button(Icons.Kind.LEFT, 24, false), 64, 64);
    }

    @Test
    void eCbtn8VerySmall() throws IOException {
        looks("5x5", button(Icons.Kind.LEFT, 32, false), 5, 5);
        looks("4x4", button(Icons.Kind.LEFT, 32, false), 4, 4);
        looks("0x0", button(Icons.Kind.LEFT, 32, false), 0, 0);
    }

    @Test
    void eCbtn9Properties() {
        CircleButton b = button(Icons.Kind.LEFT, 32, false);
        assertEquals(new Dimension(32, 32), b.getPreferredSize());
        assertEquals("팁", b.getToolTipText());
        assertEquals("팁", b.getAccessibleContext().getAccessibleName());
        assertEquals(Cursor.HAND_CURSOR, b.getCursor().getType());
        assertFalse(b.isOpaque());
        assertFalse(b.isContentAreaFilled());
        assertFalse(b.isBorderPainted());
        assertFalse(b.isFocusPainted());
        assertTrue(b.isRolloverEnabled());
        assertEquals("", b.getText());
        assertNull(b.getIcon());
        CircleButton zero = new CircleButton(Icons.Kind.LEFT, 0, null, false);
        assertEquals(new Dimension(0, 0), zero.getPreferredSize());
        assertNull(zero.getToolTipText());
        assertEquals("", zero.getAccessibleContext().getAccessibleName());
    }

    /** 창 없이 초점을 주려고 setGlobalFocusOwner를 연다(RoundButtonUITest와 같다). 검사 동안만 건다. */
    private static final class Focus extends DefaultKeyboardFocusManager {
        void own(Component c) {
            setGlobalFocusOwner(c);
        }
    }

    @Test
    void eCbtn5KeyboardFocus() throws IOException {
        Focus focus = new Focus();
        KeyboardFocusManager.setCurrentKeyboardFocusManager(focus);
        try {
            CircleButton b = button(Icons.Kind.LEFT, 32, false);
            focus.own(b);
            looks("focus", b, 32, 32);
            b = button(Icons.Kind.LEFT, 32, false);
            b.setEnabled(false);
            focus.own(b);
            looks("focus-disabled", b, 32, 32);
            looks("disabled", b, 32, 32);
            b = button(Icons.Kind.RIGHT, 30, true);
            focus.own(b);
            looks("focus-plain", b, 30, 30);
        } finally {
            KeyboardFocusManager.setCurrentKeyboardFocusManager(null);
        }
    }

    @Test
    void eCbtn6Plain() throws IOException {
        CircleButton b = button(Icons.Kind.RIGHT, 30, true);
        looks("plain", b, 30, 30);
        b.getModel().setRollover(true);
        looks("plain-over", b, 30, 30);
        b.getModel().setArmed(true);
        b.getModel().setPressed(true);
        looks("plain-down", b, 30, 30);
        CircleButton off = button(Icons.Kind.LEFT, 30, true);
        off.setEnabled(false);
        looks("plain-disabled", off, 30, 30);
    }
}
