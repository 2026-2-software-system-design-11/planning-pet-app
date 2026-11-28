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
    void eSit1Parts() throws IOException {
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
            BufferedImage whole = new BufferedImage(320, 320, BufferedImage.TYPE_INT_ARGB);
            p = part(whole, kind);
            p.sit();
            p.g.dispose();
            Snap.assertLooksLikePrototype("pet-sit-1x-" + name(kind), whole);
        }
    }

    private static int cell(double size) {
        return (int) Math.ceil(Math.abs(size)) + 8;
    }

    /** 크기마다 한 변이 cell(크기)인 칸을 한 줄로 두고 칸 가운데, 그림 아래에서 4 위를 바닥으로 그린다. 기준 그림과 같은 배치다. */
    private static BufferedImage sizes(PetKind kind, double... sizes) {
        int w = 0;
        int h = 0;
        for (double s : sizes) {
            w += cell(s);
            h = Math.max(h, cell(s));
        }
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        int x = 0;
        for (double s : sizes) {
            PetArt.drawAt(g, kind, 0, 99, x + cell(s) / 2.0, h - 4, s);
            x += cell(s);
        }
        g.dispose();
        return img;
    }

    /** 160x160 그림의 (x, ground)에 크기 size로 그린다. */
    private static BufferedImage one(PetKind kind, double x, double ground, double size) {
        BufferedImage img = new BufferedImage(160, 160, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        PetArt.drawAt(g, kind, 0, 99, x, ground, size);
        g.dispose();
        return img;
    }

    @Test
    void eSit2SizesAndPlaces() throws IOException {
        for (PetKind kind : KINDS) {
            Snap.assertLooksLikePrototype("pet-sit-sizes-" + name(kind), sizes(kind, 0, 1, 7, 48, 140, 320, 141.5));
        }
        Snap.assertLooksLikePrototype("pet-sit-neg140", one(PetKind.CAT, 80, 10, -140));
        Snap.assertLooksLikePrototype("pet-sit-moved", one(PetKind.DOG, 77.5, 150.25, 140));
    }

    /** 크기 140의 펫을 값마다 148x148 칸에 한 줄로 그린다. times가 참이면 값은 t(since 99), 아니면 since(t 0)다. */
    private static BufferedImage row(PetKind kind, boolean times, double... values) {
        BufferedImage img = new BufferedImage(148 * values.length, 148, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        for (int i = 0; i < values.length; i++) {
            PetArt.drawAt(g, kind, times ? values[i] : 0, times ? 99 : values[i], 148 * i + 74, 144, 140);
        }
        g.dispose();
        return img;
    }

    @Test
    void eSit3Times() throws IOException {
        for (PetKind kind : KINDS) {
            Snap.assertLooksLikePrototype("pet-sit-times-" + name(kind), row(kind, true, 0, 0.25, 0.6, 1.1, 1.3, 2.0, 1e6, -1));
        }
    }

    @Test
    void eSit4Since() throws IOException {
        for (PetKind kind : KINDS) {
            Snap.assertLooksLikePrototype("pet-sit-since-" + name(kind), row(kind, false, 0, 0.1, 0.2, 0.3499, 0.35, 99, -1));
        }
    }
}
