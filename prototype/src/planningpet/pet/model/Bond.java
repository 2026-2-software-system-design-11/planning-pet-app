package planningpet.pet.model;

import planningpet.schedule.model.Task;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/** D-49 친밀도: 펫이 경험치를 받은 서로 다른 날 수 / 30, 100%에서 멈춘다. */
public final class Bond {
    public static final int FULL_DAYS = 30;

    private Bond() {
    }

    public static int days(Collection<Task> tasks, String kind) {
        Set<LocalDate> days = tasks.stream()
                .filter(t -> t.rewardExp > 0 && kind.equals(t.rewardPet) && t.rewardDay != null)
                .map(t -> t.rewardDay).collect(Collectors.toSet());
        return days.size();
    }

    public static int percent(Collection<Task> tasks, String kind) {
        return Math.min(100, Math.round(days(tasks, kind) * 100f / FULL_DAYS));
    }
}
