package planningpet.pet.view;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Set;
import org.junit.jupiter.api.Test;
import planningpet.common.view.Snap;
import planningpet.pet.model.PetKind;
import planningpet.pet.model.WearId;

/** 펫이 입은 목걸이. 번호는 docs/porting/pet-art.md 5절. 기준 그림은 proto/pet-wear-*.png. */
class PetArtWearTest {
    @Test
    void eWear1Shapes() throws IOException {
        BufferedImage img = new BufferedImage(260, 80, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        PetPaintBase.hints(g);
        PetPaintBody p = new PetPaintBody(g, PetKind.CAT, Set.of(), 0);
        p.bow(40, 40, 1.0, PetPaintBase.PINK_LIGHT);
        p.bow(110, 40, 1.4, PetPaintBase.PINK);
        p.filled(PetPaintWear.star(170, 40, 15, 7), PetPaintBase.GOLD);
        p.filled(PetPaintWear.crown(220, 40, 30, 22), PetPaintBase.GOLD);
        p.g.dispose();
        Snap.assertLooksLikePrototype("pet-wear-shapes", img);
    }

    /** 160x160 그림의 (x, ground)에 크기 size로 그린다. 4.2절 기준 그림과 같은 배치다. */
    private static BufferedImage one(PetKind kind, Set<WearId> wear, double x, double ground, double size) {
        BufferedImage img = new BufferedImage(160, 160, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        PetArt.drawAt(g, kind, wear, 0, 99, x, ground, size);
        g.dispose();
        return img;
    }

    @Test
    void eWear6EmptyAndNull() throws IOException {
        Snap.assertLooksLikePrototype("pet-sit-neg140", one(PetKind.CAT, null, 80, 10, -140));
        Snap.assertLooksLikePrototype("pet-sit-neg140", one(PetKind.CAT, Set.of(), 80, 10, -140));
        Snap.assertLooksLikePrototype("pet-sit-moved", one(PetKind.DOG, null, 77.5, 150.25, 140));
        Snap.assertLooksLikePrototype("pet-sit-moved", one(PetKind.DOG, Set.of(), 77.5, 150.25, 140));
    }
}
