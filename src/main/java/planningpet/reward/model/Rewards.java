package planningpet.reward.model;

import java.time.LocalDate;
import planningpet.app.model.AppData;
import planningpet.pet.model.PetState;
import planningpet.schedule.model.Task;

/** 할 일 끝내기의 보상(06 8번). 보상을 받는 완료는 앱 날짜 하루에 20번까지이고 하나에 10P와 경험치 10이다(D-18). */
public final class Rewards {
    static final int DAILY_CAP = 20;
    static final int TASK_POINTS = 10;
    static final int TASK_EXP = 10;

    private final AppData data;

    public Rewards(AppData data) {
        this.data = data;
    }

    /** 06 8번. 일정의 날짜는 보지 않고 하루 상한은 today로 센다(D-18). 경험치는 고른 펫이 받는다(D-48). */
    public RewardResult complete(Task task, LocalDate today) {
        if (task.isDone()) {
            throw new IllegalStateException("이미 끝낸 일정"); // 06 8번 오류. 보상을 두 번 받지 않는다(E-RWD-2).
        }
        PetState pet = data.activePet();
        Level before = Level.of(pet.getExp());
        if (!today.equals(data.getRewardCountDay())) {
            data.setRewardCount(today, 0);
        }
        if (data.getRewardCount() >= DAILY_CAP) {
            task.finish(today, 0, 0, null);
            return new RewardResult(0, 0, RewardResult.Outcome.CAPPED, before, before);
        }
        task.finish(today, TASK_POINTS, TASK_EXP, pet.getKind());
        data.setPoints(data.getPoints() + TASK_POINTS);
        pet.setExp(pet.getExp() + TASK_EXP);
        data.setRewardCount(today, data.getRewardCount() + 1);
        return new RewardResult(TASK_POINTS, TASK_EXP, RewardResult.Outcome.REWARDED, before, Level.of(pet.getExp()));
    }
}
