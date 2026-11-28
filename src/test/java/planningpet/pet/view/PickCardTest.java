package planningpet.pet.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.DefaultKeyboardFocusManager;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.RepaintManager;
import org.junit.jupiter.api.Test;
import planningpet.common.view.Snap;
import planningpet.common.view.Theme;
import planningpet.pet.model.PetKind;

/**
 * 반려동물 고르기 창의 동물 카드. 번호는 docs/porting/pick.md 2절. 기준 그림은 종류 이름과 설명이 빈 글인 카드만
 * 두고(proto/pickcard-*.png), 글이 든 그림은 운영체제마다 픽셀이 달라 테스트에 두지 않는다.
 */
class PickCardTest {
    private static final String CAT_ABOUT = "조용하고 편안한 성격으로\n언제나 곁에서 응원해요!";
    private static final String DOG_ABOUT = "활발하고 다정한 성격으로\n항상 힘이 되어줘요!";

    /** 다시 그리라고 한 자리를 모은다. */
    private static final class Dirty extends RepaintManager {
        private final Set<Rectangle> all = new HashSet<>();

        @Override
        public void addDirtyRegion(JComponent c, int x, int y, int w, int h) {
            all.add(new Rectangle(x, y, w, h));
        }
    }

    /** 창 없이 초점을 주려고 setGlobalFocusOwner를 연다(RoundFieldTest와 같다). 검사 동안만 건다. */
    private static final class Focus extends DefaultKeyboardFocusManager {
        void own(Component c) {
            setGlobalFocusOwner(c);
        }
    }

    private static PickCard blank(PetKind kind, boolean selected) {
        return new PickCard("", kind, selected, "");
    }

    private static void looks(String name, PickCard c, int w, int h) throws IOException {
        Snap.assertLaidOutLooksLikePrototype("pickcard-" + name, c, w, h);
    }

    private static void mouse(PickCard c, int id) {
        c.dispatchEvent(new MouseEvent(c, id, 0, 0, 5, 5, 0, false));
    }

    private static long nanos(double seconds) {
        return Math.round(seconds * 1e9);
    }

    /** 만든 때에서 t초, 고른 때에서 since초 지난 시각으로 센 카드. since가 99면 고른 때를 두지 않는다. */
    private static PickCard at(PetKind kind, boolean selected, double t, double since) {
        PickCard c = blank(kind, selected);
        if (since != 99) {
            c.pickedAt = c.start + nanos(t - since);
        }
        c.tick(c.start + nanos(t));
        assertEquals(t, c.t);
        assertEquals(since, c.since);
        return c;
    }

    private static String described(String about) {
        return new PickCard("x", PetKind.CAT, false, about).getAccessibleContext().getAccessibleDescription();
    }

    @Test
    void eCard1Values() {
        PickCard cat = new PickCard("고양이", PetKind.CAT, true, CAT_ABOUT);
        PickCard dog = new PickCard("강아지", PetKind.DOG, false, DOG_ABOUT);
        assertTrue(cat.isSelected());
        assertFalse(dog.isSelected());
        for (PickCard c : new PickCard[] {cat, dog}) {
            assertFalse(c.isOpaque() || c.isContentAreaFilled() || c.isBorderPainted() || c.isFocusPainted());
            assertTrue(c.isRolloverEnabled() && c.isFocusable());
            assertEquals(Theme.heavy(22), c.getFont());
        }
        assertEquals("강아지", dog.getAccessibleContext().getAccessibleName());
        assertEquals("조용하고 편안한 성격으로 언제나 곁에서 응원해요!", cat.getAccessibleContext().getAccessibleDescription());
        assertEquals("활발하고 다정한 성격으로 항상 힘이 되어줘요!", dog.getAccessibleContext().getAccessibleDescription());
    }

    @Test
    void eCard2AboutAndName() {
        assertEquals("한 줄", described("한 줄"));
        assertEquals("", described(""));
        assertEquals("가 나 다", described("가\n나\n다"));
        assertEquals("가  나 ", described("가\n\n나\n"));
        assertThrows(NullPointerException.class, () -> new PickCard("x", PetKind.CAT, false, null));
        assertEquals("", new PickCard(null, PetKind.CAT, false, "").getText());
    }

