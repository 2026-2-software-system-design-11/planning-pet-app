package planningpet.schedule.model;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 일정 입력 창에 쓴 값(06의 2번). 화면 칸과 떨어져 검사와 저장에 쓴다. 반복하지 않으면 repeat와 repeatEnd는 null,
 * 기간 안에 하기를 켜지 않았으면 dueDate는 null이다.
 */
public record TaskDraft(Task.Kind kind, String title, String category, LocalDate date, LocalDate dueDate,
        LocalTime start, int minutes, String memo, Repeat repeat, LocalDate repeatEnd) {
    /** 반복과 기간이 없는 입력. 유형은 기타다(D-54). */
    public TaskDraft(Task.Kind kind, String title, LocalDate date, LocalTime start, int minutes, String memo) {
        this(kind, title, Categories.OTHER, date, null, start, minutes, memo, null, null);
    }

    public TaskDraft withDue(LocalDate due) {
        return new TaskDraft(kind, title, category, date, due, start, minutes, memo, repeat, repeatEnd);
    }

    public TaskDraft withRepeat(Repeat how, LocalDate end) {
        return new TaskDraft(kind, title, category, date, dueDate, start, minutes, memo, how, end);
    }
}
