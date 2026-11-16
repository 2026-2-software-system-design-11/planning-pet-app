package planningpet.proto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

/** 일정 입력 창의 저장 전 검사. */
public final class TaskCheck {
    private TaskCheck() {
    }

    /** 06의 2번 검사 가운데 화면에서 걸릴 수 있는 것. 통과하면 null. */
    public static String check(TaskForm f) {
        if (f.title.getText().isBlank()) {
            return "제목을 써 주세요";
        }
        if (f.title.getText().strip().length() > 100) {
            return "제목은 100자까지예요";
        }
        LocalTime start = f.startTime();
        if (start != null && start.getHour() * 60 + start.getMinute() + (int) f.minutes.getValue() > 24 * 60) {
            return "하루 안에 끝나야 해요";
        }
        if (f.repeat.isSelected()) {
            LocalDate from = Ui.dateOf(f.date);
            LocalDate to = Ui.dateOf(f.repeatEnd);
            if (to.isBefore(from)) {
                return "끝 날짜가 시작 날짜보다 앞이에요";
            }
            if (ChronoUnit.WEEKS.between(from, to) >= 26) {
                return "반복은 26번까지예요";
            }
        }
        return null;
    }
}