    @Test
    void eCard3Size() {
        PickCard c = new PickCard("", PetKind.CAT, false, "");
        Dimension card = new Dimension(248, 262);
        for (int i = 0; i < 2; i++) {
            assertEquals(card, c.getPreferredSize());
            assertEquals(card, c.getMinimumSize());
            assertEquals(card, c.getMaximumSize());
            c.setPreferredSize(new Dimension(100, 100));
            c.setMinimumSize(new Dimension(10, 10));
            c.setMaximumSize(new Dimension(1000, 1000));
        }
    }

    @Test
    void eCard9GroupAndClick() {
        PickCard cat = new PickCard("고양이", PetKind.CAT, true, CAT_ABOUT);
        PickCard dog = new PickCard("강아지", PetKind.DOG, false, DOG_ABOUT);
        cat.setSize(248, 262);
        dog.setSize(248, 262);
        ButtonGroup group = new ButtonGroup();
        group.add(cat);
        group.add(dog);
        Dirty dirty = new Dirty();
        RepaintManager.setCurrentManager(dirty);
        try {
            dog.doClick(0);
            assertTrue(dog.isSelected() && !cat.isSelected());
            dog.doClick(0);
            assertTrue(dog.isSelected() && !cat.isSelected());
            cat.doClick(0);
            assertTrue(cat.isSelected() && !dog.isSelected());
            assertEquals(Set.of(new Rectangle(0, 0, 248, 262)), dirty.all);
        } finally {
            RepaintManager.setCurrentManager(null);
        }
        PickCard lone = new PickCard("", PetKind.CAT, false, "");
        lone.doClick(0);
        assertTrue(lone.isSelected());
        lone.doClick(0);
        assertFalse(lone.isSelected());
        PickCard off = new PickCard("", PetKind.DOG, false, "");
        off.setEnabled(false);
        off.doClick(0);
        assertFalse(off.isSelected());
    }

    @Test
    void eCard4Plain() throws IOException {
        looks("cat", blank(PetKind.CAT, false), 248, 262);
        looks("dog", blank(PetKind.DOG, false), 248, 262);
    }

    @Test
    void eCard5Picked() throws IOException {
        looks("cat-on", blank(PetKind.CAT, true), 248, 262);
        looks("dog-on", blank(PetKind.DOG, true), 248, 262);
    }

    @Test
    void eCard6RolloverAndDisabled() throws IOException {
        for (PetKind kind : PetKind.values()) {
            String name = kind == PetKind.CAT ? "cat" : "dog";
            PickCard c = blank(kind, false);
            mouse(c, MouseEvent.MOUSE_ENTERED);
            looks(name + "-over", c, 248, 262);
            mouse(c, MouseEvent.MOUSE_EXITED);
            looks(name, c, 248, 262);
            PickCard on = blank(kind, true);
            mouse(on, MouseEvent.MOUSE_ENTERED);
            assertTrue(on.getModel().isRollover());
            looks(name + "-on", on, 248, 262);
        }
        for (boolean selected : new boolean[] {false, true}) {
            PickCard off = blank(PetKind.CAT, selected);
            off.setEnabled(false);
            looks(selected ? "cat-on" : "cat", off, 248, 262);
            mouse(off, MouseEvent.MOUSE_ENTERED);
            off.getModel().setRollover(true);
            assertFalse(off.getModel().isRollover());
            looks(selected ? "cat-on" : "cat", off, 248, 262);
        }
    }

    @Test
    void eCard7KeyboardFocus() throws IOException {
        Focus focus = new Focus();
        KeyboardFocusManager.setCurrentKeyboardFocusManager(focus);
        try {
            for (PetKind kind : PetKind.values()) {
                for (boolean selected : new boolean[] {false, true}) {
                    PickCard c = blank(kind, selected);
                    focus.own(c);
                    assertTrue(c.isFocusOwner());
                    looks((kind == PetKind.CAT ? "cat" : "dog") + (selected ? "-on" : "") + "-focus", c, 248, 262);
                }
            }
            PickCard over = blank(PetKind.CAT, false);
            focus.own(over);
            mouse(over, MouseEvent.MOUSE_ENTERED);
            looks("cat-focus-over", over, 248, 262);
            focus.own(blank(PetKind.DOG, false));
            looks("cat-over", over, 248, 262);
        } finally {
            KeyboardFocusManager.setCurrentKeyboardFocusManager(null);
        }
    }

