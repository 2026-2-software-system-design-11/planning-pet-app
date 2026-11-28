package planningpet.reward.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import planningpet.app.controller.AppController;
import planningpet.app.model.AppData;
import planningpet.app.model.DataStore;
import planningpet.app.model.LoadResult;
import planningpet.pet.model.PetKind;
import planningpet.pet.model.PetState;
import planningpet.reward.model.DailyProgress;
import planningpet.schedule.model.Category;
import planningpet.schedule.model.Kind;
import planningpet.schedule.model.Task;
import planningpet.schedule.model.TaskDraft;

/** 06 11번(docs/porting/app-window.md E-ATT)과 8번, 9번(docs/porting/home.md S3a)을 controller로 부르는 경계. 시계는 테스트가 옮긴다. */
class RewardControllerTest {
    private static final ZoneId ZONE = ZoneId.systemDefault();
    private LocalDateTime now = LocalDateTime.of(2026, 11, 16, 10, 0);
    private final AppData data = new AppData(new Clock() {
        public ZoneId getZone() { return ZONE; }
        public Clock withZone(ZoneId zone) { return this; }
        public Instant instant() { return now.atZone(ZONE).toInstant(); }
    });
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
    private final RewardController reward = new RewardController(data, app);

    RewardControllerTest() {
        data.onChange(() -> heard++);
    }

    static LocalDate d(int day) { return LocalDate.of(2026, 11, day); }

    @Test
    void E_ATT_1_attend() {
        reward.attend();
        assertEquals(20, data.getPoints());
        assertEquals(d(16), data.getAttendanceDay());
        assertEquals(Set.of(d(16)), data.getAttendDays());
        assertEquals("출석했어! +20P", app.getBubble().getLine());
        assertEquals(1, saves);
        assertEquals(1, heard);
    }

    @Test
    void E_ATT_2_sameDayAgainChangesNothing() {
        reward.attend();
        reward.attend();
        assertEquals(20, data.getPoints());
        assertEquals("출석했어! +20P", app.getBubble().getLine());
        assertEquals(2, heard);
    }

    @Test
    void E_ATT_3_appDayEnd() {
        data.setAttendanceDay(d(16));
        now = LocalDateTime.of(2026, 11, 17, 3, 59);
        reward.attend();
        assertEquals(0, data.getPoints());
        now = LocalDateTime.of(2026, 11, 17, 4, 0);
        reward.attend();
        assertEquals(20, data.getPoints());
        assertEquals(d(17), data.getAttendanceDay());
    }

    @Test
    void E_ATT_4_saveFailsRestores() {
        data.setPoints(30);
        failing = true;
        reward.attend();
        assertEquals(30, data.getPoints());
        assertNull(data.getAttendanceDay());
        assertEquals(Set.of(), data.getAttendDays());
        assertEquals("", app.getBubble().getLine());
        assertEquals(List.of("출석 저장에 실패해서 바꾸기 전으로 돌렸어요."), errors);
    }

    /** 고른 고양이 나비와 그날 할 일들(끝내지 않음, 30분, 시각 없음). 첫 할 일을 돌려준다. */
    private Task tasks(LocalDate day, String... titles) {
        data.addPet(new PetState(PetKind.CAT, "나비"));
        data.setActiveKind(PetKind.CAT);
        for (String title : titles) {
            data.getBook().add(new TaskDraft(Kind.TASK, title, Category.OTHER, day, null, null, 30, "", null, null, false,
                    null, null, false));
        }
        return data.getBook().getTasks().get(0);
    }

    private String rewardOf(Task t) {
        return t.isDone() + " " + t.getRewardPoints() + " " + t.getRewardExp() + " " + t.getRewardDay() + " "
                + t.getRewardPet() + " " + t.getDoneDay();
    }

    private String totals() {
        return data.getPoints() + "P " + data.activePet().getExp() + " " + data.getRewardCountDay() + "/"
                + data.getRewardCount();
    }

    @Test
    void E_HOME_1_complete() {
        Task t = tasks(d(16), "가", "나");
        reward.complete(t);
        assertEquals("true 10 10 2026-11-16 CAT 2026-11-16", rewardOf(t));
        assertEquals("10P 10 2026-11-16/1", totals());
        assertEquals("잘했어! +10P 1 null", app.getBubble().getLine() + " " + app.getBubble().getNewLines() + " "
                + app.getBubble().getRecountDay());
        assertEquals("저장 1 알림 1", "저장 " + saves + " 알림 " + heard);
    }

    @Test
    void T_SAVE_5_oneNoticeChangesAll() {
        tasks(d(16), "a", "b", "c", "e");
        data.setPoints(30);
        reward.complete(data.getBook().getTasks().get(0));
        List<String> seen = new ArrayList<>();
        data.onChange(() -> {
            DailyProgress p = DailyProgress.of(data.getBook(), data.today());
            seen.add(totals() + " " + p.getDone() + " / " + p.getTotal() + " " + app.getBubble().getLine());
        });
        reward.complete(data.getBook().getTasks().get(1));
        assertEquals(List.of("50P 20 2026-11-16/2 2 / 4 잘했어! +10P"), seen);
    }

