package planningpet.pet.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import planningpet.common.view.Snap;
import planningpet.pet.model.PetKind;

/** 펫 얼굴과 로고. 번호는 docs/porting/pet-art.md 2절과 3.2절. 기준 그림은 proto/pet-*.png. */
class PetArtTest {
    /** 부품은 240x200 그림의 (120, 110)에 1배로, 선 굵기 line으로 그린다. 기준 그림과 같은 배치다. */
    private static PetPaintHead part(BufferedImage img, PetKind kind, float line) {
        Graphics2D g = img.createGraphics();
        PetPaintBase.hints(g);
        g.translate(120, 110);
        PetPaintHead p = new PetPaintHead(g, kind);
        p.line = line;
        return p;
    }

    private static int cell(int size) {
        return (int) Math.ceil(Math.abs(size) * 1.3) + 4;
    }

    /** 크기마다 한 변이 cell(크기)인 칸을 한 줄로 두고 칸 가운데에 얼굴을 그린다. 기준 그림과 같은 배치다. */
    private static BufferedImage faces(int... sizes) {
        int w = 0;
        int h = 0;
        for (int s : sizes) {
            w += cell(s);
            h = Math.max(h, cell(s));
        }
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        int x = 0;
        for (int s : sizes) {
            PetArt.drawFace(g, x + cell(s) / 2.0, cell(s) / 2.0, s);
            x += cell(s);
        }
        g.dispose();
        return img;
    }

    @Test
    void eFace1LineWidthSizes() throws IOException {
        Snap.assertLooksLikePrototype("pet-face-sizes", faces(30, 47, 48, 89, 90, 112, 230));
    }

    @Test
    void eFace2VerySmall() throws IOException {
        Snap.assertLooksLikePrototype("pet-face-small", faces(0, 1, 7));
    }

    @Test
    void eFace3NegativeSize() throws IOException {
        BufferedImage img = new BufferedImage(80, 80, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        PetArt.drawFace(g, 40, 40, -48);
        g.dispose();
        Snap.assertLooksLikePrototype("pet-face-neg48", img);
    }

    @Test
    void eFace4PaintedBox() {
        BufferedImage img = new BufferedImage(600, 600, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        PetArt.drawFace(g, 300, 300, 230);
        g.dispose();
        int[] box = {600, 600, -1, -1};
        for (int y = 0; y < 600; y++) {
            for (int x = 0; x < 600; x++) {
                if (img.getRGB(x, y) >>> 24 != 0) {
                    box = new int[] {Math.min(box[0], x), Math.min(box[1], y), Math.max(box[2], x), Math.max(box[3], y)};
                }
            }
        }
        assertEquals("-126 -115 125 72", (box[0] - 300) + " " + (box[1] - 300) + " " + (box[2] - 300) + " " + (box[3] - 300));
    }

    /** w x h 그림의 (x, y)에 bw x bh 상자로 로고를 그린다. */
    private static BufferedImage logo(int w, int h, double x, double y, double bw, double bh) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        PetArt.drawLogo(g, x, y, bw, bh);
        g.dispose();
        return img;
    }

    @Test
    void eLogo1To5Boxes() throws IOException {
        Snap.assertLooksLikePrototype("pet-logo-side", logo(124, 64, 18, 0, 88, 64));
        Snap.assertLooksLikePrototype("pet-logo-pick", logo(74, 51, 0, 0, 74, 51));
        Snap.assertLooksLikePrototype("pet-logo-wide", logo(200, 64, 0, 0, 200, 64));
        Snap.assertLooksLikePrototype("pet-logo-tall", logo(88, 200, 0, 0, 88, 200));
        Snap.assertLooksLikePrototype("pet-logo-0x0", logo(10, 10, 5, 5, 0, 0));
        Snap.assertLooksLikePrototype("pet-logo-moved", logo(100, 90, 7.5, 13.25, 80, 60));
    }

    @Test
    void eFace5CallerStateKept() {
        Graphics2D g = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB).createGraphics();
        g.setColor(Color.RED);
        BasicStroke stroke = new BasicStroke(5);
        g.setStroke(stroke);
        g.translate(3, 4);
        AffineTransform before = g.getTransform();
        Object aa = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        PetArt.drawFace(g, 40, 40, 60);
        assertEquals(Color.RED, g.getColor());
        assertSame(stroke, g.getStroke());
        assertEquals(before, g.getTransform());
        assertEquals(aa, g.getRenderingHint(RenderingHints.KEY_ANTIALIASING));
        assertEquals(null, g.getClip());
        g.dispose();
    }

    @Test
    void eFace6Ears() throws IOException {
        BufferedImage img = new BufferedImage(240, 200, BufferedImage.TYPE_INT_ARGB);
        PetPaintHead p = part(img, PetKind.CAT, 6f);
        p.ear(-1);
        p.ear(1);
        p.g.dispose();
        Snap.assertLooksLikePrototype("pet-ears", img);
    }

    @Test
    void eFace6FaceParts() throws IOException {
        for (float line : new float[] {6f, 9f}) {
            BufferedImage img = new BufferedImage(240, 200, BufferedImage.TYPE_INT_ARGB);
            PetPaintHead p = part(img, PetKind.CAT, line);
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
        new PetPaintHead(g, PetKind.CAT).head();
        g.dispose();
        Snap.assertLooksLikePrototype("pet-head", img);
    }

    @Test
    void eDog4Ears() throws IOException {
        BufferedImage img = new BufferedImage(240, 240, BufferedImage.TYPE_INT_ARGB);
        PetPaintHead p = part(img, PetKind.DOG, 6f);
        p.dogEar(-1);
        p.dogEar(1);
        p.g.dispose();
        Snap.assertLooksLikePrototype("pet-dog-ears", img);
    }

    @Test
    void eDog4FaceParts() throws IOException {
        for (float line : new float[] {6f, 9f}) {
            BufferedImage img = new BufferedImage(240, 200, BufferedImage.TYPE_INT_ARGB);
            PetPaintHead p = part(img, PetKind.DOG, line);
            p.faceParts();
            p.g.dispose();
            Snap.assertLooksLikePrototype("pet-dog-face-parts-" + (int) line, img);
        }
    }

    @Test
    void eDog4Head() throws IOException {
        BufferedImage img = new BufferedImage(260, 240, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        PetPaintBase.hints(g);
        g.translate(130, 120);
        new PetPaintHead(g, PetKind.DOG).head();
        g.dispose();
        Snap.assertLooksLikePrototype("pet-dog-head", img);
    }
}
