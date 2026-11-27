package planningpet.schedule.model;

import java.time.LocalDate;

/** 일정관리 오른쪽 목록의 한 줄(06 7번 출력): 일정과 줄 앞에 붙일 날짜. */
public record WeekRow(Task task, LocalDate day) {
}
