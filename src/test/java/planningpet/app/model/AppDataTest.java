package planningpet.app.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.Test;
import planningpet.pet.model.PetKind;
import planningpet.pet.model.PetState;
import planningpet.schedule.model.Category;
import planningpet.schedule.model.Kind;
import planningpet.schedule.model.Repeat;
import planningpet.schedule.model.Task;
import planningpet.schedule.model.TaskDraft;

/** 06 데이터 절 AppData와 docs/porting/app-data.md 2절의 경계. 따로 적지 않으면 지금은 11-18(수) 10:00이다. */
class AppDataTest {
    private final AppData data = at("2026-11-18T10:00");

    private static AppData at(String time) {
        ZoneId zone = ZoneId.systemDefault();
        return new AppData(Clock.fixed(LocalDateTime.parse(time).atZone(zone).toInstant(), zone));
    }

    @Test
    void E_APP_1_rewardAndVisitStartEmpty() {
        assertEquals(0, data.getPoints());
        assertNull(data.getRewardCountDay());
        assertEquals(0, data.getRewardCount());
        assertNull(data.getAttendanceDay());
        assertTrue(data.getAttendDays().isEmpty());
        assertNull(data.getLastOpenDay());
        assertEquals(0, data.getStreak());
    }

    @Test
    void E_APP_2_itemsAndSettingsStartOff() {
        assertTrue(data.getOwnedItems().isEmpty());
        assertTrue(data.getPlacedItems().isEmpty());
        assertFalse(data.isTrayNoticeShown());
        assertFalse(data.isCalendarFixed());
        assertFalse(data.isListClickTime());
        assertEquals(DayOfWeek.MONDAY, data.getWeekStart());
    }

    @Test
    void E_APP_3_samplesAndTasksStartEmpty() {
        assertTrue(data.getSampleIds().isEmpty());
        assertTrue(data.getSampleLooks().isEmpty());
        assertTrue(data.getBook().getTasks().isEmpty());
        assertTrue(data.getBook().getDeletedRewarded().isEmpty());
        assertEquals(1, data.getNextSeq());
    }

    @Test
    void E_APP_4_firstPet() {
        assertEquals(1, data.getPets().size());
        assertEquals("나비", data.activePet().getName());
        assertEquals(PetKind.CAT, data.getActiveKind());
    }

    @Test
    void E_APP_6_midnight() {
        assertEquals(LocalDate.of(2026, 11, 16), at("2026-11-17T00:00").today());
    }

    @Test
    void E_APP_7_endOfAppDay() {
        assertEquals(LocalDate.of(2026, 11, 16), at("2026-11-17T03:59").today());
    }

    @Test
    void E_APP_8_startOfAppDay() {
        assertEquals(LocalDate.of(2026, 11, 17), at("2026-11-17T04:00").today());
    }

    @Test
    void E_APP_9_monthChanges() {
        assertEquals(LocalDate.of(2026, 11, 30), at("2026-12-01T03:59").today());
    }

    @Test
    void E_APP_10_yearChanges() {
        assertEquals(LocalDate.of(2026, 12, 31), at("2027-01-01T03:59").today());
    }

    @Test
    void E_APP_11_activePet() {
        data.addPet(new PetState(PetKind.DOG, "바둑이"));
        data.setActiveKind(PetKind.DOG);
        assertEquals("바둑이", data.activePet().getName());
        assertEquals("나비", data.pet(PetKind.CAT).getName());
    }

    @Test
    void E_APP_12_activeKindWithoutPet() {
        data.setActiveKind(PetKind.DOG);
        assertEquals("나비", data.activePet().getName());
        assertNull(data.pet(PetKind.DOG));
    }

    @Test
    void E_APP_13_listenersInOrder() {
        StringBuilder heard = new StringBuilder();
        data.onChange(() -> heard.append("A"));
        data.onChange(() -> heard.append("B"));
        data.onChange(() -> heard.append("A"));
        data.changed();
        assertEquals("ABA", heard.toString());
    }

    @Test
    void E_APP_14_noListener() {
        data.changed();
        assertEquals(1, data.getNextSeq());
    }

    @Test
    void E_APP_15_bookTakesNextSeq() {
        LocalDate today = data.today();
        List<Task> one = data.getBook().add(new TaskDraft(Kind.TASK, "과제", Category.OTHER, today, null, null, 30, "",
                null, null, false, null, today, false));
        assertEquals(1, one.get(0).getSeq());
        assertEquals(2, data.getNextSeq());
        List<Task> copies = data.getBook().add(new TaskDraft(Kind.FIXED, "수업", Category.OTHER,
                LocalDate.of(2026, 11, 23), null, LocalTime.of(9, 0), 60, "", Repeat.weekly(DayOfWeek.MONDAY),
                LocalDate.of(2026, 12, 7), false, null, today, false));
        assertEquals(List.of(2L, 3L, 4L), copies.stream().map(Task::getSeq).toList());
        assertEquals(5, data.getNextSeq());
    }
}
