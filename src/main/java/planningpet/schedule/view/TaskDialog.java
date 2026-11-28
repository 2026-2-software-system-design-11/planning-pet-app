package planningpet.schedule.view;

import java.awt.Component;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import planningpet.common.model.Words;
import planningpet.schedule.model.DeleteScope;
import planningpet.schedule.model.EditScope;
import planningpet.schedule.model.Overlap;
import planningpet.schedule.model.Task;

/**
 * 일정 입력 창(05 일정 입력 창, UC-03, UC-05). 지금은 창과 목록 줄, 시각 고치기 창이 묻는 물음 창 넷(05 그 밖의 창과 알림)만 있다. 창 내용은
 * 창 없이 만들어 검사하고, 띄우는 함수는 고른 답을 07의 값으로 바꿔 controller에 넘긴다(docs/porting/task-dialog.md S4a).
 */
public final class TaskDialog {
    static final String OVERLAP = "시간이 겹쳐요";
    static final String SCOPE = "반복 일정 저장";
    static final String DELETE = "일정 삭제";
    static final String CLEAR = "시각 빼기";
    private static final String[] SCOPES = {"이 일정만", "이후 반복 모두", "취소"};

    private TaskDialog() {
    }

    /** 겹침 물음(D-87). 넣기를 고르면 참. TaskController의 ask로 넘긴다. */
    public static boolean askOverlap(Component parent, Overlap overlap) {
        return insert(choose(parent, OVERLAP, overlapPane(overlap)));
    }

    /** 반복 사본 저장 범위(D-42). 취소나 창 닫기는 null. TaskController의 chooseScope로 넘긴다. */
    public static EditScope chooseScope(Component parent, Task task) {
        return editScope(choose(parent, SCOPE, scopePane(task)));
    }

    /** 삭제 확인(D-41). 반복 사본이면 범위도 묻는다. 취소나 창 닫기는 null이고 TaskController.delete를 부르지 않는다. */
    public static DeleteScope confirmDelete(Component parent, Task task) {
        return deleteScope(task, choose(parent, DELETE, deletePane(task)));
    }

    /** 반복 사본의 시각 빼기 범위(D-72, 05 그 밖의 창과 알림). 취소나 창 닫기는 null. TimeEditor가 부른다. */
    static EditScope chooseClear(Component parent, Task task) {
        return editScope(choose(parent, CLEAR, clearPane(task)));
    }

    /** 겹침 창 내용. 글은 Overlap.question()이고 처음 골라 둔 단추는 넣기다(05). */
    static JOptionPane overlapPane(Overlap overlap) {
        return pane(overlap.question(), new String[] {"넣기", "취소"}, 0);
    }

    /** 범위 창 내용. 처음 골라 둔 단추는 취소다(프로토타입 Chooser). */
    static JOptionPane scopePane(Task task) {
        return pane(quoted(task, "은", "는") + " 반복 일정이에요. 어디까지 바꿀까요?", SCOPES, 2);
    }

    /** 시각 빼기 범위 창 내용. 처음 골라 둔 단추는 이 일정만이다(프로토타입 TaskController.clearTime). */
    static JOptionPane clearPane(Task task) {
        return pane(quoted(task, "은", "는") + " 반복 일정이에요. 어디까지 시각을 뺄까요?", SCOPES, 0);
    }

    /** 삭제 확인 창 내용. 반복 사본이 아니면 삭제, 취소이고 처음 골라 둔 단추는 둘 다 취소다. */
    static JOptionPane deletePane(Task task) {
        if (task.getSeriesId() == null) {
            return pane(quoted(task, "을", "를") + " 삭제할까요?", new String[] {"삭제", "취소"}, 1);
        }
        return pane(quoted(task, "은", "는") + " 반복 일정이에요.", SCOPES, 2);
    }

    /** 넣기(0)만 참이다. */
    static boolean insert(int answer) {
        return answer == 0;
    }

    /** 이 일정만(0), 이후 반복 모두(1). 취소(2)와 창 닫기(-1)는 null. */
    static EditScope editScope(int answer) {
        return answer == 0 ? EditScope.ONLY_THIS : answer == 1 ? EditScope.THIS_AND_AFTER : null;
    }

    /** 반복 사본이 아니면 삭제(0)만 ONLY_THIS다. 반복 사본은 범위 창과 같은 번호다. */
    static DeleteScope deleteScope(Task task, int answer) {
        if (task.getSeriesId() == null) {
            return answer == 0 ? DeleteScope.ONLY_THIS : null;
        }
        return answer == 0 ? DeleteScope.ONLY_THIS : answer == 1 ? DeleteScope.THIS_AND_AFTER : null;
    }

    /** 고른 단추의 번호. 창을 닫았거나 아직 고르지 않았으면 -1(JOptionPane.CLOSED_OPTION)이다. */
    static int answer(JOptionPane pane) {
        Object[] options = pane.getOptions();
        for (int i = 0; i < options.length; i++) {
            if (options[i].equals(pane.getValue())) {
                return i;
            }
        }
        return JOptionPane.CLOSED_OPTION;
    }

    /** 프로토타입 JOptionPane.showOptionDialog와 같은 차례로 창을 만들고 띄우고 닫는다. 단추 글은 로캘을 따르지 않는다. */
    private static int choose(Component parent, String title, JOptionPane pane) {
        JDialog dialog = pane.createDialog(parent, title);
        pane.selectInitialValue();
        dialog.setVisible(true);
        dialog.dispose();
        return answer(pane);
    }

    private static String quoted(Task task, String withBatchim, String without) {
        return "'" + task.getTitle() + "'" + Words.josa(task.getTitle(), withBatchim, without);
    }

    private static JOptionPane pane(String message, String[] options, int initial) {
        return new JOptionPane(message, JOptionPane.QUESTION_MESSAGE, JOptionPane.DEFAULT_OPTION, null, options, options[initial]);
    }
}
