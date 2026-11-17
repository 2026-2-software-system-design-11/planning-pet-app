package planningpet.reward.model;

import planningpet.app.model.Store;
import planningpet.schedule.model.Task;
import java.time.LocalDate;

/** 보상과 출석(06의 8, 9, 11, 13번). */
public final class Game {
    private final Store s;

    public Game(Store store) {
        this.s = store;
    }

    public RewardResult complete(Task t) {
        LocalDate today = s.today();
        int before = Level.level(s.exp);
        if (!today.equals(s.rewardCountDay)) {
            s.rewardCountDay = today;
            s.rewardCount = 0;
        }
        t.done = true;
        if (s.rewardCount >= Level.DAILY_CAP) {
            return new RewardResult(RewardResult.Outcome.CAPPED, before, before);
        }
        t.rewardPoints = Level.TASK_POINTS;
        t.rewardExp = Level.TASK_EXP;
        t.rewardDay = today;
        s.points += t.rewardPoints;
        s.exp += t.rewardExp;
        s.rewardCount++;
        return new RewardResult(RewardResult.Outcome.REWARDED, before, Level.level(s.exp));
    }

    public void uncomplete(Task t) {
        s.points -= t.rewardPoints;
        s.exp -= t.rewardExp;
        if (s.today().equals(t.rewardDay) && s.today().equals(s.rewardCountDay)) {
            s.rewardCount--;
        }
        t.done = false;
        t.rewardPoints = 0;
        t.rewardExp = 0;
        t.rewardDay = null;
    }

    public boolean attend() {
        if (s.today().equals(s.attendanceDay)) {
            return false;
        }
        s.points += Level.ATTEND_POINTS;
        s.attendanceDay = s.today();
        return true;
    }
}
