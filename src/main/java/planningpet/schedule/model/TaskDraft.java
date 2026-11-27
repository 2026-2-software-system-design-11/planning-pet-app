package planningpet.schedule.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import planningpet.common.model.Words;

/**
 * 일정 입력 창에 쓴 값(06 2번 입력). 기간 안에 하기를 켜지 않았으면 dueDate는 null이다. placed는 수정하는 기간 할 일에 시각을
 * 정해 둔 날, today는 앱의 오늘이고 없으면 null이다(D-99). 반복하지 않으면 repeat와 repeatEnd는 null이다. editing은 수정
 * 창의 값인지다(D-42). timeOnly는 시각 고치기 창의 값인지이고, 그 창의 값의 날짜는 옮겨 갈 날이다(06 4번, D-32, D-98).
 */
public record TaskDraft(Kind kind, String title, Category category, LocalDate date, LocalDate dueDate,
        LocalTime startTime, int durationMinutes, String memo, Repeat repeat, LocalDate repeatEnd, boolean nextDay,
        LocalDate placed, LocalDate today, boolean editing, boolean timeOnly) {
    /** 끝이 다음 날 04:00을 넘을 때의 문장(D-77). 자정을 넘는 것은 막지 않는다. */
    public static final String END_PROBLEM = "다음 날 04:00까지 끝나야 해요";

    /** 추가 창과 수정 창의 값. timeOnly는 false다. */
    public TaskDraft(Kind kind, String title, Category category, LocalDate date, LocalDate dueDate,
            LocalTime startTime, int durationMinutes, String memo, Repeat repeat, LocalDate repeatEnd, boolean nextDay,
            LocalDate placed, LocalDate today, boolean editing) {
        this(kind, title, category, date, dueDate, startTime, durationMinutes, memo, repeat, repeatEnd, nextDay, placed,
                today, editing, false);
    }

    /** 앱 하루의 시작 분(D-77). 시각이 없으면 null. */
    public Integer startMinute() {
        return startTime == null ? null : (nextDay ? Task.DAY : 0) + startTime.getHour() * 60 + startTime.getMinute();
    }

    /**
     * 06 2번 검사. 칸 순서(제목, 마감, 시작 시각, 걸리는 시간, 메모, 반복)로 모두 담는다. 비어 있으면 저장할 수 있다. 글자 수는 코드 포인트로
     * 센다(06 읽는 법).
     */
    public List<FieldError> validate() {
        List<FieldError> out = new ArrayList<>();
        String stripped = title.strip();
        if (title.isBlank()) {
            out.add(new FieldError("제목", "제목을 써 주세요"));
        } else if (stripped.codePointCount(0, stripped.length()) > 100) {
            out.add(new FieldError("제목", "제목은 100자까지예요"));
        }
        String due = dueDate == null ? null : dueProblem();
        if (due != null) {
            out.add(new FieldError("마감", due));
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
        if (memo.codePointCount(0, memo.length()) > 1000) {
            out.add(new FieldError("메모", "메모는 1000자까지예요"));
        }
        // D-42: 수정할 때 반복 칸은 보이기만 하므로 검사하지 않는다.
        String repeatProblem = repeat == null || editing ? null : repeatProblem();
        if (repeatProblem != null) {
            out.add(new FieldError("반복", repeatProblem));
        }
        return out;
    }

    /**
     * 마감 칸의 문장(D-65, D-99). 시작일, 오늘, 정해 둔 날, 정해 둔 날 후의 시작일 순서로 하나만 고른다. 끝낸 기간 할 일도 날짜(기간
     * 첫날)가 그대로이므로 검사한다(D-66). 맞으면 null.
     */
    private String dueProblem() {
        if (kind == Kind.FIXED) {
            return "기간은 할 일만 정할 수 있어요";
        } else if (dueDate.isBefore(date)) {
            return "마감일이 시작일보다 전이에요";
        } else if (today != null && dueDate.isBefore(today)) {
            return "마감일이 오늘보다 전이에요";
        } else if (placed != null && dueDate.isBefore(placed)) {
            return "마감일이 시각을 정해 둔 " + monthDay(placed) + "보다 전이에요";
        } else if (placed != null && date.isAfter(placed)) {
            return "시작일이 시각을 정해 둔 " + monthDay(placed) + "보다 후예요";
        }
        return null;
    }

    /** 반복 칸의 문장(D-64, D-65). 기간, 요일, 끝 날짜, 반복하는 날, 사본 수 순서로 하나만 고른다. 맞으면 null. */
    private String repeatProblem() {
        if (dueDate != null) {
            return "기간 안에 하는 할 일은 반복할 수 없어요";
        } else if (!repeat.daily() && repeat.days().isEmpty()) {
            return "요일을 하나 이상 골라 주세요";
        } else if (repeatEnd.isBefore(date)) {
            return "반복 끝나는 날이 날짜보다 전이에요";
        }
        int copies = repeat.dates(date, repeatEnd, Repeat.MAX_COPIES).size();
        if (copies == 0) {
            return "반복 끝나는 날 안에 반복하는 날이 없어요";
        }
        return copies > Repeat.MAX_COPIES ? "반복이 너무 많아요. 반복 끝나는 날을 앞당겨 주세요" : null;
    }

    /** "11/19(목)". */
    private static String monthDay(LocalDate d) {
        return d.getMonthValue() + "/" + d.getDayOfMonth() + "(" + "월화수목금토일".charAt(d.getDayOfWeek().getValue() - 1) + ")";
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
