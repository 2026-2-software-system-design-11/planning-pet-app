package planningpet.reward.model;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;
import planningpet.schedule.model.DayPlan;
import planningpet.schedule.model.Task;
import planningpet.schedule.model.TaskBook;

/** 홈 펫 카드의 "오늘 끝낸 일" 막대가 보일 값(06 13번, D-22). 고른 날짜와 관계없이 오늘이다. */
public final class DailyProgress {
    private final int done;
    private final int total;

    private DailyProgress(int done, int total) {
        this.done = done;
        this.total = total;
    }

    /** 오늘 목록의 "오늘 할 일"(dueToday, timed, untimed)을 센다. 기간 안에 할 일(period)은 세지 않는다(D-70). */
    public static DailyProgress of(TaskBook book, LocalDate today) {
        DayPlan plan = book.dayPlan(today);
        List<Task> all = Stream.of(plan.dueToday(), plan.timed(), plan.untimed()).flatMap(List::stream).toList();
        return new DailyProgress((int) all.stream().filter(Task::isDone).count(), all.size());
    }

    public int getDone() { return done; }
    public int getTotal() { return total; }

    /** 오늘 일정이 하나도 없다. 막대가 비고 "0 / 0"이며 막대가 찬 비율 ratio는 0이다. */
    public boolean isEmpty() { return total == 0; }

    public double ratio() {
        return total == 0 ? 0 : done / (double) total;
    }
}
