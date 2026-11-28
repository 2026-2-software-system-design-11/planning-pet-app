package planningpet.pet.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Dimension;
import java.awt.Rectangle;
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

    private static PickCard blank(PetKind kind, boolean selected) {
        return new PickCard("", kind, selected, "");
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
        Snap.assertLaidOutLooksLikePrototype("pickcard-cat", blank(PetKind.CAT, false), 248, 262);
        Snap.assertLaidOutLooksLikePrototype("pickcard-dog", blank(PetKind.DOG, false), 248, 262);
    }
}
