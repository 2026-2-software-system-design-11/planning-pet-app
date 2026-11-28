package planningpet.pet.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import planningpet.app.controller.AppController;
import planningpet.app.model.AppData;
import planningpet.app.model.DataStore;
import planningpet.app.model.LoadResult;
import planningpet.pet.model.PetKind;
import planningpet.pet.model.Pose;
import planningpet.pet.model.Slot;
import planningpet.pet.model.WearId;

/** 06 30번 choose, adopt, select의 검사 줄과 docs/porting/pet-controller.md 2절의 경계. 지금은 11-16 10:00이다. */
class PetControllerTest {
    private final AppData data = new AppData(Clock.fixed(
            LocalDateTime.of(2026, 11, 16, 10, 0).atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault()));
    private final List<String> errors = new ArrayList<>();
    private int saves;
    private int heard;
    private boolean failing;
    private final AppController app = new AppController(data, new DataStore() {
        public LoadResult load() { return LoadResult.firstRun(); }
        public void save(AppData d) throws IOException {
            if (failing) {
                throw new IOException("디스크가 찼다");
            }
            saves++;
        }
    }, errors::add);
    private final PetController pets = new PetController(data, app);

    PetControllerTest() {
        data.onChange(() -> heard++);
    }

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

    @Test
    void T_BRF_6_firstRunGreets() {
        pets.choose(PetKind.DOG, "바둑이");
        app.onWindowShown();
        assertEquals(1, data.getPets().size());
        assertEquals(PetKind.DOG, data.getActiveKind());
        assertEquals("처음 만났네! 나는 바둑이야. 수업부터 넣어 볼까?", app.getBubble().getLine());
        assertEquals(1, data.getStreak());
    }

    @Test
    void T_EX_3_emptyNameNoSamples() {
        pets.choose(PetKind.CAT, "");
        app.onWindowShown();
        assertEquals("나비", data.activePet().getName());
        assertEquals(List.of(), data.getBook().getTasks());
        assertEquals(0, data.getSampleIds().size());
        assertEquals("처음 만났네! 나는 나비야. 수업부터 넣어 볼까?", app.getBubble().getLine());
    }

    @Test
    void E_PET_6_chooseSaysNothing() {
        pets.choose(PetKind.CAT, "콩");
        assertEquals("콩", data.activePet().getName());
        assertEquals(0, data.activePet().getExp());
        assertEquals(Pose.SIT, data.activePet().getPose());
        assertEquals(Map.of(Slot.NECK, WearId.BASIC_NECKLACE), data.activePet().getWear());
        assertEquals("", app.getBubble().getLine());
        assertEquals(0, app.getBubble().getNewLines());
        assertEquals(1, saves);
        assertEquals(1, heard);
    }

    @Test
    void E_PET_13_chooseSaveFails() {
        failing = true;
        pets.choose(PetKind.DOG, "바둑이");
        assertEquals(List.of(), data.getPets());
        assertNull(data.getActiveKind());
        assertEquals(List.of("반려동물 고르기 저장에 실패해서 바꾸기 전으로 돌렸어요."), errors);
    }
}
