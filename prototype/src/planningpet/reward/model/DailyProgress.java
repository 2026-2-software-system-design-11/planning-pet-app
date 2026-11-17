package planningpet.reward.model;

import planningpet.schedule.model.TaskBook;
import java.time.LocalDate;

/** 06의 13번. 오늘 체크할 수 있는 일정 가운데 끝낸 수(성취도 막대). */
public record DailyProgress(long done, int total) {
    public static DailyProgress of(TaskBook book, LocalDate today) {
        var list = book.checkables(today);
        return new DailyProgress(list.stream().filter(t -> t.done).count(), list.size());
    }

    public double ratio() {
        return total == 0 ? 0 : done / (double) total;
    }
}
