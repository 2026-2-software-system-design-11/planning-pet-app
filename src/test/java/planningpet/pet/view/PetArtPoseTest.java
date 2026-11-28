package planningpet.pet.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
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
