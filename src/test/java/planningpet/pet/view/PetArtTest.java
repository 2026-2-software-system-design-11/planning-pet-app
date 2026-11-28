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
import java.util.Set;
import org.junit.jupiter.api.Test;
import planningpet.common.view.Snap;
import planningpet.pet.model.PetKind;

/** 펫 얼굴과 로고. 번호는 docs/porting/pet-art.md 2절과 3절. 기준 그림은 proto/pet-*.png. */
class PetArtTest {
    /** 부품은 240x200 그림의 (120, 110)에 1배로, 선 굵기 line으로 그린다. 기준 그림과 같은 배치다. */
    private static PetPaintHead part(BufferedImage img, PetKind kind, float line) {
        Graphics2D g = img.createGraphics();
        PetPaintBase.hints(g);
        g.translate(120, 110);
        PetPaintHead p = new PetPaintHead(g, kind, Set.of(), 0);
        p.line = line;
        return p;
    }

    private static int cell(int size) {
        return (int) Math.ceil(Math.abs(size) * 1.3) + 4;
    }

    /** 크기마다 한 변이 cell(크기)인 칸을 한 줄로 두고 칸 가운데에 얼굴을 그린다. 기준 그림과 같은 배치다. */
    private static BufferedImage faces(PetKind kind, int... sizes) {
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
            PetArt.drawFace(g, kind, Set.of(), x + cell(s) / 2.0, cell(s) / 2.0, s);
            x += cell(s);
        }
        g.dispose();
        return img;
    }

    @Test
    void eFace1LineWidthSizes() throws IOException {
        Snap.assertLooksLikePrototype("pet-face-sizes", faces(PetKind.CAT, 30, 47, 48, 89, 90, 112, 230));
    }

    @Test
    void eFace2VerySmall() throws IOException {
        Snap.assertLooksLikePrototype("pet-face-small", faces(PetKind.CAT, 0, 1, 7));
    }

    @Test
    void eFace3NegativeSize() throws IOException {
        BufferedImage img = new BufferedImage(80, 80, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        PetArt.drawFace(g, PetKind.CAT, Set.of(), 40, 40, -48);
        g.dispose();
        Snap.assertLooksLikePrototype("pet-face-neg48", img);
    }

    /** 칠한(알파가 0이 아닌) 픽셀을 모두 담는 상자를 (ox, oy)에서 잰 "왼쪽 위 오른쪽 아래". */
    static String paintedBox(BufferedImage img, int ox, int oy) {
        int[] box = {img.getWidth(), img.getHeight(), -1, -1};
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                if (img.getRGB(x, y) >>> 24 != 0) {
                    box = new int[] {Math.min(box[0], x), Math.min(box[1], y), Math.max(box[2], x), Math.max(box[3], y)};
                }
            }
        }
        return (box[0] - ox) + " " + (box[1] - oy) + " " + (box[2] - ox) + " " + (box[3] - oy);
    }

    /** 크기 230 얼굴을 (300, 300)에 그려 칠한 픽셀의 상자를 가운데에서 잰다. */
    private static String face230Box(PetKind kind) {
        BufferedImage img = new BufferedImage(600, 600, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        PetArt.drawFace(g, kind, Set.of(), 300, 300, 230);
        g.dispose();
        return paintedBox(img, 300, 300);
    }

    @Test
    void eFace4PaintedBox() {
        assertEquals("-126 -115 125 72", face230Box(PetKind.CAT));
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
        PetArt.drawFace(g, PetKind.CAT, Set.of(), 40, 40, 60);
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
        p.ear(-1, 0);
        p.ear(1, 0);
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
        new PetPaintHead(g, PetKind.CAT, Set.of(), 0).head(0, 0, 0);
        g.dispose();
        Snap.assertLooksLikePrototype("pet-head", img);
    }

    @Test
    void eDog1LineWidthSizes() throws IOException {
        Snap.assertLooksLikePrototype("pet-dog-face-sizes", faces(PetKind.DOG, 30, 47, 48, 89, 90, 112, 230));
    }

    @Test
    void eDog2SmallAndNegative() throws IOException {
        Snap.assertLooksLikePrototype("pet-dog-face-small", faces(PetKind.DOG, 0, 1, 7));
        BufferedImage img = new BufferedImage(80, 80, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        PetArt.drawFace(g, PetKind.DOG, Set.of(), 40, 40, -48);
        g.dispose();
        Snap.assertLooksLikePrototype("pet-dog-face-neg48", img);
    }

    @Test
    void eDog3PaintedBox() {
        assertEquals("-117 -81 109 72", face230Box(PetKind.DOG));
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

    /** 260x240 그림의 (130, 120)에 머리를 움직임 시각 t에 rot 기울여 1배로 그린다. */
    private static BufferedImage head(PetKind kind, double t, double rot) {
        BufferedImage img = new BufferedImage(260, 240, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        PetPaintBase.hints(g);
        g.translate(130, 120);
        new PetPaintHead(g, kind, Set.of(), t).head(0, 0, rot);
        g.dispose();
        return img;
    }

    @Test
    void eDog4Head() throws IOException {
        Snap.assertLooksLikePrototype("pet-dog-head", head(PetKind.DOG, 0, 0));
    }

    @Test
    void eHead1TurnedAndMoving() throws IOException {
        Snap.assertLooksLikePrototype("pet-head-turned-cat", head(PetKind.CAT, 1.1, -9));
        Snap.assertLooksLikePrototype("pet-head-turned-dog", head(PetKind.DOG, 0.6, -9));
    }
}
