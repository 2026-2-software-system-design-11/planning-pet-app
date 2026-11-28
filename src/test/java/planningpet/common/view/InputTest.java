package planningpet.common.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.RepaintManager;
import org.junit.jupiter.api.Test;

/** 공용 누르기, 초점, 다시 그리기 자리 모으기 Input(#520). 글 없는 부품으로 본다. */
class InputTest {
    /** 받은 마우스 사건을 "번호 x,y 횟수 단추 눌린 단추"로 적는 판. */
    private static JPanel heard(List<String> out) {
        JPanel p = new JPanel();
        p.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                out.add(say(e));
            }

            @Override
            public void mousePressed(MouseEvent e) {
                out.add(say(e));
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                out.add(say(e));
            }
        });
        return p;
    }

    private static String say(MouseEvent e) {
        return e.getID() + " " + e.getX() + "," + e.getY() + " " + e.getClickCount() + " " + e.getButton() + " "
                + (e.getModifiersEx() != 0);
    }

    @Test
    void mouseSendsOneEventAtFiveFive() {
        List<String> out = new ArrayList<>();
        JPanel p = heard(out);
        Input.mouse(p, MouseEvent.MOUSE_ENTERED);
        Input.mouse(p, MouseEvent.MOUSE_PRESSED);
        assertEquals(List.of(MouseEvent.MOUSE_ENTERED + " 5,5 0 0 false", MouseEvent.MOUSE_PRESSED + " 5,5 1 1 true"),
                out);
    }

    @Test
    void clickTellsEveryListener() {
        List<String> out = new ArrayList<>();
        JPanel p = heard(out);
        p.addMouseListener(heard(out).getMouseListeners()[0]);
        Input.click(p);
        assertEquals(List.of(MouseEvent.MOUSE_CLICKED + " 5,5 1 1 false", MouseEvent.MOUSE_CLICKED + " 5,5 1 1 false"),
                out);
    }

    @Test
    void focusOwnsWithoutWindowAndRestores() {
        JButton b = new JButton();
        Input.Focus f = Input.focus();
        try (f) {
            f.own(b);
            assertTrue(b.hasFocus());
        }
        assertNotSame(f, KeyboardFocusManager.getCurrentKeyboardFocusManager());
        assertFalse(b.hasFocus());
    }

    @Test
    void dirtyCollectsAndRestores() {
        RepaintManager before = RepaintManager.currentManager((Component) null);
        JPanel p = new JPanel();
        p.setSize(10, 10);
        Input.Dirty d = Input.dirty();
        try (d) {
            assertNull(d.union());
            p.repaint();
            p.repaint(1, 12, 3, 4);
        }
        assertEquals(List.of(p, p), d.who());
        assertEquals(List.of(new Rectangle(0, 0, 10, 10), new Rectangle(1, 12, 3, 4)), d.where());
        assertEquals(new Rectangle(0, 0, 10, 16), d.union());
        assertSame(before, RepaintManager.currentManager((Component) null));
    }
}
