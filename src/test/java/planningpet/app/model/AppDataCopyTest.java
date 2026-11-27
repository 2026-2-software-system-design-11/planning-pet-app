package planningpet.app.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.Test;
import planningpet.pet.model.PetKind;
import planningpet.pet.model.PetState;
import planningpet.schedule.model.Category;
import planningpet.schedule.model.EditScope;
import planningpet.schedule.model.Kind;
import planningpet.schedule.model.Repeat;
import planningpet.schedule.model.Task;
import planningpet.schedule.model.TaskDraft;

/** docs/porting/store.md 2절의 E-APP 경계(사본, 되돌리기). 지금은 11-18(수) 10:00이다. */
class AppDataCopyTest {
    private static final ZoneId ZONE = ZoneId.systemDefault();
    private static final Clock CLOCK = Clock.fixed(LocalDateTime.parse("2026-11-18T10:00").atZone(ZONE).toInstant(), ZONE);
    private final AppData data = Filled.data(CLOCK);
    private final Task a = data.getBook().getTasks().get(0);
    private final PetState cat = data.pet(PetKind.CAT);

    /** 일정 a의 시각을 빼고 고양이 경험치를 바꾸고, 나머지 값은 처음 값으로 바꾼다. */
    private void changeAll() {
        data.getBook().clearTime(a, EditScope.ONLY_THIS);
        cat.setExp(999);
        data.restore(new AppData(CLOCK));
    }

    @Test
    void E_APP_16_copyHasSameValues() {
        assertEquals(Filled.values(data), Filled.values(data.copy()));
    }

    @Test
    void E_APP_17_copyIsSeparate() {
        List<Object> before = Filled.values(data);
        AppData copy = data.copy();
        changeAll();
        assertEquals(before, Filled.values(copy));
    }

    @Test
    void E_APP_18_restoreBringsBackValues() {
        List<Object> before = Filled.values(data);
        AppData copy = data.copy();
        changeAll();
        data.restore(copy);
        assertEquals(before, Filled.values(data));
    }

    @Test
    void E_APP_19_restoreKeepsObjects() {
        AppData copy = data.copy();
        data.getBook().clearTime(a, EditScope.ONLY_THIS);
        cat.setExp(999);
        data.restore(copy);
        assertSame(a, data.getBook().getTasks().get(0));
        assertEquals(LocalTime.of(14, 0), a.getStartTime());
        assertSame(cat, data.getPets().get(0));
        assertEquals(120, cat.getExp());
    }

    @Test
    void E_APP_20_addedTaskAndPetLeave() {
        AppData one = new AppData(CLOCK);
        PetState nabi = new PetState(PetKind.CAT, "나비");
        one.addPet(nabi);
        one.setActiveKind(PetKind.CAT);
        AppData copy = one.copy();
        LocalDate today = one.today();
        one.getBook().add(new TaskDraft(Kind.TASK, "운동", Category.EXERCISE, today, null, null, 30, "", Repeat.DAILY,
                today.plusDays(2), false, null, today, false));
        one.addPet(new PetState(PetKind.DOG, "바둑이"));
        one.setActiveKind(PetKind.DOG);
        one.restore(copy);
        assertTrue(one.getBook().getTasks().isEmpty());
        assertEquals(1, one.getNextSeq());
        assertEquals(List.of(nabi), one.getPets());
        assertEquals(PetKind.CAT, one.getActiveKind());
    }

    @Test
    void E_APP_21_listenersStay() {
        int[] heard = {0};
        data.onChange(() -> heard[0]++);
        AppData copy = data.copy();
        copy.changed();
        assertEquals(0, heard[0]);
        data.restore(copy);
        data.changed();
        assertEquals(1, heard[0]);
    }
}