    @Test
    void T_SAVE_1_completeSaveFails() {
        Task t = tasks(d(16), "가", "나");
        data.setPoints(30);
        failing = true;
        reward.complete(t);
        assertEquals("false 0 0 null null null", rewardOf(t));
        assertEquals("30P 0 null/0", totals());
        assertEquals("", app.getBubble().getLine());
        assertEquals(List.of("할 일 완료 저장에 실패해서 바꾸기 전으로 돌렸어요."), errors);
    }

    @Test
    void E_HOME_2_completeDoneTask() {
        Task t = tasks(d(16), "가", "나");
        reward.complete(t);
        assertThrows(IllegalStateException.class, () -> reward.complete(t));
        assertEquals("10P 10 2026-11-16/1", totals());
        assertEquals("잘했어! +10P", app.getBubble().getLine());
        assertEquals("저장 1 알림 2", "저장 " + saves + " 알림 " + heard);
    }

    @Test
    void E_HOME_3_completeAfterMidnight() {
        now = LocalDateTime.of(2026, 11, 21, 1, 45);
        Task t = tasks(d(20), "복습");
        reward.complete(t);
        assertEquals("true 10 10 2026-11-20 CAT 2026-11-20", rewardOf(t));
        assertEquals("10P 10 2026-11-20/1", totals());
        assertEquals("오늘 할 일을 다 끝냈어! 푹 쉬어.", app.getBubble().getLine());
    }

    @Test
    void T_TALK_7_uncompleteLevelDown() {
        Task t = tasks(d(16), "가", "나");
        data.activePet().setExp(45);
        reward.complete(t);
        reward.uncomplete(t);
        assertEquals("false 0 0 null null null", rewardOf(t));
        assertEquals("0P 45 2026-11-16/0", totals());
        assertEquals("체크를 풀었어. -10P, 이제 Lv.1", app.getBubble().getLine());
        assertEquals("저장 2 알림 2", "저장 " + saves + " 알림 " + heard);
    }

    @Test
    void T_TALK_8_uncompleteCapped() {
        Task t = tasks(d(16), "가", "나");
        data.setRewardCount(d(16), 20);
        data.setPoints(50);
        reward.complete(t);
        assertEquals("잘했어! 오늘 보상은 다 받았지만 체크는 계속 돼.", app.getBubble().getLine());
        reward.uncomplete(t);
        assertEquals("50P 0 2026-11-16/20", totals());
        assertEquals("체크를 풀었어.", app.getBubble().getLine());
    }

    @Test
    void E_HOME_4_levelOfPetThatGotIt() {
        Task t = tasks(d(16), "가", "나");
        data.addPet(new PetState(PetKind.DOG, "바둑이"));
        data.pet(PetKind.DOG).setExp(45);
        data.setActiveKind(PetKind.DOG);
        reward.complete(t);
        data.setActiveKind(PetKind.CAT);
        reward.uncomplete(t);
        assertEquals("45 0", data.pet(PetKind.DOG).getExp() + " " + data.pet(PetKind.CAT).getExp());
        assertEquals("체크를 풀었어. -10P, 이제 Lv.1", app.getBubble().getLine());
    }

    @Test
    void E_HOME_5_oldDataWithoutPet() {
        Task t = tasks(d(16), "가", "나");
        data.activePet().setExp(55);
        data.setPoints(10);
        t.finish(d(15), 10, 10, null);
        reward.uncomplete(t);
        assertEquals("0P 45 null/0", totals());
        assertEquals("체크를 풀었어. -10P, 이제 Lv.1", app.getBubble().getLine());
    }

    @Test
    void E_HOME_6_uncompleteNotDone() {
        Task t = tasks(d(16), "가", "나");
        assertThrows(IllegalStateException.class, () -> reward.uncomplete(t));
        assertEquals("false 0 0 null null null", rewardOf(t));
        assertEquals("", app.getBubble().getLine());
        assertEquals("저장 0 알림 1", "저장 " + saves + " 알림 " + heard);
    }

    @Test
    void E_HOME_7_uncompleteSaveFails() {
        Task t = tasks(d(16), "가", "나");
        reward.complete(t);
        failing = true;
        reward.uncomplete(t);
        assertEquals("true 10 10 2026-11-16 CAT 2026-11-16", rewardOf(t));
        assertEquals("10P 10 2026-11-16/1", totals());
        assertEquals("잘했어! +10P", app.getBubble().getLine());
        assertEquals(List.of("할 일 완료 취소 저장에 실패해서 바꾸기 전으로 돌렸어요."), errors);
    }
}
