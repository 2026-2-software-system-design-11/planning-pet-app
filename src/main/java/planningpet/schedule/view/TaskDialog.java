package planningpet.schedule.view;

import javax.swing.JOptionPane;
import planningpet.common.model.Words;
import planningpet.schedule.model.Overlap;
import planningpet.schedule.model.Task;

/**
 * 일정 입력 창(05 일정 입력 창, UC-03, UC-05). 지금은 창과 목록 줄이 묻는 물음 창(05 그 밖의 창과 알림)의 내용만 있고, 창 없이
 * 만들어 검사한다(docs/porting/task-dialog.md S4a).
 */
public final class TaskDialog {
    static final String OVERLAP = "시간이 겹쳐요";
    static final String SCOPE = "반복 일정 저장";
    static final String DELETE = "일정 삭제";
    private static final String[] SCOPES = {"이 일정만", "이후 반복 모두", "취소"};

    private TaskDialog() {
    }

    /** 겹침 창 내용. 글은 Overlap.question()이고 처음 골라 둔 단추는 넣기다(05). */
    static JOptionPane overlapPane(Overlap overlap) {
        return pane(overlap.question(), new String[] {"넣기", "취소"}, 0);
    }

    /** 범위 창 내용. 처음 골라 둔 단추는 취소다(프로토타입 Chooser). */
    static JOptionPane scopePane(Task task) {
        return pane(quoted(task, "은", "는") + " 반복 일정이에요. 어디까지 바꿀까요?", SCOPES, 2);
    }

    /** 삭제 확인 창 내용. 반복 사본이 아니면 삭제, 취소이고 처음 골라 둔 단추는 둘 다 취소다. */
    static JOptionPane deletePane(Task task) {
        if (task.getSeriesId() == null) {
            return pane(quoted(task, "을", "를") + " 삭제할까요?", new String[] {"삭제", "취소"}, 1);
        }
        return pane(quoted(task, "은", "는") + " 반복 일정이에요.", SCOPES, 2);
    }

    private static String quoted(Task task, String withBatchim, String without) {
        return "'" + task.getTitle() + "'" + Words.josa(task.getTitle(), withBatchim, without);
    }

    private static JOptionPane pane(String message, String[] options, int initial) {
        return new JOptionPane(message, JOptionPane.QUESTION_MESSAGE, JOptionPane.DEFAULT_OPTION, null, options, options[initial]);
    }
}
