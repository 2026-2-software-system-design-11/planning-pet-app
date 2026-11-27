package planningpet.pet.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** 06 데이터 절 PetState와 docs/porting/app-data.md 2절의 펫 경계. */
class PetStateTest {
    private final PetState dog = new PetState(PetKind.DOG, "바둑이");

    @Test
    void E_PET_1_newPet() {
        assertEquals(PetKind.DOG, dog.getKind());
        assertEquals("바둑이", dog.getName());
        assertEquals(0, dog.getExp());
        assertEquals(Pose.SIT, dog.getPose());
    }

    @Test
    void T_WEAR_2_firstWear() {
        assertEquals(Map.of(Slot.NECK, WearId.BASIC_NECKLACE), dog.getWear());
        assertThrows(UnsupportedOperationException.class, () -> dog.getWear().clear());
    }

    @Test
    void E_PET_2_posesInOrder() {
        assertEquals(List.of("앉기", "자기", "기지개", "밥 먹기", "노래"),
                Arrays.stream(Pose.values()).map(Pose::label).toList());
    }
}
