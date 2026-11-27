package planningpet.pet.view;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import planningpet.common.view.Snap;

/** 펫 얼굴과 로고. 번호는 docs/porting/pet-art.md 2절. 기준 그림은 proto/pet-*.png. */
class PetArtTest {
    /** 부품은 240x200 그림의 (120, 110)에 1배로, 선 굵기 line으로 그린다. 기준 그림과 같은 배치다. */
    private static PetPaintHead part(BufferedImage img, float line) {
        Graphics2D g = img.createGraphics();
        PetPaintBase.hints(g);
        g.translate(120, 110);
        PetPaintHead p = new PetPaintHead(g);
        p.line = line;
        return p;
    }

    @Test
    void eFace6Ears() throws IOException {
        BufferedImage img = new BufferedImage(240, 200, BufferedImage.TYPE_INT_ARGB);
        PetPaintHead p = part(img, 6f);
        p.ear(-1);
        p.ear(1);
        p.g.dispose();
        Snap.assertLooksLikePrototype("pet-ears", img);
    }

    @Test
    void eFace6FaceParts() throws IOException {
        for (float line : new float[] {6f, 9f}) {
            BufferedImage img = new BufferedImage(240, 200, BufferedImage.TYPE_INT_ARGB);
            PetPaintHead p = part(img, line);
            p.faceParts();
            p.g.dispose();
            Snap.assertLooksLikePrototype("pet-face-parts-" + (int) line, img);
        }
    }

    @Test
    void eFace6Head() throws IOException {
        BufferedImage img = new BufferedImage(260, 200, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        PetPaintBase.hints(g);
        g.translate(130, 120);
        new PetPaintHead(g).head();
        g.dispose();
        Snap.assertLooksLikePrototype("pet-head", img);
    }
}
