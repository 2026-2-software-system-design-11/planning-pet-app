package planningpet.pet.view;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import planningpet.common.view.Snap;
import planningpet.pet.model.PetKind;
import planningpet.pet.model.WearId;

/** 펫이 입은 목걸이. 번호는 docs/porting/pet-art.md 5절. 기준 그림은 proto/pet-wear-*.png. */
class PetArtWearTest {
    private static final PetKind[] KINDS = {PetKind.CAT, PetKind.DOG};
    private static final Set<WearId> BASIC = Set.of(WearId.BASIC_NECKLACE);
    private static final WearId[] COLLARS = {
        WearId.BASIC_NECKLACE, WearId.RIBBON_NECKLACE, WearId.STAR_NECKLACE, WearId.CROWN_NECKLACE};

    private static String name(PetKind kind) {
        return kind.name().toLowerCase();
    }

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
            Snap.assertLooksLikePrototype("pet-wear-collars-" + name(kind), img);
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
    void eWear2SitOneX() throws IOException {
        for (PetKind kind : KINDS) {
            BufferedImage img = new BufferedImage(320, 320, BufferedImage.TYPE_INT_ARGB);
            PetPaintBody p = part(img, kind, BASIC, 0, 0);
            p.sit();
            p.g.dispose();
            Snap.assertLooksLikePrototype("pet-wear-sit-1x-" + name(kind), img);
        }
    }

    /** 크기 140의 펫을 칸 148x148에 한 줄로 그린다. 칸 i는 wears와 ts의 i번째(모자라면 마지막) 묶음과 (t, since)다. */
    private static BufferedImage row(PetKind kind, List<Set<WearId>> wears, double[]... ts) {
        int n = Math.max(wears.size(), ts.length);
        BufferedImage img = new BufferedImage(148 * n, 148, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        for (int i = 0; i < n; i++) {
            double[] t = ts[Math.min(i, ts.length - 1)];
            PetArt.drawAt(g, kind, wears.get(Math.min(i, wears.size() - 1)), t[0], t[1], 148 * i + 74, 144, 140);
        }
        g.dispose();
        return img;
    }

    @Test
    void eWear3CardTimes() throws IOException {
        for (PetKind kind : KINDS) {
            Snap.assertLooksLikePrototype("pet-wear-card-" + name(kind), row(kind, List.of(BASIC),
                    new double[] {0, 99}, new double[] {1.3, 99}, new double[] {1e6, 99}, new double[] {-1, 99},
                    new double[] {0, 0}, new double[] {0, 0.2}));
        }
    }

    @Test
    void eWear4Sizes() throws IOException {
        for (PetKind kind : KINDS) {
            BufferedImage sizes = PetArtSitTest.sizes(kind, BASIC, 0, 1, 7, 48, 140, 320);
            Snap.assertLooksLikePrototype("pet-wear-sizes-" + name(kind), sizes);
            Snap.assertLooksLikePrototype("pet-wear-neg140-" + name(kind), one(kind, BASIC, 80, 10, -140));
        }
    }

    @Test
    void eWear5ManyCollars() throws IOException {
        for (PetKind kind : KINDS) {
            List<Set<WearId>> many = List.of(Set.of(COLLARS[0], COLLARS[1]), Set.of(COLLARS[1], COLLARS[2]),
                    Set.of(COLLARS[2], COLLARS[3]), Set.of(COLLARS));
            Snap.assertLooksLikePrototype("pet-wear-many-" + name(kind), row(kind, many, new double[] {0, 99}));
            List<Set<WearId>> first = List.of(Set.of(COLLARS[1]), Set.of(COLLARS[2]), Set.of(COLLARS[3]), Set.of(COLLARS[3]));
            Snap.assertLooksLikePrototype("pet-wear-many-" + name(kind), row(kind, first, new double[] {0, 99}));
        }
    }

    @Test
    void eWear6EmptyAndNull() throws IOException {
        Snap.assertLooksLikePrototype("pet-sit-neg140", one(PetKind.CAT, null, 80, 10, -140));
        Snap.assertLooksLikePrototype("pet-sit-neg140", one(PetKind.CAT, Set.of(), 80, 10, -140));
        Snap.assertLooksLikePrototype("pet-sit-moved", one(PetKind.DOG, null, 77.5, 150.25, 140));
        Snap.assertLooksLikePrototype("pet-sit-moved", one(PetKind.DOG, Set.of(), 77.5, 150.25, 140));
    }
}
