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

    public static JPanel content(TaskForm form, boolean editing) {
        JPanel root = new JPanel(new BorderLayout());
        root.add(form.panel, BorderLayout.CENTER);
        JPanel buttons = new JPanel(new BorderLayout());
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        right.add(new JButton("취소"));
        right.add(new JButton("저장"));
        buttons.add(right, BorderLayout.EAST);
        if (editing) {
            buttons.add(Ui.flow(new JButton("삭제")), BorderLayout.WEST);
        }
        root.add(buttons, BorderLayout.SOUTH);
        return root;
    }

    public static void open(App app, Task editing, LocalDate defaultDate) {
        TaskForm f = new TaskForm(editing, defaultDate);
        JPanel root = content(f, editing != null);
        JDialog d = new JDialog(SwingUtilities.getWindowAncestor(app.root),
                editing == null ? "일정 추가" : "일정 수정", JDialog.ModalityType.APPLICATION_MODAL);
        d.setContentPane(root);
        for (JButton b : buttons(root)) {
            switch (b.getText()) {
                case "취소" -> b.addActionListener(e -> d.dispose());
                case "삭제" -> b.addActionListener(e -> {
                    d.dispose();
                    app.actions.delete(editing);
                });
                default -> {
                    d.getRootPane().setDefaultButton(b);
                    b.addActionListener(e -> save(app, f, editing, d));
                }
            }
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

    private static java.util.List<JButton> buttons(java.awt.Container c) {
        java.util.List<JButton> found = new java.util.ArrayList<>();
        for (java.awt.Component k : c.getComponents()) {
            if (k instanceof JButton b) {
                found.add(b);
            } else if (k instanceof java.awt.Container inner) {
                found.addAll(buttons(inner));
            }
        }
        return found;
    }
}
