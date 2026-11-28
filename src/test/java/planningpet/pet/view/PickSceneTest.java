package planningpet.pet.view;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import planningpet.common.view.Images;
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

    private static void clouds(Graphics2D g, BufferedImage im) {
        PickScene.cloud(g, im, 10, 10, 120);
        PickScene.cloud(g, im, 150.5, 20.25, 56);
        PickScene.cloud(g, im, 230, 10, 0);
        PickScene.cloud(g, im, 250, 10, 1);
    }

    @Test
    void ePick5Clouds() throws IOException {
        same("pick-scene-clouds", 320, 80, g -> clouds(g, Images.CLOUD));
        same("pick-scene-clouds-no-image", 320, 80, g -> clouds(g, null));
    }

    @Test
    void ePick6Corners() throws IOException {
        same("pick-scene-corners-800x651", 800, 651, g -> PickScene.corners(g, 800, 651));
        same("pick-scene-corners-800x200", 800, 200, g -> PickScene.corners(g, 800, 200));
    }

    private static void back(int w, int h) throws IOException {
        same("pick-scene-" + w + "x" + h, w, h, g -> PickScene.backdrop(g, w, h));
    }

    @Test
    void ePick7UsualWindow() throws IOException {
        back(800, 651);
    }

    @Test
    void ePick8OtherWindowSizes() throws IOException {
        back(1600, 1302);
        back(1200, 651);
        back(800, 200);
    }

    @Test
    void ePick9ZeroAndOne() throws IOException {
        back(0, 0);
        back(1, 1);
    }

    @Test
    void ePick10NoCloudImage() throws IOException {
        same("pick-scene-800x651-no-cloud", 800, 651, g -> PickScene.backdrop(g, 800, 651, null));
    }
}
