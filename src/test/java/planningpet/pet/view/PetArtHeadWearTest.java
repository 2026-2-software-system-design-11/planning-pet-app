package planningpet.pet.view;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import planningpet.common.view.Snap;
import planningpet.pet.model.PetKind;
import planningpet.pet.model.WearId;

/** 펫이 입은 머리 장신구. 번호는 docs/porting/pet-art.md 6절. 기준 그림은 proto/pet-hat-*.png. */
class PetArtHeadWearTest {
    private static final PetKind[] KINDS = {PetKind.CAT, PetKind.DOG};
    private static final WearId[] ACCESSORIES = {WearId.FLOWER_PIN, WearId.RIBBON_PIN, WearId.GLASSES};
    private static final WearId[] HATS = {WearId.STRAW_HAT, WearId.BERET, WearId.CROWN};
    private static final Set<WearId> STRAW = Set.of(WearId.STRAW_HAT);

    /** 칸 260x240마다 장신구 하나를 입은 머리를 (130, 140)에 선 굵기 6, 1배, 움직임 시각 0으로 그린다. */
    private static BufferedImage heads(PetKind kind, WearId... wears) {
        BufferedImage img = new BufferedImage(260 * wears.length, 240, BufferedImage.TYPE_INT_ARGB);
        for (int i = 0; i < wears.length; i++) {
            Graphics2D g = img.createGraphics();
            PetPaintBase.hints(g);
            new PetPaintBody(g, kind, Set.of(wears[i]), 0).head(130 + 260 * i, 140, 0);
            g.dispose();
        }
        return img;
    }

    private static int cell(int size) {
        return (int) Math.ceil(size * 1.3) + 4;
    }

