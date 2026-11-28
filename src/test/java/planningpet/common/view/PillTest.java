package planningpet.common.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import org.junit.jupiter.api.Test;

/** 알약 탭. 번호는 docs/porting/character.md S8d 2절. 그림을 프로토타입과 견주지 않고 크기와 칠한 색을 본다. */
class PillTest {
    private static Pill pill(String text, Icons.Kind kind, boolean selected, boolean enabled) {
        Pill p = new Pill(text, kind, 14);
        p.setSelected(selected);
        p.setEnabled(enabled);
        return p;
    }

    private static int width(int size, String text) {
        return new Pill("", null, 14).getFontMetrics(Theme.bold(size)).stringWidth(text);
    }

    /** 바라는 크기로 그린 그림의 (가운데, 3) 픽셀. 글과 아이콘 위의 바탕이다. */
    private static int fill(Pill p) {
        Dimension d = p.getPreferredSize();
        return Snap.paint(p, d.width, d.height).getRGB((d.width - 1) / 2, 3);
    }

    /** 높이 h의 칠 칸을 위에서 아래로 MINT_TOP에서 MINT_DEEP으로 칠했을 때 y줄의 색. */
    private static int mint(int h, int y) {
        BufferedImage col = new BufferedImage(1, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = col.createGraphics();
        g.setPaint(new GradientPaint(0, 0, Theme.MINT_TOP, 0, h, Theme.MINT_DEEP));
        g.fillRect(0, 0, 1, h);
        g.dispose();
        return col.getRGB(0, y);
    }

    @Test
    void ePill1OneLineSize() {
        Pill icon = new Pill("상태", Icons.Kind.CHART, 14);
        Pill plain = new Pill("상태", null, 14);
        int text = width(14, "상태");
        assertEquals(new Dimension(text + 60, 32), icon.getPreferredSize());
        assertEquals(new Dimension(text + 34, 32), plain.getPreferredSize());
        icon.showIcon(false);
        assertEquals(plain.getPreferredSize(), icon.getPreferredSize());
    }

    @Test
    void ePill3SelectedFillAndWhiteText() {
        assertEquals(mint(28, 3), fill(pill("상태", null, true, true)));
        assertEquals(Theme.CARD_TOP.getRGB(), fill(pill("상태", null, false, true)));
    }

    @Test
    void ePill4DisabledTextDim() {
        assertEquals(mint(28, 3), fill(pill("상태", null, true, false)));
        assertEquals(Theme.CARD_TOP.getRGB(), fill(pill("상태", null, false, false)));
    }

    @Test
    void ePill5RolloverFill() {
        Pill off = pill("상태", null, false, true);
        off.getModel().setRollover(true);
        assertEquals(Theme.MINT_PALE.getRGB(), fill(off));
        Pill on = pill("상태", null, true, true);
        on.getModel().setRollover(true);
        assertEquals(mint(28, 3), fill(on));
    }

    @Test
    void ePill6Properties() {
        Pill p = new Pill("상태", Icons.Kind.CHART, 14);
        assertEquals(Cursor.HAND_CURSOR, p.getCursor().getType());
        assertFalse(p.isFocusPainted());
        assertFalse(p.isOpaque());
        assertFalse(p.isContentAreaFilled());
        assertFalse(p.isBorderPainted());
        assertTrue(p.isRolloverEnabled());
        assertEquals(Theme.bold(14), p.getFont());
    }
}
