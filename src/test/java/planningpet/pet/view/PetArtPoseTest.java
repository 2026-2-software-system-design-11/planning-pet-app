package planningpet.pet.view;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
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

/** 자세(D-50)와 모습 단계 크기, 얼굴 그림. 번호는 docs/porting/character.md S8b, S8c. */
class PetArtPoseTest {
    private static final PetKind[] KINDS = {PetKind.CAT, PetKind.DOG};
    private static final Set<WearId> COLLAR = Set.of(WearId.BASIC_NECKLACE);

    /** 320x340 그림에 크기 320(1배), 바닥 y=300, 가운데 x=160으로 시각 t에 그린다. 그림 좌표가 상자 좌표다. */
    private static BufferedImage draw(PetKind kind, Pose pose, Set<WearId> wear, double t) {
        BufferedImage img = new BufferedImage(320, 340, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        PetArt.drawAt(g, kind, pose, wear, t, 99, 160, 300, 320);
        g.dispose();
        return img;
    }

    /** 칠한 픽셀의 상자(왼쪽, 위, 오른쪽, 아래)를 (160, 300)에서 잰다. */
    private static int[] box(PetKind kind, Pose pose, double t) {
        String box = PetArtTest.paintedBox(draw(kind, pose, COLLAR, t), 160, 300);
        return Arrays.stream(box.split(" ")).mapToInt(Integer::parseInt).toArray();
    }

    /** 칠한 위 끝을 상자 320으로 나눠 둘째 자리에서 반올림한 값이 자세의 높이 비율이다(모자 없이 기본 목걸이, 시각 0). Pose 차례의 값도 본다. */
    @Test
    void ePose1HeightRatio() {
        for (PetKind kind : KINDS) {
            for (Pose pose : new Pose[] {Pose.SIT, Pose.EAT, Pose.SING}) {
                assertEquals(Math.round(-box(kind, pose, 0)[1] / 320.0 * 100) / 100.0, PetArt.heightRatio(kind, pose), kind + " " + pose);
            }
        }
        assertEquals(List.of(0.88, 0.51, 0.62, 0.83, 0.92), Arrays.stream(Pose.values()).map(p -> PetArt.heightRatio(PetKind.CAT, p)).toList());
        assertEquals(List.of(0.77, 0.50, 0.47, 0.73, 0.86), Arrays.stream(Pose.values()).map(p -> PetArt.heightRatio(PetKind.DOG, p)).toList());
    }

    @Test
    void ePose2StageScale() {
        assertEquals(List.of(0.72, 0.72, 0.86, 1.0, 1.0), IntStream.range(0, 5).mapToObj(PetArt::stageScale).toList());
    }

    /**
     * 밥그릇은 사료 윗면 타원(가운데 (160, 268), 반지름 58과 12)과 아래 끝 y=306의 그릇에 굵기 6의 외곽선이라 바닥 아래 8줄까지 칠한다.
     * 밥 먹기 자세의 아래 끝도 그릇이다. 앉기의 아래 끝은 고양이 7, 강아지 5다.
     */
    @Test
    void ePose3BowlBelowGround() {
        BufferedImage img = new BufferedImage(320, 340, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        PetPaintBase.hints(g);
        new PetPaintBody(g, PetKind.CAT, Set.of(), 0).bowl();
        g.dispose();
        assertEquals("-61 -47 60 8", PetArtTest.paintedBox(img, 160, 300));
        assertEquals(List.of(7, 8), List.of(box(PetKind.CAT, Pose.SIT, 0)[3], box(PetKind.CAT, Pose.EAT, 0)[3]));
        assertEquals(List.of(5, 8), List.of(box(PetKind.DOG, Pose.SIT, 0)[3], box(PetKind.DOG, Pose.EAT, 0)[3]));
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

    /** 음수 시각도 2초 주기로 본다. 시각 -1은 1과 같다(프로토타입은 알파가 음수라 멈춘다). */
    @Test
    void ePose13NegativeTime() {
        assertArrayEquals(Snap.pixels(notes(1)), Snap.pixels(notes(-1)));
        assertTrue(box(PetKind.DOG, Pose.SING, -0.3)[1] < 0);
    }

    /** x=206 세로줄에서 y 200 아래 처음 나오는 선 색 픽셀(든 앞발의 위 테두리). */
    private static int pawTop(double t) {
        BufferedImage img = draw(PetKind.CAT, Pose.SING, Set.of(), t);
        int y = 200;
        while (img.getRGB(206, y) != PetPaintBase.CAT_INK.getRGB()) {
            y++;
        }
        return y;
    }

    /** 노래의 오른 앞발은 0.8초 주기로 8만큼 오르내린다. 0.8초에는 숨쉬기로 몸이 부푼 만큼 1 높다. */
    @Test
    void ePose5RaisedPaw() {
        assertEquals(List.of(209, 201, 208), List.of(pawTop(0), pawTop(0.4), pawTop(0.8)));
    }

    /** 자는 펫의 목 장신구는 바닥 아래를 잘라 낸다. 잘라 내지 않은 스카프는 바닥 아래까지 내려오지만, 입혀 그려도 바닥 줄부터 아래는 픽셀이 같다. */
    @Test
    void ePose9SleepNeckClipped() {
        BufferedImage alone = new BufferedImage(320, 340, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = alone.createGraphics();
        new PetPaintBody(g, PetKind.CAT, Pose.SLEEP, Set.of(WearId.SCARF), 0).neckWear(116, 280, -6, 46);
        g.dispose();
        assertTrue(Integer.parseInt(PetArtTest.paintedBox(alone, 0, 300).split(" ")[3]) > 0);
        for (PetKind kind : KINDS) {
            int[] bare = Snap.pixels(draw(kind, Pose.SLEEP, Set.of(), 0));
            for (WearId wear : new WearId[] {WearId.SCARF, WearId.CROWN_NECKLACE}) {
                int[] worn = Snap.pixels(draw(kind, Pose.SLEEP, Set.of(wear), 0));
                assertArrayEquals(Arrays.copyOfRange(bare, 320 * 300, bare.length), Arrays.copyOfRange(worn, 320 * 300, worn.length), kind + " " + wear);
            }
        }
    }

    /** Z 셋만 320x320 그림에 상자 좌표 그대로 그린다. */
    private static BufferedImage zzz(double t) {
        BufferedImage img = new BufferedImage(320, 320, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        PetPaintBase.hints(g);
        new PetPaintBody(g, PetKind.CAT, Pose.SLEEP, Set.of(), t).zzz();
        g.dispose();
        return img;
    }

    /**
     * 2.6초 주기의 자리 p(뒤의 Z는 3분의 1 주기씩 앞)에서 Z는 가운데 (214 + 46p, 176 - 70p), 반폭 9 + 8p이고 알파는 음표와 같다.
     * 시각 0에 첫 Z는 투명이고 둘째와 셋째의 위 획은 다 보인다. 시각 1.3에 첫 Z가 (237, 141)에 반폭 13으로 보인다.
     */
    @Test
    void ePose12Zzz() {
        BufferedImage at0 = zzz(0);
        assertEquals(0, at0.getRGB(214, 167) >>> 24);
        assertEquals(List.of(PetPaintBase.BLUE.getRGB(), PetPaintBase.BLUE.getRGB()), List.of(at0.getRGB(229, 141), at0.getRGB(244, 115)));
        assertEquals(PetPaintBase.BLUE.getRGB(), zzz(1.3).getRGB(237, 128));
    }

    /** 머리만 상자 좌표 (160, 160)에 표정 face로 그린다. */
    private static BufferedImage head(PetKind kind, PetPaintBase.Face face) {
        BufferedImage img = new BufferedImage(320, 320, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        PetPaintBase.hints(g);
        PetPaintHead p = new PetPaintHead(g, kind, Set.of(), 0);
        p.face = face;
        p.head(160, 160, 0);
        g.dispose();
        return img;
    }

    /** 잠든 얼굴은 입을 닫고(늘 입을 벌린 강아지도), 기지개 얼굴은 입을 벌린다(고양이도). 웃는 얼굴은 강아지만 입을 벌린다. */
    @Test
    void ePose15SleepAndStretchFaces() {
        assertEquals(List.of(0L, 0L, true, true), List.of(count(head(PetKind.CAT, PetPaintBase.Face.HAPPY), PetPaintBase.MOUTH),
                count(head(PetKind.DOG, PetPaintBase.Face.SLEEP), PetPaintBase.MOUTH),
                count(head(PetKind.CAT, PetPaintBase.Face.STRETCH), PetPaintBase.MOUTH) > 0,
                count(head(PetKind.DOG, PetPaintBase.Face.HAPPY), PetPaintBase.MOUTH) > 0));
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

    /** 자세 없는 drawAt은 앉기다(고르기 카드). */
    @Test
    void ePcard8PickCardSits() {
        for (PetKind kind : KINDS) {
            BufferedImage plain = new BufferedImage(320, 340, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = plain.createGraphics();
            PetArt.drawAt(g, kind, COLLAR, 0, 99, 160, 300, 320);
            g.dispose();
            assertArrayEquals(Snap.pixels(draw(kind, Pose.SIT, COLLAR, 0)), Snap.pixels(plain));
        }
    }
}
