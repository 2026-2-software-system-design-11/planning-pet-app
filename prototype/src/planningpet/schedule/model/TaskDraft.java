package planningpet.schedule.model;

import java.time.LocalDate;
import java.time.LocalTime;

/** 일정 입력 창에 쓴 값(06의 2번). 화면 칸과 떨어져 검사와 저장에 쓴다. 반복하지 않으면 repeatEnd는 null이다. */
public record TaskDraft(Task.Kind kind, String title, LocalDate date, LocalTime start, int minutes, String memo,
        LocalDate repeatEnd) {
}
