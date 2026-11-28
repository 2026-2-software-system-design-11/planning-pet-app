package planningpet.pet.view;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Set;
import org.junit.jupiter.api.Test;
import planningpet.common.view.Snap;
import planningpet.pet.model.PetKind;
import planningpet.pet.model.WearId;

/** 펫이 입은 옷. 번호는 docs/porting/pet-art.md 7절. 기준 그림은 proto/pet-clo-*.png. */
class PetArtClothesTest {
    private static final PetKind[] KINDS = {PetKind.CAT, PetKind.DOG};

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
}
