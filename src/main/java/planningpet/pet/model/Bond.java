package planningpet.pet.model;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import planningpet.schedule.model.Task;

/** 펫의 친밀도와 함께한 날 수(06 27번, D-49). 지운 일도 세도록 부르는 쪽이 TaskBook.rewardHistory()를 넘긴다(D-19). */
public final class Bond {
    /** 친밀도가 100%가 되는 날 수. */
    public static final int FULL_DAYS = 30;

    private Bond() {
    }

    /** kind 펫이 경험치를 받은 서로 다른 앱 날짜의 수. 같은 날 여러 번 받아도 하루다. */
    public static int days(List<Task> tasks, PetKind kind) {
        Set<LocalDate> days = tasks.stream()
                .filter(t -> t.getRewardExp() > 0 && kind == t.getRewardPet() && t.getRewardDay() != null)
                .map(Task::getRewardDay).collect(Collectors.toSet());
        return days.size();
    }

    /** days를 30으로 나눈 백분율을 반올림한 값. 100에서 멈춘다. */
    public static int percent(List<Task> tasks, PetKind kind) {
        return Math.min(100, Math.round(days(tasks, kind) * 100f / FULL_DAYS));
    }
}
