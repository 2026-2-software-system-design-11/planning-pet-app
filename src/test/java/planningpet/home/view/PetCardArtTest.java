package planningpet.home.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import planningpet.common.view.Snap;
import planningpet.common.view.Theme;

/** 홈 펫 카드의 그림 도구. 번호는 docs/porting/home.md S3j 2절. 기준 그림은 proto/scene-*.png다. */
class PetCardArtTest {
    /** 프로토타입 PetCard처럼 안티에일리어싱과 품질 우선을 켜고, 틀보다 오른쪽과 아래로 4 큰 그림에 그린다. */
    private static BufferedImage scene(Rectangle f, int ground) {
        BufferedImage img = new BufferedImage(f.x + f.width + 4, f.y + f.height + 4, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        PetCardArt.scene(g, f, ground);
        g.dispose();
        return img;
    }

    /** 카드 w x h의 PetCard 틀과 땅 선(틀 아래에서 높이의 1/11, 최소 14). */
    private static BufferedImage card(int w, int h) {
        Rectangle f = new Rectangle(2, 1, w - 4, h - 8);
        return scene(f, f.y + f.height - Math.max(14, f.height / 11));
    }

    @Test
    void eScene1CardSizes() throws IOException {
        for (int[] s : new int[][] {{360, 240}, {300, 210}, {800, 500}}) {
            Snap.assertLooksLikePrototype("scene-card-" + s[0] + "x" + s[1], card(s[0], s[1]));
        }
    }

    @Test
    void eScene2MovedFrame() throws IOException {
        Snap.assertLooksLikePrototype("scene-moved", scene(new Rectangle(30, 20, 356, 232), 20 + 232 - 21));
    }

    @Test
    void eMix1() {
        Color a = Theme.GRASS;
        Color b = Theme.GRASS_FAR;
        assertEquals(a, PetCardArt.mix(a, b, 0));
        assertEquals(b, PetCardArt.mix(a, b, 1));
        assertEquals(new Color(0xD6ECAF), PetCardArt.mix(a, b, 0.35));
        assertEquals(new Color(212, 236, 181), PetCardArt.mix(a, b, 0.5));
        assertEquals(new Color(228, 238, 143), PetCardArt.mix(a, b, -0.5));
        assertEquals(new Color(188, 233, 238), PetCardArt.mix(a, b, 2));
        assertEquals(new Color(127, 127, 127), PetCardArt.mix(Color.BLACK, Color.WHITE, 0.5));
        assertEquals(new Color(254, 254, 254), PetCardArt.mix(Color.BLACK, Color.WHITE, 0.999));
        assertEquals(255, PetCardArt.mix(new Color(10, 20, 30, 40), new Color(10, 20, 30, 90), 0.5).getAlpha());
        assertThrows(IllegalArgumentException.class, () -> PetCardArt.mix(Color.WHITE, Color.BLACK, -1));
    }

    @Test
    void eAlpha1() {
        for (int a : new int[] {0, 120, 200, 255}) {
            assertEquals(new Color(0xCC, 0xEB, 0xC8, a), PetCardArt.withAlpha(Theme.GRASS_FAR, a));
        }
        assertEquals(new Color(1, 2, 3, 200), PetCardArt.withAlpha(new Color(1, 2, 3, 9), 200));
        assertThrows(IllegalArgumentException.class, () -> PetCardArt.withAlpha(Theme.GRASS_FAR, 256));
        assertThrows(IllegalArgumentException.class, () -> PetCardArt.withAlpha(Theme.GRASS_FAR, -1));
    }
}
