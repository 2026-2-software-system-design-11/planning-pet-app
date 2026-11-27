package planningpet.pet.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static planningpet.pet.model.Situation.FIRST_RUN;
import static planningpet.pet.model.Situation.OTHER_DATE;
import static planningpet.pet.model.Situation.WELCOME_BACK;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import planningpet.app.model.AppData;
import planningpet.schedule.model.Category;
import planningpet.schedule.model.Kind;
import planningpet.schedule.model.Task;

/** 06 16번과 docs/porting/pet-talk.md 2절. at은 시계("11-16"이면 10:00)와 일정, 고른 고양이(이름 petName)를 정한다. 날 32는 12-02다. */
class PetTalkTest {
    private static final ZoneId ZONE = ZoneId.systemDefault();
    private AppData data;
    private long seq;
    private String petName = "나비";

    private void at(String now, Task... tasks) {
        LocalDateTime time = LocalDateTime.parse("2026-" + (now.contains("T") ? now : now + "T10:00"));
        data = new AppData(Clock.fixed(time.atZone(ZONE).toInstant(), ZONE), List.of(tasks), List.of());
        data.addPet(new PetState(PetKind.CAT, petName));
        data.setActiveKind(PetKind.CAT);
    }

    private static LocalDate d(int day) {
        return day > 30 ? LocalDate.of(2026, 12, day - 30) : LocalDate.of(2026, 11, day);
    }

    /** 일정 하나. hour가 있으면 date의 그 시각, due가 있으면 첫날 first부터의 기간 할 일. */
    private Task make(Kind kind, String title, int date, Integer hour, Integer first, Integer due) {
        return new Task(title + seq, seq++, kind, title, d(date), hour == null ? null : LocalTime.of(hour, 0), 30, "",
                false, Category.OTHER, null, null, due == null ? null : d(due), first == null ? null : d(first), false,
                null, 0, 0, null, null);
    }

    private Task task(String title, int day) {
        return make(Kind.TASK, title, day, null, null, null);
    }

    private Task fixed(String title, int day, int hour) {
        return make(Kind.FIXED, title, day, hour, null, null);
    }

    private Task period(String title, int first, int due) {
        return make(Kind.TASK, title, first, null, first, due);
    }

    private String say(Situation situation) {
        return new PetTalk(data).say(situation);
    }

    @Test
    void T_TALK_3_firstRunNames() {
        for (String name : List.of("나비", "콩", "Max")) {
            petName = name;
            at("11-16");
            assertEquals("처음 만났네! 나는 " + name + (name.equals("콩") ? "이야" : "야") + ". 수업부터 넣어 볼까?",
                    say(FIRST_RUN));
        }
    }

    @Test
    void E_TALK_1_firstRunWithSamples() {
        at("11-16");
        data.setSamples(Set.of("x"), Map.of());
        assertEquals("처음 만났네! 나는 나비야. 예시 일정을 넣어 뒀어.", say(FIRST_RUN));
    }

    @Test
    void T_TALK_9_otherDateCountsFixed() {
        at("11-14", fixed("F1", 16, 9), fixed("F2", 16, 13));
        assertEquals("11월 16일 할 일은 2개야.", new PetTalk(data).say(OTHER_DATE, d(16)));
    }

    @Test
    void E_TALK_12_welcomeBackCount() {
        at("11-24");
        assertEquals("오랜만이야! 다시 와서 반가워.", say(WELCOME_BACK));
        at("11-24", fixed("F1", 24, 9), task("가", 24), period("나", 24, 28));
        assertEquals("오랜만이야! 다시 와서 반가워. 오늘 할 일은 2개야.", say(WELCOME_BACK));
    }

    @Test
    void E_TALK_13_otherDateNextMonthPeriod() {
        at("11-30", period("가", 30, 33), period("나", 30, 35));
        assertEquals("12월 1일은 할 일이 없어. 기간 할 일 2개가 있어.", new PetTalk(data).say(OTHER_DATE, d(31)));
    }
}
