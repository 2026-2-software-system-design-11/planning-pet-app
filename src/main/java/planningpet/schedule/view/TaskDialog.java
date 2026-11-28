package planningpet.schedule.view;

import javax.swing.JOptionPane;
import planningpet.schedule.model.Overlap;

/**
 * 일정 입력 창(05 일정 입력 창, UC-03, UC-05). 지금은 창과 목록 줄이 묻는 물음 창(05 그 밖의 창과 알림)의 내용만 있고, 창 없이
 * 만들어 검사한다(docs/porting/task-dialog.md S4a).
 */
public final class TaskDialog {
    static final String OVERLAP = "시간이 겹쳐요";

    private TaskDialog() {
    }

    /** 겹침 창 내용. 글은 Overlap.question()이고 처음 골라 둔 단추는 넣기다(05). */
    static JOptionPane overlapPane(Overlap overlap) {
        return pane(overlap.question(), new String[] {"넣기", "취소"}, 0);
    }

    private static JOptionPane pane(String message, String[] options, int initial) {
        return new JOptionPane(message, JOptionPane.QUESTION_MESSAGE, JOptionPane.DEFAULT_OPTION, null, options, options[initial]);
    }
}
