package planningpet.schedule.view;

import planningpet.app.view.App;
import planningpet.common.view.Ui;
import planningpet.schedule.model.*;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.time.LocalDate;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

/** 일정 입력 창(UC-03, UC-05). 추가와 수정에 같은 창을 쓴다. */
public final class TaskDialog {
    private TaskDialog() {
    }

    /** delete가 null이면 추가 창이다(삭제 버튼 없음). */
    public static JPanel content(TaskForm form, JButton cancel, JButton save, JButton delete) {
        JPanel root = new JPanel(new BorderLayout());
        root.add(form.panel, BorderLayout.CENTER);
        JPanel buttons = new JPanel(new BorderLayout());
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        right.add(cancel);
        right.add(save);
        buttons.add(right, BorderLayout.EAST);
        if (delete != null) {
            buttons.add(Ui.flow(delete), BorderLayout.WEST);
        }
        root.add(buttons, BorderLayout.SOUTH);
        return root;
    }

    /** 창의 칸. 반복 사본을 고치면 반복 칸이 그 반복의 방식과 마지막 날짜를 보인다(D-42). */
    public static TaskForm form(App app, Task editing, LocalDate defaultDate) {
        TaskForm f = new TaskForm(editing, defaultDate, app.store.now, app.store.weekStart);
        if (editing != null && editing.seriesId != null) {
            f.showSeries(editing.repeat, app.store.book.seriesEnd(editing));
        }
        return f;
    }

    /** 창 제목 줄. */
    public static String title(Task editing) {
        return editing == null ? "일정 추가" : "일정 수정";
    }

    public static void open(App app, Task editing, LocalDate defaultDate) {
        TaskForm f = form(app, editing, defaultDate);
        JButton cancel = new JButton("취소");
        JButton saveButton = new JButton("저장");
        JButton delete = editing == null ? null : new JButton("삭제");
        JPanel root = content(f, cancel, saveButton, delete);
        JDialog d = new JDialog(SwingUtilities.getWindowAncestor(app.root),
                title(editing), JDialog.ModalityType.APPLICATION_MODAL);
        d.setContentPane(root);
        f.relayout = d::pack;
        cancel.addActionListener(e -> d.dispose());
        saveButton.addActionListener(e -> save(app, f, editing, d));
        d.getRootPane().setDefaultButton(saveButton);
        if (delete != null) {
            delete.addActionListener(e -> {
                if (app.schedule.delete(editing)) {
                    d.dispose();
                }
            });
        }
        d.getRootPane().registerKeyboardAction(e -> d.dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        d.pack();
        d.setLocationRelativeTo(app.root);
        d.setVisible(true);
    }

    /** UC-03 5a: 검사를 통과하지 못하면 창 아래에 문장을 보이고 닫지 않는다. */
    private static void save(App app, TaskForm f, Task editing, JDialog d) {
        if (trySave(app, f, editing)) {
            d.dispose();
        } else {
            d.pack();
        }
    }

    /** 저장 버튼과 같다. 검사를 통과하면 저장하고 true, 아니면 칸 아래에 오류를 모두 보이고 false. */
    public static boolean trySave(App app, TaskForm f, Task editing) {
        var problems = f.problems();
        f.showErrors(problems);
        if (!problems.isEmpty()) {
            return false;
        }
        return app.schedule.save(editing, f.draft());
    }
}
