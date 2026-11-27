package planningpet.pet.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** 06 데이터 절 PetState와 docs/porting/app-data.md 2절, store.md 2절, pet.md 2절의 펫 경계. */
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

    @Test
    void E_PET_5_patLine() {
        assertEquals("야옹, 좋아!", new PetState(PetKind.CAT, "나비").pat());
        assertEquals("멍멍, 좋아!", dog.pat());
    }

    /** 이름, 경험치, 자세, 장신구를 처음 값과 다르게 넣은 고양이. */
    private static PetState nabi() {
        PetState cat = new PetState(PetKind.CAT, "나비");
        cat.setExp(120);
        cat.setPose(Pose.SING);
        cat.setWear(Map.of(Slot.HAT, WearId.CROWN));
        return cat;
    }

    private static List<Object> values(PetState p) {
        return List.of(p.getKind(), p.getName(), p.getExp(), p.getPose(), Map.copyOf(p.getWear()));
    }

    @Test
    void E_PET_3_copyIsSeparate() {
        PetState cat = nabi();
        PetState copy = cat.copy();
        List<Object> before = values(cat);
        cat.restore(new PetState(PetKind.CAT, "콩"));
        assertEquals(before, values(copy));
    }

    @Test
    void E_PET_4_restoreBringsBackValues() {
        PetState cat = nabi();
        PetState copy = cat.copy();
        cat.setExp(999);
        cat.setPose(Pose.EAT);
        cat.setWear(Map.of());
        cat.restore(copy);
        assertEquals(values(copy), values(cat));
    }
}
