package planningpet.pet.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import planningpet.pet.model.PetKind;

/** 06 30번 choose, adopt, select의 검사 줄과 docs/porting/pet-controller.md 2절의 경계. */
class PetControllerTest {
    private static String cat(String name) {
        return PetController.nameOf(PetKind.CAT, name);
    }

    @Test
    void E_PET_7_emptyNames() {
        assertEquals("바둑이", PetController.nameOf(PetKind.DOG, ""));
        assertEquals("나비", cat("   "));
    }

    @Test
    void E_PET_8_spacesAroundName() {
        assertEquals("콩", cat("  콩  "));
    }

    @Test
    void E_PET_9_tenAndEleven() {
        assertEquals("가나다라마바사아자차", cat("가나다라마바사아자차"));
        assertEquals("가나다라마바사아자차", cat("가나다라마바사아자차카"));
    }

    @Test
    void E_PET_10_cutEndsInSpace() {
        assertEquals("가나다라마바사아자 ", cat("가나다라마바사아자 차카"));
    }

    @Test
    void E_PET_11_emojiName() {
        String e = "😀";
        assertEquals(e.repeat(5), cat(e.repeat(10)));
        assertEquals("가나다라마바사아자\uD83D", cat("가나다라마바사아자" + e));
    }
}
