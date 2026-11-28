package planningpet.pet.view;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import planningpet.common.view.Snap;
import planningpet.pet.model.PetKind;

/** 펫이 입은 목걸이. 번호는 docs/porting/pet-art.md 5절. 기준 그림은 proto/pet-wear-*.png. */
class PetArtWearTest {
    @Test
    void eWear1Shapes() throws IOException {
        BufferedImage img = new BufferedImage(260, 80, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        PetPaintBase.hints(g);
        PetPaintBody p = new PetPaintBody(g, PetKind.CAT, 0);
        p.bow(40, 40, 1.0, PetPaintBase.PINK_LIGHT);
        p.bow(110, 40, 1.4, PetPaintBase.PINK);
        p.filled(PetPaintWear.star(170, 40, 15, 7), PetPaintBase.GOLD);
        p.filled(PetPaintWear.crown(220, 40, 30, 22), PetPaintBase.GOLD);
        p.g.dispose();
        Snap.assertLooksLikePrototype("pet-wear-shapes", img);
    }
}
