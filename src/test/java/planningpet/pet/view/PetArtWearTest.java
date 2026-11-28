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
    private static final PetKind[] KINDS = {PetKind.CAT, PetKind.DOG};
    private static final WearId[] COLLARS = {
        WearId.BASIC_NECKLACE, WearId.RIBBON_NECKLACE, WearId.STAR_NECKLACE, WearId.CROWN_NECKLACE};

    /** 움직임 시각 0의 부품을 선 굵기 6, 1배로 (x, y)에 그린다. 기준 그림과 같은 배치다. */
    private static PetPaintBody part(BufferedImage img, PetKind kind, Set<WearId> wear, double x, double y) {
        Graphics2D g = img.createGraphics();
        PetPaintBase.hints(g);
        g.translate(x, y);
        return new PetPaintBody(g, kind, wear, 0);
    }

    @Test
    void eWear1Shapes() throws IOException {
        BufferedImage img = new BufferedImage(260, 80, BufferedImage.TYPE_INT_ARGB);
        PetPaintBody p = part(img, PetKind.CAT, Set.of(), 0, 0);
        p.bow(40, 40, 1.0, PetPaintBase.PINK_LIGHT);
        p.bow(110, 40, 1.4, PetPaintBase.PINK);
        p.filled(PetPaintWear.star(170, 40, 15, 7), PetPaintBase.GOLD);
        p.filled(PetPaintWear.crown(220, 40, 30, 22), PetPaintBase.GOLD);
        p.g.dispose();
        Snap.assertLooksLikePrototype("pet-wear-shapes", img);
    }

    @Test
    void eWear1Collars() throws IOException {
        for (PetKind kind : KINDS) {
            BufferedImage img = new BufferedImage(560, 90, BufferedImage.TYPE_INT_ARGB);
            for (int i = 0; i < COLLARS.length; i++) {
                PetPaintBody p = part(img, kind, Set.of(COLLARS[i]), 70 + 140 * i, 30);
                p.neckWear(0, 0, 58);
                p.g.dispose();
            }
            Snap.assertLooksLikePrototype("pet-wear-collars-" + kind.name().toLowerCase(), img);
        }
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
