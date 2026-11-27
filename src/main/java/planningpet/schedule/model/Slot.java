package planningpet.schedule.model;

/** 시간표 한 날에서 일정 하나의 자리(06 19번 출력): 열 번호와 그 무리의 열 수. */
public record Slot(Task task, int column, int columns) {
}