    /** 묶음마다 한 줄, 크기마다 한 변 cell(크기)인 칸 가운데에 얼굴을 그린다. 2절의 얼굴 칸 배치를 줄로 쌓는다. */
    private static BufferedImage faces(PetKind kind, List<Set<WearId>> wears, int... sizes) {
        int w = Arrays.stream(sizes).map(PetArtHeadWearTest::cell).sum();
        int h = Arrays.stream(sizes).map(PetArtHeadWearTest::cell).max().orElse(0);
        BufferedImage img = new BufferedImage(w, h * wears.size(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        for (int r = 0; r < wears.size(); r++) {
            int x = 0;
            for (int s : sizes) {
                PetArt.drawFace(g, kind, wears.get(r), x + cell(s) / 2.0, h * r + h / 2.0, s);
                x += cell(s);
            }
        }
        g.dispose();
        return img;
    }

    @Test
    void eHat1AccessoryHeads() throws IOException {
        for (PetKind kind : KINDS) {
            Snap.assertLooksLikePrototype("pet-hat-heads-acc-" + kind.name().toLowerCase(), heads(kind, ACCESSORIES));
        }
    }

    @Test
    void eHat2AccessoryFaceSizes() throws IOException {
        for (PetKind kind : KINDS) {
            BufferedImage img = faces(kind, Arrays.stream(ACCESSORIES).map(Set::of).toList(), 30, 76, 230);
            Snap.assertLooksLikePrototype("pet-hat-faces-acc-" + kind.name().toLowerCase(), img);
        }
    }

    @Test
    void eHat1HatHeads() throws IOException {
        for (PetKind kind : KINDS) {
            Snap.assertLooksLikePrototype("pet-hat-heads-hat-" + kind.name().toLowerCase(), heads(kind, HATS));
        }
    }

    @Test
    void eHat2HatFaceSizes() throws IOException {
        for (PetKind kind : KINDS) {
            BufferedImage img = faces(kind, Arrays.stream(HATS).map(Set::of).toList(), 30, 76, 230);
            Snap.assertLooksLikePrototype("pet-hat-faces-hat-" + kind.name().toLowerCase(), img);
        }
    }

    @Test
    void eHat3AllSix() throws IOException {
        for (PetKind kind : KINDS) {
            Snap.assertLooksLikePrototype("pet-hat-many-" + kind.name().toLowerCase(),
                    faces(kind, List.of(EnumSet.range(WearId.FLOWER_PIN, WearId.CROWN)), 230));
        }
    }

    @Test
    void eHat4PinUnderHat() throws IOException {
        List<Set<WearId>> pinHat = new ArrayList<>();
        for (WearId hat : HATS) {
            pinHat.add(Set.of(WearId.FLOWER_PIN, hat));
            pinHat.add(Set.of(WearId.RIBBON_PIN, hat));
        }
        for (PetKind kind : KINDS) {
            Snap.assertLooksLikePrototype("pet-hat-pinhat-" + kind.name().toLowerCase(), faces(kind, pinHat, 230));
        }
    }

    @Test
    void eHat5CardSit() throws IOException {
        List<Set<WearId>> sit = new ArrayList<>();
        EnumSet.range(WearId.FLOWER_PIN, WearId.CROWN).forEach(w -> sit.add(Set.of(w)));
        sit.add(Set.of(WearId.BASIC_NECKLACE, WearId.FLOWER_PIN, WearId.STRAW_HAT));
        for (PetKind kind : KINDS) {
            Snap.assertLooksLikePrototype("pet-hat-sit-" + kind.name().toLowerCase(),
                    PetArtWearTest.row(kind, sit, new double[] {0, 99}));
        }
    }

    @Test
    void eHat6CardTimes() throws IOException {
        for (PetKind kind : KINDS) {
            Snap.assertLooksLikePrototype("pet-hat-card-" + kind.name().toLowerCase(), PetArtWearTest.row(kind,
                    List.of(STRAW), new double[] {0, 99}, new double[] {1.3, 99}, new double[] {1e6, 99},
                    new double[] {-1, 99}, new double[] {0, 0}, new double[] {0, 0.2}));
        }
    }

    @Test
    void eHat7Sizes() throws IOException {
        for (PetKind kind : KINDS) {
            String n = kind.name().toLowerCase();
            Snap.assertLooksLikePrototype("pet-hat-sizes-" + n, PetArtSitTest.sizes(kind, STRAW, 0, 1, 7, 48, 140, 320));
            Snap.assertLooksLikePrototype("pet-hat-neg140-" + n, PetArtWearTest.one(kind, STRAW, 80, 10, -140));
        }
    }

    /** 크기 320과 140의 앉은 펫을 (400, 600)에, 크기 230의 얼굴을 (300, 300)에 그려 잰 칠한 칸. */
    private static String boxes(PetKind kind, Set<WearId> wear) {
        StringBuilder s = new StringBuilder();
        for (double size : new double[] {320, 140}) {
            BufferedImage img = new BufferedImage(800, 800, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            PetArt.drawAt(g, kind, wear, 0, 99, 400, 600, size);
            g.dispose();
            s.append(PetArtTest.paintedBox(img, 400, 600)).append(" | ");
        }
        BufferedImage img = new BufferedImage(600, 600, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        PetArt.drawFace(g, kind, wear, 300, 300, 230);
        g.dispose();
        return s.append(PetArtTest.paintedBox(img, 300, 300)).toString();
    }

    @Test
    void eHat8PaintedBox() {
        int[][] dogTop = {{-249, -247, -247, -274, -277, -270}, {-109, -108, -108, -120, -121, -118},
            {-82, -81, -81, -108, -111, -104}};
        List<WearId> six = List.copyOf(EnumSet.range(WearId.FLOWER_PIN, WearId.CROWN));
        for (int i = 0; i < six.size(); i++) {
            Set<WearId> w = Set.of(six.get(i));
            assertEquals("-147 -282 125 7 | -64 -124 54 3 | -126 -115 125 72", boxes(PetKind.CAT, w), "CAT " + w);
            assertEquals("-116 " + dogTop[0][i] + " 110 5 | -51 " + dogTop[1][i] + " 48 2 | -117 " + dogTop[2][i]
                    + " 109 72", boxes(PetKind.DOG, w), "DOG " + w);
        }
    }
}
