package planningpet.pet.view;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
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
}
