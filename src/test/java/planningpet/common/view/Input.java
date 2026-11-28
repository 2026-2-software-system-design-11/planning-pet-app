package planningpet.common.view;

import java.awt.Component;
import java.awt.DefaultKeyboardFocusManager;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.RepaintManager;

/** 테스트가 함께 쓰는 창 없는 누르기, 초점, 다시 그리기 자리 모으기(#520). */
public final class Input {
    private Input() {
    }

    /** 부품의 (5, 5)에 마우스 사건 하나를 보낸다. 누름, 뗌, 누름 끝은 왼쪽 단추 한 번이다. */
    public static void mouse(Component c, int id) {
        boolean press = id == MouseEvent.MOUSE_PRESSED || id == MouseEvent.MOUSE_RELEASED
                || id == MouseEvent.MOUSE_CLICKED;
        int mods = id == MouseEvent.MOUSE_PRESSED ? InputEvent.BUTTON1_DOWN_MASK : 0;
        c.dispatchEvent(new MouseEvent(c, id, 0, mods, 5, 5, press ? 1 : 0, false,
                press ? MouseEvent.BUTTON1 : MouseEvent.NOBUTTON));
    }

    /** 사건을 보내지 않고 부품의 마우스 듣는 이 모두에 (5, 5)의 왼쪽 단추 한 번 누름을 알린다. */
    public static void click(Component c) {
        MouseEvent e = new MouseEvent(c, MouseEvent.MOUSE_CLICKED, 0, 0, 5, 5, 1, false, MouseEvent.BUTTON1);
        for (MouseListener l : c.getMouseListeners()) {
            l.mouseClicked(e);
        }
    }

    /** 창 없이 초점을 줄 수 있는 관리자를 건다. try-with-resources로 쓰면 끝날 때 기본 관리자로 돌린다. */
    public static Focus focus() {
        Focus f = new Focus();
        KeyboardFocusManager.setCurrentKeyboardFocusManager(f);
        return f;
    }

    /** 창 없이 초점 주인을 정하는 관리자. 창이 없으면 requestFocus가 듣지 않아 setGlobalFocusOwner를 연다. */
    public static final class Focus extends DefaultKeyboardFocusManager implements AutoCloseable {
        private Focus() {
        }

        public void own(Component c) {
            setGlobalFocusOwner(c);
        }

        /** 초점 주인은 관리자가 바뀌어도 남으므로 먼저 비우고 기본 관리자로 돌린다. */
        @Override
        public void close() {
            setGlobalFocusOwner(null);
            KeyboardFocusManager.setCurrentKeyboardFocusManager(null);
        }
    }

    /** 다시 그리라고 한 부품과 자리를 모으는 관리자를 건다. try-with-resources로 쓰면 끝날 때 앞의 관리자로 돌린다. */
    public static Dirty dirty() {
        Dirty d = new Dirty(RepaintManager.currentManager((Component) null));
        RepaintManager.setCurrentManager(d);
        return d;
    }

    /** 다시 그리라고 한 부품과 자리를 받은 차례대로 모은다. 그리지는 않는다. */
    public static final class Dirty extends RepaintManager implements AutoCloseable {
        private final RepaintManager before;
        private final List<JComponent> who = new ArrayList<>();
        private final List<Rectangle> where = new ArrayList<>();

        private Dirty(RepaintManager before) {
            this.before = before;
        }

        @Override
        public void addDirtyRegion(JComponent c, int x, int y, int w, int h) {
            who.add(c);
            where.add(new Rectangle(x, y, w, h));
        }

        public List<JComponent> who() { return List.copyOf(who); }

        public List<Rectangle> where() { return List.copyOf(where); }

        /** 모은 자리를 모두 덮는 가장 작은 네모. 하나도 없으면 null이다. */
        public Rectangle union() {
            return where.stream().reduce(Rectangle::union).orElse(null);
        }

        @Override
        public void close() {
            RepaintManager.setCurrentManager(before);
        }
    }
}
