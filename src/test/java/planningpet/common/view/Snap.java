package planningpet.common.view;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.JLabel;

/**
 * 부품을 창 없이 w x h로 두고 (w+4) x (h+4) 그림에 그려, 프로토타입이 같은 방법으로 그린 기준 그림
 * (src/test/resources의 proto/이름.png)과 픽셀이 모두 같은지 본다. docs/porting/theme.md 2절.
 * 창 틀(app.view)의 기준 그림도 같은 폴더에 frame-이름으로 둔다.
 */
public final class Snap {
    private Snap() {
    }

    /** 부품을 w x h로 두고 (w+4) x (h+4) 그림에 그린다. */
    public static BufferedImage paint(JComponent c, int w, int h) {
        c.setSize(w, h);
        BufferedImage img = new BufferedImage(w + 4, h + 4, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        c.paint(g);
        g.dispose();
        return img;
    }

    /** 이미 배치한 부품을 지금 크기 그대로 그린다. */
    public static BufferedImage paint(JComponent c) {
        return paint(c, c.getWidth(), c.getHeight());
    }

    /** 글이 놓인 자리처럼 운영체제마다 다른 픽셀을 자홍(0xFF00FF)으로 덮는다. 기준 그림도 같은 자리를 덮어 둔다. */
    public static BufferedImage cover(BufferedImage img, Rectangle... boxes) {
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(0xFF00FF));
        for (Rectangle r : boxes) {
            g.fill(r);
        }
        g.dispose();
        return img;
    }

    /** 그림의 픽셀을 줄 순서로 늘어놓는다. 두 그림이 같은지 볼 때 쓴다. */
    public static int[] pixels(BufferedImage img) {
        return img.getRGB(0, 0, img.getWidth(), img.getHeight(), null, 0, img.getWidth());
    }

    static void assertLooksLikePrototype(String name, JComponent c, int w, int h) throws IOException {
        assertLooksLikePrototype(name, paint(c, w, h));
    }

    /** 창 없이는 배치가 돌지 않아 자식까지 배치한 뒤 기준 그림과 맞춘다(자식이 든 부품). */
    public static void assertLaidOutLooksLikePrototype(String name, JComponent c, int w, int h) throws IOException {
        layOut(c, w, h);
        assertLooksLikePrototype(name, c, w, h);
    }

    /** 부품을 w x h로 두고 창 없이 자식까지 배치한다. */
    public static void layOut(Component c, int w, int h) {
        c.setSize(w, h);
        layOut(c);
    }

    private static void layOut(Component c) {
        if (c instanceof Container k) {
            k.doLayout();
            for (Component child : k.getComponents()) {
                layOut(child);
            }
        }
    }

    /** 운영체제마다 픽셀이 다른 글을 그리지 않도록 라벨과 버튼의 글을 자식까지 모두 비운다. */
    public static void blank(Component c) {
        if (c instanceof JLabel l) {
            l.setText("");
        }
        if (c instanceof AbstractButton b) {
            b.setText("");
        }
        if (c instanceof Container k) {
            for (Component child : k.getComponents()) {
                blank(child);
            }
        }
    }

    /** 이미 그린 그림을 기준 그림과 맞춘다(아이콘처럼 부품이 아닌 것). */
    public static void assertLooksLikePrototype(String name, BufferedImage got) throws IOException {
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
