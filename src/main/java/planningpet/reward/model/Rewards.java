package planningpet.reward.model;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;
import planningpet.app.model.AppData;
import planningpet.pet.model.PetState;
import planningpet.schedule.model.Task;

/** 할 일 끝내기의 보상, 체크 풀기, 출석(06 8번, 9번, 11번). 보상을 받는 완료는 앱 날짜 하루에 20번까지이고 하나에 10P와 경험치 10이다(D-18). */
public final class Rewards {
    static final int DAILY_CAP = 20;
    static final int TASK_POINTS = 10;
    static final int TASK_EXP = 10;
    /** 출석은 20P이고 경험치는 없다(D-18). */
    static final int ATTEND_POINTS = 20;

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

    /**
     * 06 9번. 받은 보상만큼 포인트를 빼고(음수가 될 수 있다, D-19) 받은 펫에서 경험치를 뺀다. 받은 펫이 적히지 않은 옛 데이터는 경험치를
     * 받았으면 고른 펫에서 뺀다(D-48). 오늘 받은 보상이면 오늘 횟수를 하나 줄인다.
     */
    public void uncomplete(Task task, LocalDate today) {
        if (!task.isDone()) {
            throw new IllegalStateException("끝내지 않은 일정"); // 06 9번 오류(E-UNDO-2).
        }
        data.setPoints(data.getPoints() - task.getRewardPoints());
        PetState pet = task.getRewardPet() != null ? data.pet(task.getRewardPet())
                : task.getRewardExp() > 0 ? data.activePet() : null;
        if (pet != null) {
            pet.setExp(pet.getExp() - task.getRewardExp());
        }
        if (today.equals(task.getRewardDay()) && today.equals(data.getRewardCountDay())) {
            data.setRewardCount(today, data.getRewardCount() - 1);
        }
        task.unfinish();
    }

    /** 06 11번. 오늘 처음이면 20P를 주고 출석한 날을 적는다(D-55). 경험치와 하루 상한 횟수는 그대로다. 오늘 이미 했으면 false다. */
    public boolean attend(LocalDate today) {
        if (today.equals(data.getAttendanceDay())) {
            return false;
        }
        data.setPoints(data.getPoints() + ATTEND_POINTS);
        data.setAttendanceDay(today);
        Set<LocalDate> days = new LinkedHashSet<>(data.getAttendDays());
        days.add(today);
        data.setAttendDays(days);
        return true;
    }
}
