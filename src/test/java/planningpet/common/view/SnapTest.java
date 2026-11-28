package planningpet.common.view;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.List;
import javax.swing.JPanel;
import org.junit.jupiter.api.Test;

/** Snap의 그리기, 덮기, 픽셀 늘어놓기(#518). 글 없는 판 하나로 본다. */
class SnapTest {
    private static JPanel plate() {
        JPanel p = new JPanel();
        p.setBackground(Color.BLUE);
        return p;
    }

    @Test
    void paintsAtSizeWithFourPixelsMore() {
        BufferedImage img = Snap.paint(plate(), 6, 3);
        assertEquals(List.of(10, 7, Color.BLUE.getRGB(), 0), List.of(img.getWidth(), img.getHeight(), img.getRGB(5, 2),
                img.getRGB(6, 3)));
    }

    @Test
    void paintsLaidOutSizeAsIs() {
        JPanel p = plate();
        p.setSize(6, 3);
        assertArrayEquals(Snap.pixels(Snap.paint(plate(), 6, 3)), Snap.pixels(Snap.paint(p)));
    }

    @Test
    void coversBoxesInMagenta() {
        BufferedImage img = Snap.cover(Snap.paint(plate(), 6, 3), new Rectangle(1, 1, 2, 1));
        int magenta = new Color(0xFF00FF).getRGB();
        assertEquals(List.of(magenta, magenta, Color.BLUE.getRGB()), List.of(img.getRGB(1, 1), img.getRGB(2, 1),
                img.getRGB(3, 1)));
    }

    @Test
    void pixelsAreRowByRow() {
        BufferedImage img = new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB);
        img.setRGB(1, 0, 7);
        img.setRGB(0, 1, 9);
        assertArrayEquals(new int[] {0, 7, 9, 0}, Snap.pixels(img));
    }
}
