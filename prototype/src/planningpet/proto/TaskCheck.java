package planningpet.proto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/** 일정 입력 창의 저장 전 검사. */
public final class TaskCheck {
    private TaskCheck() {
    }

    /** 06의 2번 검사 가운데 화면에서 걸릴 수 있는 것. 칸 순서로 {칸, 문장}을 모두 담는다. 비어 있으면 통과. */
    public static List<String[]> check(TaskForm f) {
        List<String[]> out = new ArrayList<>();
        if (f.title.getText().isBlank()) {
            out.add(new String[] {"제목", "제목을 써 주세요"});
        } else if (f.title.getText().strip().length() > 100) {
            out.add(new String[] {"제목", "제목은 100자까지예요"});
        }
        LocalTime start = f.startTime();
        int minutes = (int) f.minutes.getValue();
        if (start != null && start.getHour() * 60 + start.getMinute() + minutes > 24 * 60) {
            out.add(new String[] {"시작 시각", "하루 안에 끝나야 해요"});
        }
        if (minutes % 5 != 0) {
            out.add(new String[] {"걸리는 시간", "10분부터 720분까지, 5분 단위예요"});
        }
        if (f.memo.getText().length() > 1000) {
            out.add(new String[] {"메모", "메모는 1000자까지예요"});
        }
        if (f.repeat.isSelected()) {
            LocalDate from = Ui.dateOf(f.date);
            LocalDate to = Ui.dateOf(f.repeatEnd);
            if (to.isBefore(from)) {
                out.add(new String[] {"반복", "끝 날짜가 시작 날짜보다 앞이에요"});
            } else if (ChronoUnit.WEEKS.between(from, to) >= 26) {
                out.add(new String[] {"반복", "반복은 26번까지예요"});
            }
        }
        return out;
    }
}
