package planningpet.common.view;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import org.junit.jupiter.api.Test;

/** 디자인 시스템 9절의 선 아이콘. 번호는 docs/porting/icons.md 2절. 기준 그림은 종마다 proto/icon-종-*.png. */
class IconsTest {
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
        for (Icons.Kind k : Icons.Kind.values()) {
            Snap.assertLooksLikePrototype("icon-" + k + "-sizes", row(k, new int[] {13, 22, 24, 30, 40, 64}, c, c, c, c, c, c));
        }
    }

    @Test
    void eIcon2Colors() throws IOException {
        for (Icons.Kind k : Icons.Kind.values()) {
            Snap.assertLooksLikePrototype("icon-" + k + "-colors",
                    row(k, new int[] {22, 24, 30}, Theme.INK_DIM, Color.WHITE, Theme.COIN));
        }
    }

    @Test
    void eIcon3VerySmall() throws IOException {
        Color c = Theme.ICON;
        for (Icons.Kind k : Icons.Kind.values()) {
            Snap.assertLooksLikePrototype("icon-" + k + "-small", row(k, new int[] {0, 1, 7}, c, c, c));
        }
    }
}
