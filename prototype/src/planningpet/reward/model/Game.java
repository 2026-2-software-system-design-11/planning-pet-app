package planningpet.reward.model;

import planningpet.app.model.Store;
import planningpet.pet.model.PetState;
import planningpet.schedule.model.Task;
import java.time.LocalDate;

/** 보상과 출석(06의 8, 9, 11, 13번). */
public final class Game {
    /** 하루 보상 상한과 완료 보상(06의 8번), 출석 보상(11번). */
    private static final int DAILY_CAP = 20;
    public static final int TASK_POINTS = 10;
    public static final int TASK_EXP = 10;
    public static final int ATTEND_POINTS = 20;

    private final Store s;

    public Game(Store store) {
        this.s = store;
    }

    public RewardResult complete(Task t) {
        LocalDate today = s.today();
        PetState pet = s.pet();
        int before = Level.level(pet.exp);
        if (!today.equals(s.rewardCountDay)) {
            s.rewardCountDay = today;
            s.rewardCount = 0;
        }
        t.done = true;
        if (t.dueDate != null) {
            // D-65: 기간 할 일은 끝낸 날의 일이 된다. 마감과 시작 시각은 그대로다.
            t.date = today;
        }
        if (s.rewardCount >= DAILY_CAP) {
            return new RewardResult(RewardResult.Outcome.CAPPED, before, before);
        }
        t.rewardPoints = TASK_POINTS;
        t.rewardExp = TASK_EXP;
        t.rewardDay = today;
        t.rewardPet = pet.kind;
        s.points += t.rewardPoints;
        pet.exp += t.rewardExp;
        s.rewardCount++;
        return new RewardResult(RewardResult.Outcome.REWARDED, before, Level.level(pet.exp));
    }

    public void uncomplete(Task t) {
        s.points -= t.rewardPoints;
        PetState pet = t.rewardPet != null ? s.pet(t.rewardPet) : t.rewardExp > 0 ? s.pet() : null;
        if (pet != null) {
            pet.exp -= t.rewardExp;
        }
        if (s.today().equals(t.rewardDay) && s.today().equals(s.rewardCountDay)) {
            s.rewardCount--;
        }
        t.done = false;
        t.rewardPoints = 0;
        t.rewardExp = 0;
        t.rewardDay = null;
        t.rewardPet = null;
    }

    public boolean attend() {
        if (s.today().equals(s.attendanceDay)) {
            return false;
        }
        s.points += ATTEND_POINTS;
        s.attendanceDay = s.today();
        s.attendDays.add(s.today());
        return true;
    }
}
