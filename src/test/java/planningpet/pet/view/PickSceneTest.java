package planningpet.pet.view;

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
}
