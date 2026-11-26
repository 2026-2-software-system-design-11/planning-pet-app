package planningpet.schedule.model;

import planningpet.common.model.Rules;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** 일정 입력 창의 저장 전 검사. */
public final class TaskCheck {
    private TaskCheck() {
    }

    /**
     * 06의 2번 검사. 칸 순서(제목, 마감, 시작 시각, 걸리는 시간, 메모, 반복)로 {칸, 문장}을 모두
     * 담는다. 비어 있으면 통과. 끝낸 기간 할 일도 날짜(기간 첫날)가 그대로이므로 마감을 검사한다(D-66).
     */
    /** 끝이 다음 날 04:00을 넘을 때의 문장(D-77). */
    public static final String END_PROBLEM = "다음 날 04:00까지 끝나야 해요";

    public static List<String[]> check(TaskDraft f, boolean editing) {
        List<String[]> out = new ArrayList<>();
        if (f.title().isBlank()) {
            out.add(new String[] {"제목", "제목을 써 주세요"});
        } else if (f.title().strip().length() > 100) {
            out.add(new String[] {"제목", "제목은 100자까지예요"});
        }
        if (f.dueDate() != null) {
            if (f.kind() == Task.Kind.FIXED) {
                out.add(new String[] {"마감", "기간은 할 일만 정할 수 있어요"});
            } else if (f.dueDate().isBefore(f.date())) {
                out.add(new String[] {"마감", "마감일이 시작일보다 앞이에요"});
            }
        }
        Integer start = f.startMinute();
        int minutes = f.minutes();
        // 화면 칸이 막아도 가져오기나 저장 파일처럼 다른 길로 들어온 값을 여기서 막는다(06의 2번, D-100).
        if (start == null && f.kind() == Task.Kind.FIXED) {
            out.add(new String[] {"시작 시각", "고정 일정은 시작 시각이 있어야 해요"});
        }
        boolean offStep = start != null && start % 5 != 0;
        boolean late = start != null && start + minutes > Task.DAY_END;
        if (offStep && late) {
            out.add(new String[] {"시작 시각", END_PROBLEM + ". 5분 단위로 정해 주세요. " + bothFix(minutes)});
        } else if (offStep) {
            out.add(new String[] {"시작 시각", "5분 단위로 정해 주세요"});
        } else if (late) {
            // D-77: 자정을 넘는 것은 막지 않고 끝은 다음 날 04:00(28:00)까지다.
            out.add(new String[] {"시작 시각", END_PROBLEM});
        }
        if (minutes < 10 || minutes > 720 || minutes % 5 != 0) {
            out.add(new String[] {"걸리는 시간", "10분부터 720분까지, 5분 단위예요"});
        }
        if (f.memo().length() > 1000) {
            out.add(new String[] {"메모", "메모는 1000자까지예요"});
        }
        // D-42: 수정할 때 반복 칸은 보이기만 하므로 검사하지 않는다(사본을 반복 끝 밖으로 옮겨도 된다).
        if (f.repeat() != null && !editing) {
            String problem = repeatProblem(f);
            if (problem != null) {
                out.add(new String[] {"반복", problem});
            }
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
            return time + Rules.josa(sound, "이면", "면") + " 둘 다 맞아요";
        }
        return "걸리는 시간 " + fixed + "분이면 " + time + Rules.josa(sound, "이", "가") + " 맞아요";
    }

    /** 반복 칸의 문장(D-64, D-65). 맞으면 null. */
    private static String repeatProblem(TaskDraft f) {
        Repeat how = f.repeat();
        LocalDate from = f.date();
        LocalDate to = f.repeatEnd();
        if (f.dueDate() != null) {
            return "기간 안에 하는 할 일은 반복할 수 없어요";
        }
        if (!how.daily() && how.days().isEmpty()) {
            return "요일을 하나 이상 골라 주세요";
        }
        if (to.isBefore(from)) {
            return "반복 끝나는 날이 날짜보다 앞이에요";
        }
        int copies = how.dates(from, to, Repeat.MAX_COPIES).size();
        if (copies == 0) {
            return "반복 끝나는 날 안에 반복하는 날이 없어요";
        }
        return copies > Repeat.MAX_COPIES ? "반복이 너무 많아요. 반복 끝나는 날을 앞당겨 주세요" : null;
    }
}
