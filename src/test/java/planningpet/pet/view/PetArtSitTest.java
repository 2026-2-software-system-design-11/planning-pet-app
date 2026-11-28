package planningpet.pet.view;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import planningpet.common.view.Snap;
import planningpet.pet.model.PetKind;

/** 앉은 펫(drawAt)과 그 몸 부품. 번호는 docs/porting/pet-art.md 4절. 기준 그림은 proto/pet-sit-*.png. */
class PetArtSitTest {
    private static final PetKind[] KINDS = {PetKind.CAT, PetKind.DOG};

    private static String name(PetKind kind) {
        return kind.name().toLowerCase();
    }

    /** 움직임 시각 0의 몸 부품을 선 굵기 6, 1배로(상자 좌표 그대로) 그린다. 기준 그림과 같은 배치다. */
    private static PetPaintBody part(BufferedImage img, PetKind kind) {
        Graphics2D g = img.createGraphics();
        PetPaintBase.hints(g);
        return new PetPaintBody(g, kind, 0);
    }

    @Test
    void eSit1TailAndPaw() throws IOException {
        for (PetKind kind : KINDS) {
            BufferedImage tail = new BufferedImage(320, 320, BufferedImage.TYPE_INT_ARGB);
            PetPaintBody p = part(tail, kind);
            p.tail();
            p.g.dispose();
            Snap.assertLooksLikePrototype("pet-sit-tail-" + name(kind), tail);
            BufferedImage paw = new BufferedImage(80, 60, BufferedImage.TYPE_INT_ARGB);
            p = part(paw, kind);
            p.g.translate(40, 40);
            p.paw(0, 0);
            p.g.dispose();
            Snap.assertLooksLikePrototype("pet-sit-paw-" + name(kind), paw);
        }
    }
}
