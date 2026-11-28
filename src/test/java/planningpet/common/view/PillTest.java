package planningpet.common.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;
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

    /** 바탕 under 위에 ink를 다 덮은 색. */
    private static int over(Color ink, int under) {
        BufferedImage one = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        one.setRGB(0, 0, under);
        Graphics2D g = one.createGraphics();
        g.setColor(ink);
        g.fillRect(0, 0, 1, 1);
        g.dispose();
        return one.getRGB(0, 0);
    }

    /** 두 색의 채널마다 차이가 1 이하다. 반투명한 글은 글자 그리기와 칠하기의 반올림이 달라 1 다를 수 있다. */
    private static boolean near(int a, int b) {
        for (int shift = 0; shift < 32; shift += 8) {
            if (Math.abs((a >>> shift & 0xFF) - (b >>> shift & 0xFF)) > 1) {
                return false;
            }
        }
        return true;
    }

    /** with에만 있는 픽셀(글이나 아이콘) 가운데 그 자리 바탕에 ink를 다 덮은 색(채널마다 1까지)이 있으면 참이다. */
    private static boolean inked(BufferedImage with, BufferedImage without, Color ink) {
        for (int y = 0; y < with.getHeight(); y++) {
            for (int x = 0; x < with.getWidth(); x++) {
                int was = without.getRGB(x, y);
                if (with.getRGB(x, y) != was && near(with.getRGB(x, y), over(ink, was))) {
                    return true;
                }
            }
        }
        return false;
    }

    /** 바라는 크기로 그리고, blank로 글을 비워 같은 크기로 다시 그려 비운 글의 색이 ink인지 본다. */
    private static boolean inks(Pill p, Runnable blank, Color ink) {
        Dimension d = p.getPreferredSize();
        BufferedImage with = Snap.paint(p, d.width, d.height);
        blank.run();
        return inked(with, Snap.paint(p, d.width, d.height), ink);
    }

    private static boolean textIs(Pill p, Color ink) {
        return inks(p, () -> p.setText(""), ink);
    }

    /** 첫 줄 글이 없는 크기 30 알약의 작은 글 "고양이"의 색. 작은 글이 가늘어 크기 14로는 다 칠한 픽셀이 없다. */
    private static boolean smallIs(boolean selected, boolean enabled, Color ink) {
        Pill p = new Pill("", null, 30).small("고양이");
        p.setSelected(selected);
        p.setEnabled(enabled);
        return inks(p, () -> p.small(null), ink);
    }

    /** 글 없는 알약을 발자국이 있는 것과 없는 것으로 같은 크기에 그려 아이콘 색이 ink인지 본다. */
    private static boolean iconIs(boolean selected, boolean enabled, Color ink) {
        Pill with = pill("", Icons.Kind.PAW, selected, enabled);
        Dimension d = with.getPreferredSize();
        return inked(Snap.paint(with, d.width, d.height), Snap.paint(pill("", null, selected, enabled), d.width, d.height), ink);
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
        int small = plain.getFontMetrics(Theme.font(11)).stringWidth("고양이");
        assertEquals(new Dimension(text + 5 + small + 34, 32), plain.small("고양이").getPreferredSize());
    }

    @Test
    void ePill2TwoLinesAndPictureSize() {
        BufferedImage face = new BufferedImage(96, 96, BufferedImage.TYPE_INT_ARGB);
        Pill tab = new Pill("나비", null, 16).second("Lv. 3");
        int text = Math.max(width(16, "나비"), width(15, "Lv. 3"));
        assertEquals(new Dimension(text + 38, 58), tab.getPreferredSize());
        assertEquals(new Dimension(text + 84, 58), tab.picture(face).getPreferredSize());
        assertEquals(width(15, "Lv. 10") + 38, new Pill("가", null, 16).second("Lv. 10").getPreferredSize().width);
        Pill chip = new Pill("나비", null, 13).picture(face);
        assertEquals(new Dimension(width(13, "나비") + 72, 40), chip.getPreferredSize());
        assertEquals(chip.getPreferredSize(), new Pill("나비", Icons.Kind.PAW, 13).picture(face).getPreferredSize());
    }

    @Test
    void ePill3SelectedFillAndWhiteText() {
        assertEquals(mint(28, 3), fill(pill("상태", null, true, true)));
        assertTrue(textIs(pill("상태", null, true, true), Color.WHITE));
        assertTrue(iconIs(true, true, Color.WHITE));
        assertTrue(smallIs(true, true, new Color(255, 255, 255, 215)));
        assertEquals(Theme.CARD_TOP.getRGB(), fill(pill("상태", null, false, true)));
        assertTrue(textIs(pill("상태", null, false, true), Theme.INK));
        assertTrue(iconIs(false, true, Theme.ICON));
        assertTrue(smallIs(false, true, Theme.INK_SUB));
    }

    @Test
    void ePill4DisabledTextDim() {
        for (boolean on : new boolean[] {true, false}) {
            assertTrue(textIs(pill("상태", null, on, false), Theme.INK_DIM), "고름 " + on);
            assertTrue(smallIs(on, false, Theme.INK_DIM), "고름 " + on);
        }
        assertEquals(mint(28, 3), fill(pill("상태", null, true, false)));
        assertEquals(Theme.CARD_TOP.getRGB(), fill(pill("상태", null, false, false)));
        assertTrue(iconIs(true, false, Theme.INK_DIM));
        assertFalse(iconIs(true, false, Color.WHITE));
        assertTrue(iconIs(false, false, Theme.INK_DIM));
        assertFalse(iconIs(false, false, Theme.ICON));
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
        assertNull(p.small());
        assertEquals("고양이", p.small("고양이").small());
    }

    @Test
    void ePill7PicturePlace() {
        BufferedImage red = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = red.createGraphics();
        g.setColor(Color.RED);
        g.fillRect(0, 0, 10, 10);
        g.dispose();
        Pill tab = new Pill("나비", null, 16).second("Lv. 3").picture(red);
        Dimension d = tab.getPreferredSize();
        BufferedImage img = Snap.paint(tab, d.width, d.height);
        int x0 = img.getWidth();
        int y0 = img.getHeight();
        int x1 = -1;
        int y1 = -1;
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                if (img.getRGB(x, y) == Color.RED.getRGB()) {
                    x0 = Math.min(x0, x);
                    y0 = Math.min(y0, y);
                    x1 = Math.max(x1, x);
                    y1 = Math.max(y1, y);
                }
            }
        }
        assertEquals(List.of(18, 8, 55, 45), List.of(x0, y0, x1, y1));
    }

    /** 좁은 칸에서 첫 줄 줄이기. 프로토타입과 일부러 다름(docs/porting/character.md S8e E-PILL-8). */
    @Test
    void ePill8ShortenWhenNarrow() {
        String name = "가나다라마바사아자차";
        Pill p = new Pill(name, null, 16).small("강아지").picture(new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB))
                .second("Lv. 3");
        Dimension d = p.getPreferredSize();
        p.setSize(d);
        assertEquals(name, p.shown());
        p.setSize(200, 58);
        String cut = p.shown();
        assertTrue(cut.endsWith("…") && name.startsWith(cut.substring(0, cut.length() - 1)), cut);
        assertTrue(width(16, cut) + 5 + p.getFontMetrics(Theme.font(13)).stringWidth("강아지") <= 200 - 84, cut);
        p.setSize(148, 58);
        assertEquals("가…", p.shown());
    }
}
