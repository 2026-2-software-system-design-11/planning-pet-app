package planningpet.proto;

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

    public static void open(App app, Task editing, LocalDate defaultDate) {
        TaskForm f = new TaskForm(editing, defaultDate, app.store.now);
        JButton cancel = new JButton("취소");
        JButton saveButton = new JButton("저장");
        JButton delete = editing == null ? null : new JButton("삭제");
        JPanel root = content(f, cancel, saveButton, delete);
        JDialog d = new JDialog(SwingUtilities.getWindowAncestor(app.root),
                editing == null ? "일정 추가" : "일정 수정", JDialog.ModalityType.APPLICATION_MODAL);
        d.setContentPane(root);
        cancel.addActionListener(e -> d.dispose());
        saveButton.addActionListener(e -> save(app, f, editing, d));
        d.getRootPane().setDefaultButton(saveButton);
        if (delete != null) {
            delete.addActionListener(e -> {
                if (app.actions.delete(editing)) {
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
        String problem = TaskCheck.check(f);
        if (problem != null) {
            f.error.setText(problem);
            return;
        }
        String title = f.title.getText().strip();
        if (editing == null) {
            Task.Kind kind = f.fixed.isSelected() ? Task.Kind.FIXED : Task.Kind.TASK;
            app.store.add(kind, title, Ui.dateOf(f.date), f.startTime(), (int) f.minutes.getValue(),
                    f.memo.getText(), f.repeat.isSelected() ? Ui.dateOf(f.repeatEnd) : null);
        } else {
            editing.title = title;
            editing.date = Ui.dateOf(f.date);
            editing.start = f.startTime();
            editing.minutes = (int) f.minutes.getValue();
            editing.memo = f.memo.getText();
        }
        d.dispose();
        app.store.changed();
    }
}
