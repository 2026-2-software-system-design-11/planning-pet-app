package planningpet.app.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import planningpet.app.model.AppData;
import planningpet.app.model.DataStore;
import planningpet.app.model.LoadResult;
import planningpet.pet.model.PetKind;
import planningpet.pet.model.PetState;
import planningpet.reward.model.Rewards;
import planningpet.schedule.model.Category;
import planningpet.schedule.model.Kind;
import planningpet.schedule.model.Task;
import planningpet.schedule.model.TaskDraft;

/** 06 22번과 docs/porting/store.md 2절의 E-SAVE 경계. 저장소는 저장한 포인트를 적거나 늘 실패한다(07 실행 모드). */
class AppControllerTest {
    private final AppData data = new AppData(Clock.systemDefaultZone());
    private final List<Integer> saved = new ArrayList<>();
    private final List<String> errors = new ArrayList<>();
    private final List<Integer> heard = new ArrayList<>();
    private boolean failing;
    private final DataStore store = new DataStore() {
        public LoadResult load() { return LoadResult.firstRun(); }
        public void save(AppData d) throws IOException {
            if (failing) {
                throw new IOException("디스크가 찼다");
            }
            saved.add(d.getBook().getTasks().size());
        }
    };
    private final AppController app = new AppController(data, store, errors::add);

    AppControllerTest() {
        data.onChange(() -> heard.add(data.getPoints()));
    }

    private void addTask() {
        LocalDate today = data.today();
        data.getBook().add(new TaskDraft(Kind.TASK, "과제", Category.OTHER, today, null, null, 30, "", null, null,
                false, null, today, false));
    }

    @Test
    void T_SAVE_3_failedAddKeepsTasksAndSeq() {
        failing = true;
        app.change("일정 추가", this::addTask);
        assertTrue(data.getBook().getTasks().isEmpty());
        assertEquals(1, data.getNextSeq());
    }

    @Test
    void E_SAVE_1_savesOnceAndTellsOnce() {
        app.change("일정 추가", this::addTask);
        assertEquals(List.of(1), saved);
        assertEquals(1, heard.size());
        assertEquals(List.of(), errors);
    }

    @Test
    void E_SAVE_2_failureText() {
        failing = true;
        app.change("할 일 완료", () -> data.setPoints(40));
        assertEquals(List.of("할 일 완료 저장에 실패해서 바꾸기 전으로 돌렸어요."), errors);
        assertEquals(1, heard.size());
    }

    @Test
    void E_SAVE_3_restoresBeforeTelling() {
        data.setPoints(30);
        failing = true;
        app.change("출석", () -> {
            data.setPoints(50);
            data.setAttendDays(Set.of(data.today()));
        });
        assertEquals(30, data.getPoints());
        assertTrue(data.getAttendDays().isEmpty());
        assertEquals(List.of(30), heard);
    }

    /** 06에 없는 길. docs/porting/store.md "07 안에서 정한 것". */
    @Test
    void editThrowsRestoresAndRethrows() {
        IllegalStateException boom = new IllegalStateException("변경 오류");
        var thrown = assertThrows(IllegalStateException.class, () -> app.change("일정 추가", () -> {
            addTask();
            throw boom;
        }));
        assertSame(boom, thrown);
        assertTrue(data.getBook().getTasks().isEmpty());
        assertEquals(List.of(), saved);
        assertEquals(1, heard.size());
    }

    /** 할 일 하나와 고른 고양이를 넣고, 그 할 일을 06 8번으로 끝내는 일을 22번으로 감싼다(docs/porting/rewards.md). */
    private Task completeTask() {
        addTask();
        data.addPet(new PetState(PetKind.CAT, "나비"));
        data.setActiveKind(PetKind.CAT);
        Task t = data.getBook().getTasks().get(0);
        app.change("할 일 완료", () -> new Rewards(data).complete(t, data.today()));
        return t;
    }

    @Test
    void T_SAVE_1_failedCompleteRestores() {
        data.setPoints(30);
        failing = true;
        Task t = completeTask();
        assertEquals("false 30P 0 0", t.isDone() + " " + data.getPoints() + "P " + data.pet(PetKind.CAT).getExp() + " "
                + data.getRewardCount());
        assertEquals(List.of("할 일 완료 저장에 실패해서 바꾸기 전으로 돌렸어요."), errors);
    }

    @Test
    void T_SAVE_4_completeSavesOnce() {
        Task t = completeTask();
        assertEquals(List.of(1), saved);
        assertTrue(t.isDone());
    }

    @Test
    void E_TALK_37_sayOutsideChangeTellsOnce() {
        app.say("오늘은 할 일이 없어.", data.today());
        app.change("할 일 추가", () -> {
            addTask();
            app.say("'과제' 넣었구나! 오늘 남은 일 1개야.", data.today());
        });
        assertEquals("2 '과제' 넣었구나! 오늘 남은 일 1개야. 2", heard.size() + " " + app.getBubble().getLine() + " "
                + app.getBubble().getNewLines());
    }

    @Test
    void E_TALK_38_failedSaveRestoresBubble() {
        app.say("오늘은 할 일이 없어.", data.today());
        failing = true;
        app.change("할 일 추가", () -> {
            addTask();
            app.say("'과제' 넣었구나! 오늘 남은 일 1개야.", data.today());
            app.sayInRoom("쿠션 고마워!");
        });
        assertEquals("오늘은 할 일이 없어. 1 null", app.getBubble().getLine() + " " + app.getBubble().getNewLines() + " "
                + app.getBubble().getRoomLine());
    }
}
