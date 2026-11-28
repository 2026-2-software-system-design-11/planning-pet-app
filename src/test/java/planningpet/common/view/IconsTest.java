package planningpet.common.view;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import javax.swing.Icon;
import org.junit.jupiter.api.Test;

/**
 * 디자인 시스템 9절의 선 아이콘. 번호는 docs/porting/icons.md 2절과 character.md S8d2 2절. 기준 그림은 종마다 proto/icon-종-*.png이고,
 * 캐릭터 창의 여섯 종은 기준 그림 없이 동작으로 본다(E-ICON-7).
 */
class IconsTest {
    private static final EnumSet<Icons.Kind> NEW = EnumSet.of(Icons.Kind.INFO, Icons.Kind.LOCK, Icons.Kind.SHIRT, Icons.Kind.HAT,
            Icons.Kind.BOW, Icons.Kind.COLLAR);

    /** 기준 그림이 있는 종. */
    private static List<Icons.Kind> drawn() {
        return Arrays.stream(Icons.Kind.values()).filter(k -> IconsTest.class.getResource("proto/icon-" + k + "-sizes.png") != null)
                .toList();
    }

    /** 크기마다 size+4 칸을 한 줄로 두고 icon().paintIcon으로 (칸+2, 2)에 그린다. 기준 그림과 같은 배치다. */
    private static BufferedImage row(Icons.Kind kind, int[] sizes, Color... colors) {
        int w = 0;
        int h = 0;
        for (int size : sizes) {
            w += size + 4;
            h = Math.max(h, size + 4);
        }
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        int x = 0;
        for (int i = 0; i < sizes.length; i++) {
            Icons.icon(kind, sizes[i], colors[i]).paintIcon(null, g, x + 2, 2);
            x += sizes[i] + 4;
        }
        g.dispose();
        return img;
    }

    @Test
    void eIcon1FrameSizes() throws IOException {
        Color c = Theme.ICON;
        for (Icons.Kind k : drawn()) {
            Snap.assertLooksLikePrototype("icon-" + k + "-sizes", row(k, new int[] {13, 22, 24, 30, 40, 64}, c, c, c, c, c, c));
        }
    }

    @Test
    void eIcon2Colors() throws IOException {
        for (Icons.Kind k : drawn()) {
            Snap.assertLooksLikePrototype("icon-" + k + "-colors",
                    row(k, new int[] {22, 24, 30}, Theme.INK_DIM, Color.WHITE, Theme.COIN));
        }
    }

    @Test
    void eIcon3VerySmall() throws IOException {
        Color c = Theme.ICON;
        for (Icons.Kind k : drawn()) {
            Snap.assertLooksLikePrototype("icon-" + k + "-small", row(k, new int[] {0, 1, 7}, c, c, c));
        }
    }

    @Test
    void eIcon4NegativeSize() throws IOException {
        BufferedImage img = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        Icons.draw(g, Icons.Kind.HOME, 32, 32, -24, Theme.ICON);
        g.dispose();
        Snap.assertLooksLikePrototype("icon-home-neg24", img);
        assertEquals(-24, Icons.icon(Icons.Kind.HOME, -24, Theme.ICON).getIconWidth());
    }

    @Test
    void eIcon5IconSizeAndPlace() {
        Icon zero = Icons.icon(Icons.Kind.HOME, 0, Theme.ICON);
        assertEquals(0, zero.getIconWidth());
        assertEquals(0, zero.getIconHeight());
        Icon coin = Icons.icon(Icons.Kind.COIN, 30, Theme.COIN);
        assertEquals(30, coin.getIconWidth());
        assertEquals(30, coin.getIconHeight());
        BufferedImage painted = new BufferedImage(40, 40, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = painted.createGraphics();
        coin.paintIcon(null, g, 5, 3);
        g.dispose();
        BufferedImage drawn = new BufferedImage(40, 40, BufferedImage.TYPE_INT_ARGB);
        g = drawn.createGraphics();
        Icons.draw(g, Icons.Kind.COIN, 5, 3, 30, Theme.COIN);
        g.dispose();
        assertArrayEquals(drawn.getRGB(0, 0, 40, 40, null, 0, 40), painted.getRGB(0, 0, 40, 40, null, 0, 40));
    }

    @Test
    void eIcon6CallerStateKept() {
        Graphics2D g = new BufferedImage(40, 40, BufferedImage.TYPE_INT_ARGB).createGraphics();
        g.setColor(Color.RED);
        g.setStroke(new BasicStroke(5f));
        g.translate(3, 4);
        AffineTransform before = g.getTransform();
        Object aa = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        Icons.draw(g, Icons.Kind.GEAR, 1, 1, 30, Theme.ICON);
        assertEquals(Color.RED, g.getColor());
        assertEquals(5f, ((BasicStroke) g.getStroke()).getLineWidth());
        assertEquals(before, g.getTransform());
        assertSame(aa, g.getRenderingHint(RenderingHints.KEY_ANTIALIASING));
        g.dispose();
    }

    @Test
    void eIcon7OnlyNewKindsHaveNoPrototypeImage() {
        EnumSet<Icons.Kind> left = EnumSet.allOf(Icons.Kind.class);
        left.removeAll(drawn());
        assertEquals(NEW, left);
    }

    @Test
    void eIcon7NewKindsStayInBoxWithGivenColor() {
        for (Icons.Kind k : NEW) {
            for (int size : new int[] {24, 7, 1, 0}) {
                BufferedImage img = new BufferedImage(30, 30, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = img.createGraphics();
                Icons.draw(g, k, 2, 2, size, Color.RED);
                g.dispose();
                int painted = 0;
                for (int y = 0; y < 30; y++) {
                    for (int x = 0; x < 30; x++) {
                        int argb = img.getRGB(x, y);
                        if (argb >>> 24 == 0) {
                            continue;
                        }
                        painted++;
                        assertTrue(x >= 2 && y >= 2 && x < 2 + size && y < 2 + size, k + " " + size + " 상자 밖 (" + x + ", " + y + ")");
                        assertEquals(0xFF0000, argb & 0xFFFFFF, k + " " + size + " 색");
                    }
                }
                assertEquals(size == 0, painted == 0, k + " " + size + " 칠한 픽셀 " + painted);
            }
        }
    }
}
