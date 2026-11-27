package planningpet.schedule.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import planningpet.common.model.Words;

/**
 * 일정 입력 창에 쓴 값(06 2번 입력). 기간 안에 하기를 켜지 않았으면 dueDate는 null이다. placed는 수정하는 기간 할 일에 시각을
 * 정해 둔 날, today는 앱의 오늘이고 없으면 null이다(D-99). 반복 칸은 Repeat과 함께 M3에서 더한다.
 */
public record TaskDraft(Kind kind, String title, Category category, LocalDate date, LocalDate dueDate,
        LocalTime startTime, int durationMinutes, String memo, boolean nextDay, LocalDate placed, LocalDate today) {
    /** 끝이 다음 날 04:00을 넘을 때의 문장(D-77). 자정을 넘는 것은 막지 않는다. */
    public static final String END_PROBLEM = "다음 날 04:00까지 끝나야 해요";

    /** 앱 하루의 시작 분(D-77). 시각이 없으면 null. */
    public Integer startMinute() {
        return startTime == null ? null : (nextDay ? Task.DAY : 0) + startTime.getHour() * 60 + startTime.getMinute();
    }

    /** 06 2번 검사. 칸 순서(제목, 마감, 시작 시각, 걸리는 시간, 메모)로 모두 담는다. 비어 있으면 저장할 수 있다. */
    public List<FieldError> validate() {
        List<FieldError> out = new ArrayList<>();
        if (title.isBlank()) {
            out.add(new FieldError("제목", "제목을 써 주세요"));
        } else if (title.strip().length() > 100) {
            out.add(new FieldError("제목", "제목은 100자까지예요"));
        }
        Integer start = startMinute();
        // 화면 칸이 막아도 다른 길로 들어온 값을 여기서 막는다(06 2번, D-100).
        if (start == null && kind == Kind.FIXED) {
            out.add(new FieldError("시작 시각", "고정 일정은 시작 시각이 있어야 해요"));
        }
        boolean offStep = start != null && start % 5 != 0;
        boolean late = start != null && start + durationMinutes > Task.DAY_END;
        if (offStep && late) {
            out.add(new FieldError("시작 시각", END_PROBLEM + ". 5분 단위로 정해 주세요. " + bothFix(durationMinutes)));
        } else if (offStep) {
            out.add(new FieldError("시작 시각", "5분 단위로 정해 주세요"));
        } else if (late) {
            out.add(new FieldError("시작 시각", END_PROBLEM));
        }
        if (durationMinutes < 10 || durationMinutes > 720 || durationMinutes % 5 != 0) {
            out.add(new FieldError("걸리는 시간", "10분부터 720분까지, 5분 단위예요"));
        }
        if (memo.length() > 1000) {
            out.add(new FieldError("메모", "메모는 1000자까지예요"));
        }
        return out;
    }

    /**
     * 끝과 5분 단위를 함께 푸는 시각의 문장(D-100). 다음 날 04:00에 끝나는 가장 늦은 5분 단위 시각이다. 걸리는 시간이 틀렸으면
     * 가까운 맞는 값(10분부터 720분, 5분 단위)으로 셈하고 그 값을 밝힌다.
     */
    private static String bothFix(int minutes) {
        int fixed = Math.max(10, Math.min(720, Math.round(minutes / 5f) * 5));
        int at = (Task.DAY_END - fixed) / 5 * 5;
        String time = Task.hhmm(at) + (at >= Task.DAY ? "(다음 날)" : "");
        String sound = time.replace(")", "");
        if (fixed == minutes) {
            return time + Words.josa(sound, "이면", "면") + " 둘 다 맞아요";
        }
        return "걸리는 시간 " + fixed + "분이면 " + time + Words.josa(sound, "이", "가") + " 맞아요";
    }
}
