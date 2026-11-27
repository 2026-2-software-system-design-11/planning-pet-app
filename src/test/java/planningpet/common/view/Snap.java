package planningpet.common.view;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JComponent;

/**
 * 부품을 창 없이 w x h로 두고 (w+4) x (h+4) 그림에 그려, 프로토타입이 같은 방법으로 그린 기준 그림
 * (src/test/resources의 proto/이름.png)과 픽셀이 모두 같은지 본다. docs/porting/theme.md 2절.
 */
final class Snap {
    private Snap() {
    }

    static BufferedImage paint(JComponent c, int w, int h) {
        c.setSize(w, h);
        BufferedImage img = new BufferedImage(w + 4, h + 4, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        c.paint(g);
        g.dispose();
        return img;
    }

    static void assertLooksLikePrototype(String name, JComponent c, int w, int h) throws IOException {
        assertLooksLikePrototype(name, paint(c, w, h));
    }

    /** 이미 그린 그림을 기준 그림과 맞춘다(아이콘처럼 부품이 아닌 것). */
    static void assertLooksLikePrototype(String name, BufferedImage got) throws IOException {
        BufferedImage want = ImageIO.read(Snap.class.getResource("proto/" + name + ".png"));
        assertEquals(want.getWidth() + "x" + want.getHeight(), got.getWidth() + "x" + got.getHeight(), name);
        int differ = 0;
        for (int y = 0; y < got.getHeight(); y++) {
            for (int x = 0; x < got.getWidth(); x++) {
                differ += got.getRGB(x, y) == want.getRGB(x, y) ? 0 : 1;
            }
        }
        assertEquals(0, differ, name + "에서 프로토타입과 다른 픽셀 수");
    }
}
