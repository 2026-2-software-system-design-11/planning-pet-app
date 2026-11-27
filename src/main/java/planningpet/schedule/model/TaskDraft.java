package planningpet.schedule.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 일정 입력 창에 쓴 값(06 2번 입력). 기간 안에 하기를 켜지 않았으면 dueDate는 null이다. placed는 수정하는 기간 할 일에 시각을
 * 정해 둔 날, today는 앱의 오늘이고 없으면 null이다(D-99). 반복 칸은 Repeat과 함께 M3에서 더한다.
 */
public record TaskDraft(Kind kind, String title, Category category, LocalDate date, LocalDate dueDate,
        LocalTime startTime, int durationMinutes, String memo, boolean nextDay, LocalDate placed, LocalDate today) {

    /** 06 2번 검사. 칸 순서(제목, 마감, 시작 시각, 걸리는 시간, 메모)로 모두 담는다. 비어 있으면 저장할 수 있다. */
    public List<FieldError> validate() {
        List<FieldError> out = new ArrayList<>();
        if (title.isBlank()) {
            out.add(new FieldError("제목", "제목을 써 주세요"));
        } else if (title.strip().length() > 100) {
            out.add(new FieldError("제목", "제목은 100자까지예요"));
        }
        return out;
    }
}
