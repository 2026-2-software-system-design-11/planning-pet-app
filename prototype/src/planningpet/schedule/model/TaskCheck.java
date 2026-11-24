package planningpet.schedule.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/** 일정 입력 창의 저장 전 검사. */
public final class TaskCheck {
    private TaskCheck() {
    }

    /**
     * 06의 2번 검사 가운데 화면에서 걸릴 수 있는 것. 칸 순서(제목, 마감, 시작 시각, 걸리는 시간, 메모, 반복)로 {칸, 문장}을 모두
     * 담는다. 비어 있으면 통과. 끝낸 기간 할 일도 날짜(기간 첫날)가 그대로이므로 마감을 검사한다(D-66).
     */
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
                out.add(new String[] {"마감", "마감이 날짜보다 앞이에요"});
            }
        }
        LocalTime start = f.start();
        int minutes = f.minutes();
        if (start != null && start.getHour() * 60 + start.getMinute() + minutes > 24 * 60) {
            out.add(new String[] {"시작 시각", "하루 안에 끝나야 해요"});
        }
        if (minutes % 5 != 0) {
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
            return "끝 날짜가 시작 날짜보다 앞이에요";
        }
        int copies = how.dates(from, to, Repeat.MAX_COPIES).size();
        if (copies == 0) {
            return "끝 날짜 안에 반복하는 날이 없어요";
        }
        return copies > Repeat.MAX_COPIES ? "반복이 너무 많아요. 끝 날짜를 앞당겨 주세요" : null;
    }
}
