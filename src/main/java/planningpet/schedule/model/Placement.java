package planningpet.schedule.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/** 06 18번 출력: 놓은 할 일과 놓지 못한 할 일(대상 순서대로). 저장하지 않는다. */
public record Placement(List<Placed> placed, List<Task> unplaced) {
    /** 놓은 할 일과 놓은 날, 시작 시각. 기간 할 일은 적용하면 날짜가 day로 바뀐다(D-65). */
    public record Placed(Task task, LocalDate day, LocalTime start) {
    }

    public Placement {
        placed = List.copyOf(placed);
        unplaced = List.copyOf(unplaced);
    }

    /** 대상 수. 일정관리의 자동 배치 단추 활성과 펫 말 SCHEDULE_HINT의 수다. */
    public int count() {
        return placed.size() + unplaced.size();
    }
}
