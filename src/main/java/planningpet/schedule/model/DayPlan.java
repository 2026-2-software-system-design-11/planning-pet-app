package planningpet.schedule.model;

import java.time.LocalDate;
import java.util.List;

/**
 * 그날 목록(06 6번 출력, D-31, D-70). 맨 위 마감인 기간 할 일, 시각 있는 일, 시각 없는 할 일, 기간 안에 할 일이다. date는 목록의
 * 날짜다.
 */
public record DayPlan(LocalDate date, List<Task> dueToday, List<Task> timed, List<Task> untimed, List<Task> period) {
    /** 네 묶음이 모두 비었는가. */
    public boolean isEmpty() {
        return dueToday.isEmpty() && timed.isEmpty() && untimed.isEmpty() && period.isEmpty();
    }
}
