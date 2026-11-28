package planningpet.common.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * 화면 머리 오른쪽의 수치 카드. 번호는 docs/porting/icons.md 2절. 기준 그림은 프로토타입처럼 Theme.install 뒤에
 * 빈 글로 그렸다. install은 앱 전체 기본값을 바꾸므로 검사마다 ThemeOn이 되돌린다.
 */
@ExtendWith(ThemeOn.class)
class StatCardTest {
    /** 프로토타입 창 틀의 출석 단추와 같은 모양. */
    private static JButton attend(String text) {
        JButton b = new JButton(text);
        b.putClientProperty(RoundButtonUI.PRIMARY, Boolean.TRUE);
        b.setFont(Theme.bold(13));
        b.setBorder(BorderFactory.createEmptyBorder(7, 14, 9, 14));
        return b;
    }

    private static StatCard.Value value(String caption, String value) {
        StatCard.Value v = new StatCard.Value();
        v.set(caption, value);
        return v;
    }

    private static void looksLikePrototype(String name, JComponent c, int w, int h) throws IOException {
        Snap.assertLaidOutLooksLikePrototype(name, c, w, h);
    }

    @Test
    void eScard1StreakCard() throws IOException {
        Card card = StatCard.of(Icons.Kind.FLAME, new StatCard.Value(), attend(""));
        assertEquals(new Insets(8, 14, 12, 16), card.getInsets());
        assertEquals(3, card.getComponentCount());
        assertEquals(new Dimension(110, 62), card.getPreferredSize());
        looksLikePrototype("stat-flame", card, 110, 62);
        Card full = StatCard.of(Icons.Kind.FLAME, value("연속 접속", "5일"), attend("출석 +20P"));
        assertEquals(new Dimension(212, 62), full.getPreferredSize());
    }

    @Test
    void eScard2PointCard() throws IOException {
        Card card = StatCard.of(Icons.Kind.COIN, new StatCard.Value(), null);
        assertEquals(2, card.getComponentCount());
        assertEquals(new Dimension(72, 62), card.getPreferredSize());
        looksLikePrototype("stat-coin", card, 72, 62);
        Card full = StatCard.of(Icons.Kind.COIN, value("보유", "1,234,567P"), null);
        assertEquals(new Dimension(183, 62), full.getPreferredSize());
    }

    @Test
    void eScard3BiggerSmallerAndZero() throws IOException {
        looksLikePrototype("stat-flame-wide", StatCard.of(Icons.Kind.FLAME, new StatCard.Value(), attend("")), 320, 90);
        looksLikePrototype("stat-coin-short", StatCard.of(Icons.Kind.COIN, new StatCard.Value(), null), 120, 30);
        looksLikePrototype("stat-0x0", StatCard.of(Icons.Kind.COIN, new StatCard.Value(), null), 0, 0);
        looksLikePrototype("stat-home", StatCard.of(Icons.Kind.HOME, new StatCard.Value(), null), 120, 66);
    }

    @Test
    void eScard4ButtonPanelBeforeAndAfterClear() throws IOException {
        StatCard.Value v = new StatCard.Value();
        Card card = StatCard.of(Icons.Kind.FLAME, v, attend(""));
        assertTrue(((JComponent) card.getComponent(2)).isOpaque());
        Theme.clear(card);
        assertFalse(((JComponent) card.getComponent(2)).isOpaque());
        assertFalse(card.isOpaque());
        assertFalse(v.isOpaque());
        looksLikePrototype("stat-flame-clear", card, 110, 62);
    }

    @Test
    void eScard5ValueText() {
        assertEquals("", new StatCard.Value().getText());
        assertEquals("<html>연속 접속 <b>5일</b></html>", value("연속 접속", "5일").getText());
        assertEquals("<html> <b></b></html>", value("", "").getText());
        assertEquals("<html>보유 <b><b>x</b></b></html>", value("보유", "<b>x</b>").getText());
    }

    @Test
    void eScard6ValuePreferredSize() {
        assertEquals(new Dimension(2, 42), new StatCard.Value().getPreferredSize());
        assertEquals(45, value("연속 접속", "0일").getPreferredSize().width);
        assertEquals(29, value("보유", "0P").getPreferredSize().width);
        assertEquals(157, value("보유", "2,147,483,647P").getPreferredSize().width);
        assertEquals(new Dimension(114, 42), value("아주 긴 이름의 수치 카드", "1일").getPreferredSize());
        assertEquals(22, value("보유", "😀").getPreferredSize().width);
    }

    /** 42 칸을 세로 가운데에 두고 이름(굵은 11, INK_SUB)을 위에서 13, 값(아주 굵은 20, INK)을 38에 그린다. */
    @Test
    void eScard7ValueHeight() {
        for (int h : new int[] {30, 42, 80}) {
            BufferedImage want = new BufferedImage(84, h + 4, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = want.createGraphics();
            g.clipRect(0, 0, 80, h);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int top = (h - 42) / 2;
            g.setFont(Theme.bold(11));
            g.setColor(Theme.INK_SUB);
            g.drawString("연속 접속", 0, top + 13);
            g.setFont(Theme.heavy(20));
            g.setColor(Theme.INK);
            g.drawString("5일", 0, top + 38);
            g.dispose();
            BufferedImage got = Snap.paint(value("연속 접속", "5일"), 80, h);
            assertArrayEquals(Snap.pixels(want), Snap.pixels(got), "높이 " + h);
        }
    }

    @Test
    void eScard8NullCaption() {
        StatCard.Value v = value(null, "1");
        assertEquals("<html>null <b>1</b></html>", v.getText());
        assertThrows(NullPointerException.class, v::getPreferredSize);
    }
}
