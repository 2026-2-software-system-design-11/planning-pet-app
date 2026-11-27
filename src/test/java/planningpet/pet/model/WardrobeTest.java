package planningpet.pet.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/** 06 28번이 쓰는 장신구 표(D-51)와 docs/porting/pet.md 2절 E-WEAR-3. */
class WardrobeTest {
    @Test
    void E_WEAR_3_slotAndLevelOfEveryWear() {
        String table = Arrays.stream(Slot.values()).map(slot -> slot.label() + ": " + Arrays.stream(WearId.values())
                .filter(w -> Wardrobe.slotOf(w) == slot).map(w -> w.label() + " " + Wardrobe.opensAt(w))
                .collect(Collectors.joining(", "))).collect(Collectors.joining(". "));
        assertEquals("목걸이: 기본 목걸이 1, 리본 목걸이 3, 별 목걸이 5, 왕관 목걸이 10. 액세서리: 꽃 핀 2, 리본 핀 4, 안경 7. "
                + "모자: 밀짚모자 3, 베레모 6, 왕관 9. 의상: 스카프 2, 조끼 5, 망토 8", table);
    }

    @Test
    void E_WEAR_3_cardsInSlotAreInLevelOrder() {
        for (Slot slot : Slot.values()) {
            List<Integer> levels = Arrays.stream(WearId.values()).filter(w -> Wardrobe.slotOf(w) == slot)
                    .map(Wardrobe::opensAt).toList();
            assertEquals(levels.stream().sorted().toList(), levels, slot.label());
        }
    }
}
