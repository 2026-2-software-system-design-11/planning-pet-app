package planningpet.pet.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import planningpet.app.controller.AppController;
import planningpet.app.model.AppData;
import planningpet.app.model.DataStore;
import planningpet.app.model.LoadResult;
import planningpet.pet.model.PetKind;
import planningpet.pet.model.PetState;
import planningpet.pet.model.Pose;
import planningpet.pet.model.Slot;
import planningpet.pet.model.WearId;
import planningpet.schedule.model.Category;
import planningpet.schedule.model.DeleteScope;
import planningpet.schedule.model.Kind;
import planningpet.schedule.model.TaskDraft;

/** 06 28번, 30번의 검사 줄과 docs/porting/pet-controller.md 2절, docs/porting/character.md S8a 2절의 경계. 지금은 11-16 10:00이다. */
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
        assertEquals(e.repeat(10), cat(e.repeat(10)));
        assertEquals(e.repeat(10), cat(e.repeat(11)));
        assertEquals("가나다라마바사아자" + e, cat("가나다라마바사아자" + e));
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
    void E_PET_12_chooseReplacesPets() {
        data.addPet(new PetState(PetKind.CAT, "나비"));
        data.addPet(new PetState(PetKind.DOG, "바둑이"));
        data.setActiveKind(PetKind.DOG);
        pets.choose(PetKind.CAT, "콩");
        assertEquals(1, data.getPets().size());
        assertEquals("콩", data.pet(PetKind.CAT).getName());
        assertEquals(PetKind.CAT, data.getActiveKind());
    }

    @Test
    void E_PET_13_chooseSaveFails() {
        failing = true;
        pets.choose(PetKind.DOG, "바둑이");
        assertEquals(List.of(), data.getPets());
        assertNull(data.getActiveKind());
        assertEquals(List.of("반려동물 고르기 저장에 실패해서 바꾸기 전으로 돌렸어요."), errors);
    }

    @Test
    void T_PET_1_adopt() {
        data.addPet(new PetState(PetKind.CAT, "나비"));
        data.setActiveKind(PetKind.CAT);
        assertTrue(pets.adopt(PetKind.DOG, " "));
        assertFalse(pets.adopt(PetKind.DOG, "x"));
        assertEquals(2, data.getPets().size());
        assertEquals(PetKind.DOG, data.getActiveKind());
        assertEquals("바둑이", data.activePet().getName());
        assertEquals(0, data.activePet().getExp());
        assertEquals(Pose.SIT, data.activePet().getPose());
        assertEquals(Map.of(Slot.NECK, WearId.BASIC_NECKLACE), data.activePet().getWear());
        assertEquals(1, saves);
        assertEquals(1, heard);
    }

    @Test
    void T_TALK_13_adoptedTalks() {
        data.addPet(new PetState(PetKind.CAT, "나비"));
        pets.adopt(PetKind.DOG, " ");
        assertEquals("안녕, 나는 바둑이야. 이제 경험치는 내가 받을게!", app.getBubble().getLine());
        assertEquals("안녕, 나는 바둑이야. 이제 경험치는 내가 받을게!", app.getBubble().getRoomLine());
    }

    @Test
    void E_PET_14_adoptNameRule() {
        data.addPet(new PetState(PetKind.CAT, "나비"));
        pets.adopt(PetKind.DOG, "가나다라마바사아자차카");
        assertEquals("가나다라마바사아자차", data.pet(PetKind.DOG).getName());
    }

    @Test
    void E_PET_15_adoptSaveFails() {
        data.addPet(new PetState(PetKind.CAT, "나비"));
        data.setActiveKind(PetKind.CAT);
        failing = true;
        assertFalse(pets.adopt(PetKind.DOG, ""));
        assertEquals(1, data.getPets().size());
        assertEquals(PetKind.CAT, data.getActiveKind());
        assertEquals("", app.getBubble().getLine());
        assertNull(app.getBubble().getRoomLine());
        assertEquals(List.of("펫 데려오기 저장에 실패해서 바꾸기 전으로 돌렸어요."), errors);
    }

    /** 고양이 나비와 강아지 바둑이, 고른 펫 강아지, 오늘 할 일 셋(T-PET-6의 처음 상태). */
    private void twoPetsThreeTasks() {
        data.addPet(new PetState(PetKind.CAT, "나비"));
        data.addPet(new PetState(PetKind.DOG, "바둑이"));
        data.setActiveKind(PetKind.DOG);
        for (String title : List.of("가", "나", "다")) {
            data.getBook().add(new TaskDraft(Kind.TASK, title, Category.OTHER, data.today(), null, null, 30, "", null, null,
                    false, null, null, false));
        }
    }

    @Test
    void T_PET_5_missingPetNotSelected() {
        data.addPet(new PetState(PetKind.CAT, "나비"));
        data.setActiveKind(PetKind.CAT);
        pets.select(PetKind.DOG);
        assertEquals(PetKind.CAT, data.getActiveKind());
        assertEquals("", app.getBubble().getLine());
        assertEquals(0, saves);
        assertEquals(0, heard);
    }

    @Test
    void E_PET_16_alreadySelected() {
        data.addPet(new PetState(PetKind.CAT, "나비"));
        data.setActiveKind(PetKind.CAT);
        pets.select(PetKind.CAT);
        assertEquals("", app.getBubble().getLine());
        assertEquals(0, saves);
        assertEquals(0, heard);
    }

    @Test
    void T_PET_6_selectedTalks() {
        twoPetsThreeTasks();
        pets.select(PetKind.CAT);
        assertEquals(PetKind.CAT, data.getActiveKind());
        assertEquals("이제 경험치는 내가 받을게! 오늘 할 일 3개 남았어.", app.getBubble().getLine());
        assertNull(app.getBubble().getRoomLine());
        assertEquals(1, saves);
        assertEquals(1, heard);
    }

    @Test
    void E_PET_17_recountAfterSelect() {
        twoPetsThreeTasks();
        app.setHomeDay(() -> LocalDate.of(2026, 11, 20)); // 메인에서 다른 날을 골라도 SELECTED는 오늘 목록을 따른다
        pets.select(PetKind.CAT);
        int said = app.getBubble().getNewLines();
        app.change("일정 삭제", () -> data.getBook().delete(data.getBook().getTasks().get(0), DeleteScope.ONLY_THIS));
        assertEquals("오늘 할 일 2개 남았어.", app.getBubble().getLine());
        assertEquals(said, app.getBubble().getNewLines());
    }

    @Test
    void E_PET_18_selectSaveFails() {
        twoPetsThreeTasks();
        failing = true;
        pets.select(PetKind.CAT);
        assertEquals(PetKind.DOG, data.getActiveKind());
        assertEquals("", app.getBubble().getLine());
        assertEquals(List.of("고른 펫 바꾸기 저장에 실패해서 바꾸기 전으로 돌렸어요."), errors);
    }

    /** 고른 고양이 나비 하나. exp는 누적 경험치다(레벨 3은 130, 레벨 10은 1600, D-20). */
    private PetState oneCat(int exp) {
        PetState cat = new PetState(PetKind.CAT, "나비");
        cat.setExp(exp);
        data.addPet(cat);
        data.setActiveKind(PetKind.CAT);
        return cat;
    }

    @Test
    void T_PET_2_arrowSteps() {
        twoPetsThreeTasks();
        data.setActiveKind(PetKind.CAT);
        pets.step(1);
        assertEquals(PetKind.DOG, data.getActiveKind());
        assertEquals("이제 경험치는 내가 받을게! 오늘 할 일 3개 남았어.", app.getBubble().getLine());
        pets.step(1);
        assertEquals(PetKind.CAT, data.getActiveKind());
        assertEquals(2, saves);
        assertEquals(2, heard);
    }

    @Test
    void E_PET_19_stepBackWraps() {
        twoPetsThreeTasks();
        data.setActiveKind(PetKind.CAT);
        pets.step(-1);
        assertEquals(PetKind.DOG, data.getActiveKind());
    }

    @Test
    void E_PET_20_stepWithOnePet() {
        oneCat(0);
        pets.step(1);
        pets.step(-1);
        assertEquals(PetKind.CAT, data.getActiveKind());
        assertEquals("", app.getBubble().getLine());
        assertEquals(0, saves);
        assertEquals(0, heard);
    }

    @Test
    void T_PET_3_renameRule() {
        oneCat(0);
        pets.rename(PetKind.CAT, "  ");
        assertEquals("나비", data.pet(PetKind.CAT).getName());
        pets.rename(PetKind.CAT, "가나다라마바사아자차카");
        assertEquals("가나다라마바사아자차", data.pet(PetKind.CAT).getName());
    }

    @Test
    void T_PET_7_renamedTalks() {
        oneCat(0).setName("콩");
        pets.rename(PetKind.CAT, "보리");
        assertEquals("보리", data.pet(PetKind.CAT).getName());
        assertEquals("이제 내 이름은 보리야.", app.getBubble().getLine());
        assertNull(app.getBubble().getRoomLine());
        assertEquals(1, saves);
        assertEquals(1, heard);
    }

    @Test
    void E_PET_21_renameSaveFails() {
        oneCat(0);
        failing = true;
        pets.rename(PetKind.CAT, "보리");
        assertEquals("나비", data.pet(PetKind.CAT).getName());
        assertEquals("", app.getBubble().getLine());
        assertEquals(List.of("펫 이름 바꾸기 저장에 실패해서 바꾸기 전으로 돌렸어요."), errors);
    }

    @Test
    void E_PET_22_sameName() {
        oneCat(0);
        pets.rename(PetKind.CAT, "나비");
        assertEquals("나비", data.pet(PetKind.CAT).getName());
        assertEquals("이제 내 이름은 나비야.", app.getBubble().getLine());
        assertEquals(1, saves);
        assertEquals(1, heard);
    }

    @Test
    void T_PET_4_pose() {
        oneCat(0);
        pets.pose(PetKind.CAT, Pose.SLEEP);
        pets.pose(PetKind.CAT, null); // 06의 "춤" 자리. 다섯 자세 밖의 값은 Pose로 넘길 수 없다
        assertEquals(Pose.SLEEP, data.pet(PetKind.CAT).getPose());
        assertEquals(1, saves);
        assertEquals(1, heard);
    }

    @Test
    void E_PET_23_samePose() {
        oneCat(0);
        pets.pose(PetKind.CAT, Pose.SIT);
        assertEquals(Pose.SIT, data.pet(PetKind.CAT).getPose());
        assertEquals(1, saves);
        assertEquals(1, heard);
    }

    @Test
    void E_PET_24_poseSaveFails() {
        oneCat(0);
        failing = true;
        pets.pose(PetKind.CAT, Pose.SLEEP);
        assertEquals(Pose.SIT, data.pet(PetKind.CAT).getPose());
        assertEquals(List.of("자세 고르기 저장에 실패해서 바꾸기 전으로 돌렸어요."), errors);
    }

    @Test
    void T_WEAR_1_onlyOpenWear() {
        oneCat(130);
        pets.applyWear(PetKind.CAT, Map.of(Slot.NECK, WearId.STAR_NECKLACE, Slot.HAT, WearId.STRAW_HAT,
                Slot.CLOTHES, WearId.SCARF));
        assertEquals(Map.of(Slot.HAT, WearId.STRAW_HAT, Slot.CLOTHES, WearId.SCARF), data.pet(PetKind.CAT).getWear());
        assertEquals(1, saves);
        assertEquals(1, heard);
    }

    @Test
    void T_WEAR_3_levelTenWearsAll() {
        Map<Slot, WearId> all = Map.of(Slot.NECK, WearId.CROWN_NECKLACE, Slot.ACCESSORY, WearId.GLASSES,
                Slot.HAT, WearId.CROWN, Slot.CLOTHES, WearId.CAPE);
        oneCat(1600);
        pets.applyWear(PetKind.CAT, all);
        assertEquals(all, data.pet(PetKind.CAT).getWear());
    }

    @Test
    void E_WEAR_9_wrongSlotDropped() {
        oneCat(1600);
        pets.applyWear(PetKind.CAT, Map.of(Slot.HAT, WearId.GLASSES, Slot.NECK, WearId.RIBBON_NECKLACE));
        assertEquals(Map.of(Slot.NECK, WearId.RIBBON_NECKLACE), data.pet(PetKind.CAT).getWear());
    }

    @Test
    void E_WEAR_10_takeAllOff() {
        oneCat(1600);
        Map<Slot, WearId> hatOff = new EnumMap<>(Slot.class);
        hatOff.put(Slot.HAT, null);
        pets.applyWear(PetKind.CAT, hatOff);
        assertEquals(Map.of(), data.pet(PetKind.CAT).getWear());
        pets.applyWear(PetKind.CAT, Map.of(Slot.HAT, WearId.CROWN));
        pets.applyWear(PetKind.CAT, Map.of());
        assertEquals(Map.of(), data.pet(PetKind.CAT).getWear());
        assertEquals(3, saves);
        assertEquals(3, heard);
    }

    @Test
    void E_WEAR_11_wearSaveFails() {
        oneCat(130);
        failing = true;
        pets.applyWear(PetKind.CAT, Map.of(Slot.HAT, WearId.STRAW_HAT));
        assertEquals(Map.of(Slot.NECK, WearId.BASIC_NECKLACE), data.pet(PetKind.CAT).getWear());
        assertEquals(List.of("펫 꾸미기 저장에 실패해서 바꾸기 전으로 돌렸어요."), errors);
    }

    @Test
    void E_WEAR_12_otherPetsLevel() {
        oneCat(1600);
        data.addPet(new PetState(PetKind.DOG, "바둑이"));
        pets.applyWear(PetKind.DOG, Map.of(Slot.NECK, WearId.BASIC_NECKLACE, Slot.HAT, WearId.CROWN));
        assertEquals(Map.of(Slot.NECK, WearId.BASIC_NECKLACE), data.pet(PetKind.DOG).getWear());
        assertEquals(Map.of(Slot.NECK, WearId.BASIC_NECKLACE), data.pet(PetKind.CAT).getWear());
        assertEquals(PetKind.CAT, data.getActiveKind());
    }

    @Test
    void E_PET_25_missingKindDoesNothing() {
        oneCat(1600);
        pets.rename(PetKind.DOG, "보리");
        pets.pose(PetKind.DOG, Pose.SLEEP);
        pets.applyWear(PetKind.DOG, Map.of(Slot.HAT, WearId.CROWN));
        assertEquals(1, data.getPets().size());
        assertEquals("나비", data.activePet().getName());
        assertEquals("", app.getBubble().getLine());
        assertEquals(0, saves);
        assertEquals(0, heard);
    }

    @Test
    void E_PET_26_stepWithNoPets() {
        pets.step(1);
        assertNull(data.getActiveKind());
        assertEquals(0, saves);
        assertEquals(0, heard);
    }
}
