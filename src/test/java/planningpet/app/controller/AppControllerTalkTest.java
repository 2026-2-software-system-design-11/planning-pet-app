package planningpet.app.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.Test;
import planningpet.app.model.AppData;
import planningpet.app.model.DataStore;
import planningpet.app.model.LoadResult;
import planningpet.pet.model.PetKind;
import planningpet.pet.model.PetState;
import planningpet.pet.model.PetTalk;
import planningpet.pet.model.Situation;
import planningpet.reward.model.Level;
import planningpet.reward.model.Rewards;
import planningpet.schedule.model.Category;
import planningpet.schedule.model.DeleteScope;
import planningpet.schedule.model.EditScope;
import planningpet.schedule.model.Kind;
import planningpet.schedule.model.Task;
import planningpet.schedule.model.TaskDraft;

/** 06 16번 다시 세기(D-106)와 docs/porting/pet-talk.md 2절. 바꾸는 일은 controller가 change 안에서 할 일을 그대로 부른다. */
class AppControllerTalkTest {
    private static final ZoneId ZONE = ZoneId.systemDefault();
    private AppData data;
    private AppController app;
    private PetTalk talk;
    private LocalDate home;
    private final DataStore store = new DataStore() {
        public LoadResult load() { return LoadResult.firstRun(); }
        public void save(AppData saved) { }
    };

    /** 오늘 11-{today} 10:00, 고른 고양이, 일정(제목과 날을 번갈아, 날 앞에 F면 기간 할 일 11-16~11-25). */
    private void at(int today, Object... titleAndDay) {
        LocalDateTime now = LocalDateTime.of(2026, 11, today, 10, 0);
        data = new AppData(Clock.fixed(now.atZone(ZONE).toInstant(), ZONE), List.of(), List.of());
        data.addPet(new PetState(PetKind.CAT, "나비"));
        data.setActiveKind(PetKind.CAT);
        for (int i = 0; i < titleAndDay.length; i += 2) {
            boolean period = titleAndDay[i + 1] instanceof String;
            LocalDate date = period ? d(16) : d((Integer) titleAndDay[i + 1]);
            data.getBook().add(new TaskDraft(Kind.TASK, (String) titleAndDay[i], Category.OTHER, date, period ? d(25) : null,
                    null, 30, "", null, null, false, null, null, false));
        }
        app = new AppController(data, store, error -> { });
        talk = new PetTalk(data);
        home = data.today();
        app.setHomeDay(() -> home);
    }

    private static LocalDate d(int day) {
        return LocalDate.of(2026, 11, day);
    }

    private Task find(String title) {
        return data.getBook().getTasks().stream().filter(t -> t.getTitle().equals(title)).findFirst().orElseThrow();
    }

    private String line() {
        return app.getBubble().getLine();
    }

    /** HomeController가 달력 날짜를 고를 때(06 16번 OTHER_DATE, 오늘이면 USUAL). */
    private void pick(int day) {
        home = d(day);
        app.say(home.equals(data.today()) ? talk.say(Situation.USUAL) : talk.say(Situation.OTHER_DATE, home), home);
    }

    private void delete(String title) {
        app.change("일정 삭제", () -> data.getBook().delete(find(title), DeleteScope.ONLY_THIS));
    }

    @Test
    void E_TALK_36_retellIsNotNew() {
        at(14, "가", 16, "나", 16);
        pick(16);
        delete("나");
        assertEquals("11월 16일 할 일은 1개야. 1", line() + " " + app.getBubble().getNewLines());
    }
}
