package planningpet.pet.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

/** docs/porting/app-data.md 2절의 장신구 칸과 장신구 경계(D-51). */
class WearIdTest {
    @Test
    void E_WEAR_1_slotsInOrder() {
        assertEquals(List.of("목걸이", "액세서리", "모자", "의상"),
                Arrays.stream(Slot.values()).map(Slot::label).toList());
    }

    @Test
    void E_WEAR_2_wearsInOrder() {
        assertEquals(List.of("기본 목걸이", "리본 목걸이", "별 목걸이", "왕관 목걸이", "꽃 핀", "리본 핀", "안경", "밀짚모자",
                "베레모", "왕관", "스카프", "조끼", "망토"), Arrays.stream(WearId.values()).map(WearId::label).toList());
    }
}
