package planningpet.common.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.awt.FlowLayout;
import java.awt.Insets;
import java.io.IOException;
import org.junit.jupiter.api.Test;

/** 디자인 시스템 5절의 카드. 번호는 docs/porting/theme.md 2절. */
class CardTest {
    @Test
    void eCard1UsualSize() throws IOException {
        Card card = new Card(new FlowLayout());
        assertEquals(new Insets(14, 16, 18, 16), card.getInsets());
        assertFalse(card.isOpaque());
        Snap.assertLooksLikePrototype("card-200x100", card, 200, 100);
        Snap.assertLooksLikePrototype("card-1000x700", new Card(new FlowLayout()), 1000, 700);
    }

    @Test
    void eCard2OwnRadius() throws IOException {
        Card card = new Card(new FlowLayout(), 24);
        assertEquals(new Insets(0, 0, 0, 0), card.getInsets());
        Snap.assertLooksLikePrototype("card-r24-200x100", card, 200, 100);
    }

    @Test
    void eCard3FilledWithOneColor() throws IOException {
        Snap.assertLooksLikePrototype("card-filled-200x100", new Card(new FlowLayout()).filled(Theme.SIDE), 200, 100);
    }

    @Test
    void eCard4ZeroSize() throws IOException {
        Snap.assertLooksLikePrototype("card-0x0", new Card(new FlowLayout()), 0, 0);
    }

    @Test
    void eCard5SmallerThanShadow() throws IOException {
        Snap.assertLooksLikePrototype("card-4x8", new Card(new FlowLayout()), 4, 8);
        Snap.assertLooksLikePrototype("card-5x9", new Card(new FlowLayout()), 5, 9);
    }
}
