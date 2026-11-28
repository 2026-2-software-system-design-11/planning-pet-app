package planningpet.pet.view;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import planningpet.common.view.Snap;

/** 고르기 창의 그림 도구. 번호는 docs/porting/pick.md 2절. 기준 그림은 proto/pick-scene-*.png. */
class PickSceneTest {
    /** w x h(작으면 4 x 4) 투명 그림에 앤티에일리어싱을 켜고 draw로 그려 기준 그림 name과 맞춘다. */
    private static void same(String name, int w, int h, Consumer<Graphics2D> draw) throws IOException {
        BufferedImage img = new BufferedImage(Math.max(4, w), Math.max(4, h), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        PickScene.hints(g);
        draw.accept(g);
        g.dispose();
        Snap.assertLooksLikePrototype(name, img);
    }

    @Test
    void ePick1StarAndBud() throws IOException {
        same("pick-scene-star-bud", 160, 40, g -> {
            PickScene.star(g, 20, 20, 12);
            PickScene.star(g, 45, 20, 5.5);
            PickScene.star(g, 60, 20, 0);
            PickScene.bud(g, 80, 20, 6);
            PickScene.bud(g, 100, 20, 4);
            PickScene.bud(g, 120, 20, 0);
            PickScene.bud(g, 140, 20, 12);
        });
    }

    @Test
    void ePick2AlphaAndMix() {
        assertEquals(new Color(1, 2, 3, 9), PickScene.alpha(new Color(1, 2, 3, 200), 9));
        assertEquals(new Color(127, 127, 127), PickScene.mix(Color.BLACK, Color.WHITE, 0.5));
        assertEquals(255, PickScene.mix(new Color(10, 20, 30, 100), Color.WHITE, 0).getAlpha());
        assertEquals(new Color(254, 254, 254), PickScene.mix(Color.BLACK, Color.WHITE, 0.999));
    }

    @Test
    void ePick3DaisyAndBush() throws IOException {
        same("pick-scene-flowers", 360, 80, g -> {
            PickScene.daisy(g, 20, 40, 7, 0.2);
            PickScene.daisy(g, 50, 40, 6.5, -0.3);
            PickScene.daisy(g, 90, 40, 14, 0);
            PickScene.daisy(g, 120, 40, 2, 0);
            PickScene.daisy(g, 140, 40, 0, 0);
            PickScene.bush(g, 190, 70, 46);
            PickScene.bush(g, 260, 70, 40);
            PickScene.bush(g, 330, 70, 0);
        });
    }

    @Test
    void ePick4RibbonAndSkyPaw() throws IOException {
        same("pick-scene-ribbon", 760, 80, g -> {
            PickScene.ribbon(g, 120, 8, 380, 40);
            PickScene.skyPaw(g, 120 - 78, 8 + 32, 28, -0.25);
            PickScene.skyPaw(g, 500 + 78, 8 + 32, 28, 0.25);
            PickScene.ribbon(g, 640, 20, 60, 20);
            PickScene.ribbon(g, 740, 20, 0, 0);
        });
    }
}
