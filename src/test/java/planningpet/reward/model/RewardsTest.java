package planningpet.reward.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import planningpet.app.model.AppData;
import planningpet.pet.model.PetKind;
import planningpet.pet.model.PetState;
import planningpet.schedule.model.Category;
import planningpet.schedule.model.DayPlan;
import planningpet.schedule.model.Kind;
import planningpet.schedule.model.Task;

/** 06 8번과 docs/porting/rewards.md 2절의 E-RWD 경계. 펫은 고른 고양이 하나이고 complete와 done은 값을 한 줄로 잇는다. */
class RewardsTest {
    private final Task a = task("A", d(16));
    /** 기간 할 일. p는 시각 없이 11-16에 보이기 시작하고, q는 11-18 09:00으로 정해 두었다. */
    private final Task p = make(Kind.TASK, "P", d(16), null, d(22));
    private final Task q = make(Kind.TASK, "Q", d(18), LocalTime.of(9, 0), d(22));
    private final AppData data = new AppData(Clock.systemDefaultZone(), List.of(a, p, q), List.of());

    RewardsTest() {
        data.addPet(new PetState(PetKind.CAT, "나비"));
        data.setActiveKind(PetKind.CAT);
    }

    private static LocalDate d(int day) { return LocalDate.of(2026, 11, day); }

    private static Task task(String title, LocalDate date) {
        return make(Kind.TASK, title, date, null, null);
    }

    /** 60분 일정. due가 있으면 11-16부터 due까지인 기간 할 일이다. */
    private static Task make(Kind kind, String title, LocalDate date, LocalTime start, LocalDate due) {
        return new Task(title, 1, kind, title, date, start, 60, "", false, Category.OTHER, null, null, due,
                due == null ? null : d(16), false, null, 0, 0, null, null);
    }

    private String complete(Task t, LocalDate today) {
        RewardResult r = new Rewards(data).complete(t, today);
        return r.outcome() + " " + r.points() + " " + r.exp() + ", " + data.getPoints() + "P "
                + data.pet(PetKind.CAT).getExp() + " " + data.getRewardCountDay() + " " + data.getRewardCount();
    }

    private static String done(Task t) {
        return t.isDone() + " " + t.getDoneDay() + " " + t.getRewardPoints() + " " + t.getRewardExp() + " "
                + t.getRewardDay() + " " + t.getRewardPet();
    }

    /** 경험치 exp인 고양이로 끝냈을 때 바뀌기 전과 뒤의 레벨, 레벨과 모습 단계가 올랐는지, 끝낸 뒤 경험치. */
    private String levels(int exp) {
        data.pet(PetKind.CAT).setExp(exp);
        RewardResult r = new Rewards(data).complete(a, d(16));
        return r.before().getLevel() + " " + r.after().getLevel() + " " + r.levelUp() + " " + r.stageUp() + " "
                + data.pet(PetKind.CAT).getExp();
    }

    /** 그날 목록(06 6번)에서 t가 든 묶음. */
    private String where(Task t, int day) {
        DayPlan plan = data.getBook().dayPlan(d(day));
        return (plan.dueToday().contains(t) ? "due" : "") + (plan.timed().contains(t) ? "timed" : "")
                + (plan.untimed().contains(t) ? "untimed" : "") + (plan.period().contains(t) ? "period" : "");
    }

    @Test
    void T_RWD_1_today() {
        assertEquals("REWARDED 10 10, 10P 10 2026-11-16 1", complete(a, d(16)));
        assertEquals("true 2026-11-16 10 10 2026-11-16 CAT", done(a));
    }

    @Test
    void T_RWD_2_pastDay() {
        Task b = task("B", d(14));
        assertEquals("REWARDED 10 10, 10P 10 2026-11-16 1", complete(b, d(16)));
        assertEquals("true 2026-11-16 10 10 2026-11-16 CAT 2026-11-14", done(b) + " " + b.getDate());
    }

    @Test
    void T_RWD_3_laterDay() {
        Task b = task("B", d(30));
        assertEquals("REWARDED 10 10, 10P 10 2026-11-16 1", complete(b, d(16)));
        assertEquals("true 2026-11-16 10 10 2026-11-16 CAT 2026-11-30", done(b) + " " + b.getDate());
    }

    @Test
    void T_RWD_4_lastRewardOfDay() {
        data.setRewardCount(d(16), 19);
        assertEquals("REWARDED 10 10, 10P 10 2026-11-16 20", complete(a, d(16)));
    }

    @Test
    void T_RWD_5_capped() {
        data.setRewardCount(d(16), 20);
        data.setPoints(50);
        assertEquals("CAPPED 0 0, 50P 0 2026-11-16 20", complete(a, d(16)));
        assertEquals("true 2026-11-16 0 0 null null", done(a));
    }

    @Test
    void T_RWD_6_capResetsOnNewDay() {
        data.setRewardCount(d(15), 20);
        assertEquals("REWARDED 10 10, 10P 10 2026-11-16 1", complete(a, d(16)));
    }

    @Test
    void T_RWD_7_fixed() {
        Task f = make(Kind.FIXED, "F", d(16), LocalTime.of(9, 0), null);
        assertEquals("REWARDED 10 10, 10P 10 2026-11-16 1", complete(f, d(16)));
        assertEquals("true 2026-11-16 10 10 2026-11-16 CAT", done(f));
    }

    @Test
    void T_RWD_8_levelUp() {
        assertEquals("1 2 true false 50", levels(40));
    }

    @Test
    void T_RWD_9_stageUp() {
        assertEquals("3 4 true true 240", levels(230));
    }

    @Test
    void E_RWD_1_topLevel() {
        assertEquals("9 10 true false 1600", levels(1590));
    }

    @Test
    void T_RWD_10_activePetGetsExp() {
        data.addPet(new PetState(PetKind.DOG, "바둑이"));
        data.setActiveKind(PetKind.DOG);
        new Rewards(data).complete(a, d(16));
        assertEquals("0 10 DOG", data.pet(PetKind.CAT).getExp() + " " + data.pet(PetKind.DOG).getExp() + " "
                + a.getRewardPet());
    }

    @Test
    void T_RWD_11_periodShownOnDoneDayOnly() {
        assertEquals("REWARDED 10 10, 10P 10 2026-11-17 1", complete(p, d(17)));
        assertEquals("2026-11-16 2026-11-17 2026-11-22", p.getDate() + " " + p.getDoneDay() + " " + p.getDueDate());
        assertEquals(",untimed,", where(p, 16) + "," + where(p, 17) + "," + where(p, 18));
    }

    @Test
    void T_RWD_12_plannedSlotEmpties() {
        complete(q, d(16));
        assertEquals("2026-11-18 09:00 2026-11-16", q.getDate() + " " + q.getStartTime() + " " + q.getDoneDay());
        assertEquals("untimed,", where(q, 16) + "," + where(q, 18));
    }

    /** 프로토타입과 일부러 다름: 프로토타입은 보상을 또 준다. */
    @Test
    void E_RWD_2_alreadyDone() {
        complete(a, d(16));
        assertThrows(IllegalStateException.class, () -> complete(a, d(16)));
        assertEquals("10P 10 1", data.getPoints() + "P " + data.pet(PetKind.CAT).getExp() + " " + data.getRewardCount());
    }
}
