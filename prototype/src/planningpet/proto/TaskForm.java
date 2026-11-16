package planningpet.proto;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.LocalTime;
import javax.swing.*;

/** 일정 입력 창의 칸들(05 일정 입력 창, 06의 2번 검사). 창 없이도 만들 수 있다. */
public final class TaskForm {
    public final JPanel panel = new JPanel(new GridBagLayout());
    final JRadioButton task = new JRadioButton("할 일", true);
    final JRadioButton fixed = new JRadioButton("고정 일정");
    final JTextField title = new JTextField(22);
    final JSpinner date = new JSpinner(new SpinnerDateModel());
    final JCheckBox timed = new JCheckBox("정하기");
    final JSpinner start = new JSpinner(new SpinnerListModel(Ui.times()));
    final JSpinner minutes = Ui.minutes(30);
    final JCheckBox repeat = new JCheckBox("매주 반복, 끝");
    final JSpinner repeatEnd = new JSpinner(new SpinnerDateModel());
    final JTextArea memo = new JTextArea(3, 22);
    /** 칸 아래 빨간 오류 글(05). 칸 순서다. */
    final java.util.Map<String, JLabel> errors = new java.util.LinkedHashMap<>();
    final JLabel repeatHint = Ui.hint("반복은 고정 일정만 할 수 있어요");
    final JLabel endLabel = new JLabel();
    private final boolean editing;
    private int row;

    public TaskForm(Task t, LocalDate defaultDate, java.time.LocalDateTime now) {
        editing = t != null;
        Ui.limit(title, 100);
        Ui.limit(memo, 1000);
        ButtonGroup kinds = new ButtonGroup();
        kinds.add(task);
        kinds.add(fixed);
        date.setEditor(new JSpinner.DateEditor(date, "yyyy-MM-dd (E)"));
        repeatEnd.setEditor(new JSpinner.DateEditor(repeatEnd, "yyyy-MM-dd"));
        start.setValue(TimeEditor.defaultStart(defaultDate, now).toString());
        for (String k : new String[] {"제목", "시작 시각", "걸리는 시간", "메모", "반복"}) {
            JLabel l = new JLabel();
            l.setForeground(Theme.SUNDAY);
            l.setVisible(false);
            errors.put(k, l);
        }
        line("종류", Ui.flow(task, fixed));
        line("제목", title);
        line("", errors.get("제목"));
        line("날짜", date);
        line("시작 시각", Ui.flow(timed, start));
        line("", errors.get("시작 시각"));
        line("걸리는 시간 (분)", Ui.flow(minutes, endLabel));
        line("", errors.get("걸리는 시간"));
        line("", Ui.hint("안 바꾸면 30분으로 들어가요"));
        line("반복", Ui.flow(repeat, repeatEnd));
        line("", errors.get("반복"));
        line("", repeatHint);
        line("메모", new JScrollPane(memo));
        line("", errors.get("메모"));
        title.setText(t == null ? "" : t.title);
        Ui.setDate(date, t == null ? defaultDate : t.date);
        Ui.setDate(repeatEnd, (t == null ? defaultDate : t.date).plusWeeks(15));
        if (t != null) {
            fixed.setSelected(t.kind == Task.Kind.FIXED);
            timed.setSelected(t.timed());
            start.setValue(t.timed() ? t.start.toString() : "09:00");
            minutes.setValue(t.minutes);
            memo.setText(t.memo);
            for (JComponent c : new JComponent[] {task, fixed, repeat, repeatEnd}) {
                c.setEnabled(false);
            }
        }
        task.addActionListener(e -> sync());
        fixed.addActionListener(e -> sync());
        timed.addActionListener(e -> sync());
        repeat.addActionListener(e -> sync());
        start.addChangeListener(e -> sync());
        minutes.addChangeListener(e -> sync());
        date.addChangeListener(e -> {
            if (!repeat.isSelected() && !editing) {
                Ui.setDate(repeatEnd, Ui.dateOf(date).plusWeeks(15));
            }
        });
        sync();
    }

    /** 고정 일정이면 시작 시각이 켜진 채 비활성, 반복은 고정 일정만(D-16, D-17). */
    void sync() {
        boolean isFixed = fixed.isSelected();
        if (isFixed) {
            timed.setSelected(true);
        }
        timed.setEnabled(!isFixed);
        start.setEnabled(timed.isSelected());
        if (!isFixed) {
            repeat.setSelected(false);
        }
        repeat.setEnabled(isFixed && !editing);
        repeatEnd.setEnabled(repeat.isSelected());
        repeatHint.setVisible(!isFixed);
        LocalTime s = startTime();
        int end = s == null ? 0 : s.getHour() * 60 + s.getMinute() + (int) minutes.getValue();
        endLabel.setText(s == null ? "" : String.format("끝 %02d:%02d", end / 60, end % 60));
    }

    /** 검사 결과를 칸 아래에 보인다. 고친 칸의 오류는 지운다. */
    void showErrors(java.util.List<String[]> problems) {
        errors.values().forEach(l -> {
            l.setText("");
            l.setVisible(false);
        });
        for (String[] p : problems) {
            errors.get(p[0]).setText(p[1]);
            errors.get(p[0]).setVisible(true);
        }
    }

    /** 보이는 오류 글을 칸 순서대로 " / "로 잇는다. */
    String errorText() {
        return String.join(" / ", errors.values().stream().filter(JLabel::isVisible).map(JLabel::getText).toList());
    }

    LocalTime startTime() {
        return timed.isSelected() ? LocalTime.parse((String) start.getValue()) : null;
    }

    private void line(String label, JComponent field) {
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 8, 3, 8);
        c.gridy = row++;
        c.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel(label), c);
        c.gridx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        panel.add(field, c);
    }
}
