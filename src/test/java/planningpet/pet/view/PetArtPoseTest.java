package planningpet.pet.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import planningpet.common.view.Snap;
import planningpet.pet.model.PetKind;
import planningpet.pet.model.Pose;
import planningpet.pet.model.WearId;

/** 자세(D-50)와 모습 단계 크기, 얼굴 그림. 번호는 docs/porting/character.md S8b. */
class PetArtPoseTest {
    private static final PetKind[] KINDS = {PetKind.CAT, PetKind.DOG};

    /** 자세마다의 높이 비율(Pose 차례). */
    @Test
    void ePose1HeightRatio() {
        assertEquals(List.of(0.88, 0.51, 0.62, 0.83, 0.92), Arrays.stream(Pose.values()).map(p -> PetArt.heightRatio(PetKind.CAT, p)).toList());
        assertEquals(List.of(0.77, 0.50, 0.47, 0.73, 0.86), Arrays.stream(Pose.values()).map(p -> PetArt.heightRatio(PetKind.DOG, p)).toList());
    }

    @Test
    void ePose2StageScale() {
        assertEquals(List.of(0.72, 0.72, 0.86, 1.0, 1.0), IntStream.range(0, 5).mapToObj(PetArt::stageScale).toList());
    }

    /** 밥그릇은 사료 윗면 타원(가운데 (160, 268), 반지름 58과 12)과 아래 끝 y=306의 그릇에 굵기 6의 외곽선이라 바닥 아래 8줄까지 칠한다. */
    @Test
    void ePose3BowlBelowGround() {
        BufferedImage img = new BufferedImage(320, 340, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        PetPaintBase.hints(g);
        new PetPaintBody(g, PetKind.CAT, Set.of(), 0).bowl();
        g.dispose();
        assertEquals("-61 -47 60 8", PetArtTest.paintedBox(img, 160, 300));
    }

    /** 음표만 320x320 그림에 상자 좌표 그대로 그린다. */
    private static BufferedImage notes(double t) {
        BufferedImage img = new BufferedImage(320, 320, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        PetPaintBase.hints(g);
        new PetPaintBody(g, PetKind.CAT, Set.of(), t).notes();
        g.dispose();
        return img;
    }

    /** 2초 주기의 자리 p에서 음표 가운데는 (252 + 24i + 6 sin 2πp, 86 - 56p)이고, 처음 4분의 1에 나타나 마지막 3분의 1에 사라진다. */
    @Test
    void ePose4Notes() {
        BufferedImage at0 = notes(0);
        assertEquals(0, at0.getRGB(252, 86) >>> 24);
        assertEquals(PetPaintBase.NOTE.getRGB(), at0.getRGB(276, 58));
        BufferedImage at1 = notes(1);
        assertEquals(PetPaintBase.NOTE.getRGB(), at1.getRGB(252, 58));
        assertEquals(0, at1.getRGB(276, 86) >>> 24);
        BufferedImage at15 = notes(1.5);
        assertEquals(191, at15.getRGB(246, 44) >>> 24);
        assertEquals(PetPaintBase.NOTE.getRGB(), at15.getRGB(282, 72));
    }

    private static long count(BufferedImage img, Color c) {
        return Arrays.stream(Snap.pixels(img)).filter(p -> p == c.getRGB()).count();
    }

    /** 얼굴 그림은 96x96이고 머리 장신구는 그리고 목걸이는 그리지 않는다. */
    @Test
    void ePose6Face() {
        for (PetKind kind : KINDS) {
            BufferedImage face = PetArt.face(kind, Set.of(WearId.STRAW_HAT, WearId.BASIC_NECKLACE));
            assertEquals("96x96", face.getWidth() + "x" + face.getHeight());
            assertTrue(count(face, PetPaintBase.STRAW) > 0, kind.name());
            assertEquals(0, count(face, PetPaintBase.TEAL), kind.name());
            assertEquals(0, count(PetArt.face(kind, Set.of()), PetPaintBase.STRAW), kind.name());
        }
    }
}