    @Test
    void eCard8Sizes() throws IOException {
        int[][] sizes = {{496, 524}, {300, 300}, {100, 100}, {8, 9}, {4, 4}, {0, 0}};
        for (int[] s : sizes) {
            looks("cat-on-" + s[0] + "x" + s[1], blank(PetKind.CAT, true), s[0], s[1]);
        }
        looks("dog-on-496x524", blank(PetKind.DOG, true), 496, 524);
    }

    @Test
    void eCard10CallerStateKept() {
        PickCard c = blank(PetKind.DOG, true);
        c.setSize(248, 262);
        Graphics2D g = new BufferedImage(260, 270, BufferedImage.TYPE_INT_ARGB).createGraphics();
        BasicStroke stroke = new BasicStroke(5);
        g.setColor(Color.RED);
        g.setStroke(stroke);
        g.translate(3, 4);
        g.clipRect(10, 10, 100, 100);
        g.setFont(Theme.font(9));
        AffineTransform moved = g.getTransform();
        c.paintComponent(g);
        assertEquals(Color.RED, g.getColor());
        assertEquals(stroke, g.getStroke());
        assertEquals(moved, g.getTransform());
        assertEquals(new Rectangle(10, 10, 100, 100), g.getClipBounds());
        assertEquals(Theme.font(9), g.getFont());
        assertEquals(RenderingHints.VALUE_ANTIALIAS_OFF, g.getRenderingHint(RenderingHints.KEY_ANTIALIASING));
        g.dispose();
    }

    @Test
    void eCard14PickedAt() {
        PickCard cat = new PickCard("고양이", PetKind.CAT, true, CAT_ABOUT);
        PickCard dog = new PickCard("강아지", PetKind.DOG, false, DOG_ABOUT);
        assertEquals(0, cat.pickedAt);
        cat.tick(cat.start + nanos(5));
        assertEquals(5.0, cat.t);
        assertEquals(99.0, cat.since);
        ButtonGroup group = new ButtonGroup();
        group.add(cat);
        group.add(dog);
        dog.doClick(0);
        assertTrue(dog.pickedAt != 0);
        dog.tick(dog.pickedAt + nanos(0.2));
        assertEquals(0.2, dog.since);
        dog.pickedAt = 7;
        dog.doClick(0);
        assertEquals(7, dog.pickedAt);
        cat.doClick(0);
        assertTrue(cat.pickedAt != 0);
        assertEquals(7, dog.pickedAt);
        PickCard lone = blank(PetKind.CAT, false);
        lone.setSelected(true);
        assertTrue(lone.pickedAt != 0);
        lone.pickedAt = 7;
        lone.setSelected(false);
        assertEquals(7, lone.pickedAt);
        lone.setSelected(true);
        assertTrue(lone.pickedAt != 7 && lone.pickedAt != 0);
        PickCard off = blank(PetKind.DOG, false);
        off.setEnabled(false);
        off.doClick(0);
        assertEquals(0, off.pickedAt);
    }

    @Test
    void eCard15Moving() throws IOException {
        looks("cat-t1.1-s99.0", at(PetKind.CAT, false, 1.1, 99), 248, 262);
        looks("cat-t1.3-s99.0", at(PetKind.CAT, false, 1.3, 99), 248, 262);
        looks("dog-t0.6-s99.0", at(PetKind.DOG, false, 0.6, 99), 248, 262);
        looks("dog-t1000000.0-s99.0", at(PetKind.DOG, false, 1e6, 99), 248, 262);
        looks("cat-on-t0.0-s0.0", at(PetKind.CAT, true, 0, 0), 248, 262);
        looks("cat-on-t1.3-s0.2", at(PetKind.CAT, true, 1.3, 0.2), 248, 262);
        looks("dog-on-t0.0-s0.1", at(PetKind.DOG, true, 0, 0.1), 248, 262);
        looks("dog-on-t2.0-s0.3499", at(PetKind.DOG, true, 2.0, 0.3499), 248, 262);
    }
}
