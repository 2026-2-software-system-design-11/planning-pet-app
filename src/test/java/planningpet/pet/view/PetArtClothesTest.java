package planningpet.pet.view;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import planningpet.common.view.Snap;
import planningpet.pet.model.PetKind;
import planningpet.pet.model.WearId;

/** 펫이 입은 옷. 번호는 docs/porting/pet-art.md 7절. 기준 그림은 proto/pet-clo-*.png. */
class PetArtClothesTest {
    private static final PetKind[] KINDS = {PetKind.CAT, PetKind.DOG};
    private static final WearId[] CLOTHES = {WearId.SCARF, WearId.VEST, WearId.CAPE};
    private static final Set<WearId> ALL3 = EnumSet.range(WearId.SCARF, WearId.CAPE);

    /** 1배 앉은 펫을 상자 좌표 그대로 선 굵기 6, 움직임 시각 0으로 그린다. 망토 아랫단이 바닥 아래로 내려와 높이를 340으로 둔다. */
    private static BufferedImage oneX(PetKind kind, WearId wear) {
        BufferedImage img = new BufferedImage(320, 340, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        PetPaintBase.hints(g);
        new PetPaintBody(g, kind, Set.of(wear), 0).sit();
        g.dispose();
        return img;
    }

    private static void assertOneX(String name, WearId wear) throws IOException {
        for (PetKind kind : KINDS) {
            Snap.assertLooksLikePrototype("pet-clo-" + name + "-" + kind.name().toLowerCase(), oneX(kind, wear));
        }
    }

    @Test
    void eClo1Scarf() throws IOException {
        assertOneX("scarf", WearId.SCARF);
    }

    @Test
    void eClo1Vest() throws IOException {
        assertOneX("vest", WearId.VEST);
    }

    @Test
    void eClo1Cape() throws IOException {
        assertOneX("cape", WearId.CAPE);
    }

    @Test
    void eClo2CardWithCollarsAndHat() throws IOException {
        List<Set<WearId>> card = List.of(Set.of(WearId.SCARF), Set.of(WearId.VEST), Set.of(WearId.CAPE),
                Set.of(WearId.SCARF, WearId.BASIC_NECKLACE), Set.of(WearId.VEST, WearId.STAR_NECKLACE),
                Set.of(WearId.CAPE, WearId.BASIC_NECKLACE), ALL3,
                Set.of(WearId.SCARF, WearId.VEST, WearId.CAPE, WearId.CROWN_NECKLACE, WearId.STRAW_HAT));
        for (PetKind kind : KINDS) {
            Snap.assertLooksLikePrototype("pet-clo-card-" + kind.name().toLowerCase(),
                    PetArtWearTest.row(kind, card, new double[] {0, 99}));
        }
    }

    @Test
    void eClo3CardTimes() throws IOException {
        for (PetKind kind : KINDS) {
            Snap.assertLooksLikePrototype("pet-clo-times-" + kind.name().toLowerCase(), PetArtWearTest.row(kind,
                    List.of(ALL3), new double[] {0, 99}, new double[] {1.3, 99}, new double[] {1e6, 99},
                    new double[] {-1, 99}, new double[] {0, 0}, new double[] {0, 0.2}));
        }
    }

    @Test
    void eClo4Sizes() throws IOException {
        for (PetKind kind : KINDS) {
            String n = kind.name().toLowerCase();
            Snap.assertLooksLikePrototype("pet-clo-sizes-" + n, PetArtSitTest.sizes(kind, ALL3, 0, 1, 7, 48, 140, 320));
            Snap.assertLooksLikePrototype("pet-clo-neg140-" + n, PetArtWearTest.one(kind, ALL3, 80, 10, -140));
        }
    }

    @Test
    void eClo5FaceHasNoClothes() {
        for (PetKind kind : KINDS) {
            for (WearId c : CLOTHES) {
                for (int size : new int[] {30, 76, 230}) {
                    assertArrayEquals(PetArtWearTest.face(kind, Set.of(), size),
                            PetArtWearTest.face(kind, Set.of(c), size), kind + " " + c + " " + size);
                }
            }
        }
    }

    /** 크기 size의 앉은 펫을 800x800 그림의 (400, 600)에 그려 잰 칠한 칸. */
    private static String box(PetKind kind, Set<WearId> wear, double size) {
        BufferedImage img = new BufferedImage(800, 800, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        PetArt.drawAt(g, kind, wear, 0, 99, 400, 600, size);
        g.dispose();
        return PetArtTest.paintedBox(img, 400, 600);
    }

    @Test
    void eClo6PaintedBox() {
        String[][] plain = {{"-147 -282 125 7", "-64 -124 54 3"}, {"-116 -247 110 5", "-51 -108 48 2"}};
        String[][] cape = {{"-147 -282 125 20", "-64 -124 54 9"}, {"-116 -247 114 20", "-51 -108 50 9"}};
        double[] sizes = {320, 140};
        for (int k = 0; k < KINDS.length; k++) {
            for (int i = 0; i < sizes.length; i++) {
                for (Set<WearId> w : List.of(Set.of(WearId.SCARF), Set.of(WearId.VEST), Set.of(WearId.CAPE), ALL3)) {
                    String want = w.contains(WearId.CAPE) ? cape[k][i] : plain[k][i];
                    assertEquals(want, box(KINDS[k], w, sizes[i]), KINDS[k] + " " + w + " " + sizes[i]);
                }
            }
        }
    }
}
