package planningpet.schedule.view;

import planningpet.common.view.*;
import planningpet.schedule.model.*;
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
    public final JRadioButton task = new JRadioButton("할 일", true);
    public final JRadioButton fixed = new JRadioButton("고정 일정");
    public final JTextField title = new JTextField(22);
    public final JSpinner date = new JSpinner(new SpinnerDateModel());
    public final JCheckBox timed = new JCheckBox("정하기");
    public final JSpinner start = new JSpinner(new SpinnerListModel(Ui.times()));
    public final JSpinner minutes = Ui.minutes(30);
    /** 7번 반복(D-64): 안 함, 매일, 요일. 요일이면 요일 단추 일곱(월부터 일)에서 하나 이상 고른다. */
    public final JComboBox<String> repeat = new JComboBox<>(new String[] {"안 함", "매일", "요일"});
    public final JToggleButton[] days = new JToggleButton[7];
    public final JSpinner repeatEnd = new JSpinner(new SpinnerDateModel());
    /** 11번 기간 안에 하기와 마감(D-65). 마감 칸은 켤 때만 보인다. */
    public final JCheckBox period = new JCheckBox("기간 안에 하기");
    public final JLabel dueLabel = new JLabel("마감");
    public final JSpinner due = new JSpinner(new SpinnerDateModel());
    public final JTextArea memo = new JTextArea(3, 22);
    /** D-54 유형. */
    public final javax.swing.JComboBox<String> category = new javax.swing.JComboBox<>(Categories.ALL.toArray(new String[0]));
    /** 칸 아래 빨간 오류 글(05). 칸 순서다. */
    public final java.util.Map<String, JLabel> errors = new java.util.LinkedHashMap<>();
    public final JLabel repeatHint = Ui.hint("기간 안에 하는 할 일은 반복할 수 없어요");
    public final JLabel endLabel = new JLabel();
    /** D-43: 추가 창에서 걸리는 시간이 30일 때만 보인다. */
    public final JLabel minutesHint = Ui.hint("안 바꾸면 30분으로 들어가요");
    private final boolean editing;
    /** 고치는 일정. 추가면 null. */
    private final Task editingTask;
    /** 반복 사본을 고치는 중(기간 칸은 꺼진 채 비활성, D-65). */
    private final boolean copy;
    /** sync 안에서 칸 값을 바꿀 때 다시 sync하지 않는다. */
    private boolean syncing;
    /** 저장을 한 번 누른 뒤에는 칸을 고칠 때마다 다시 검사한다(D-43). */
    private boolean checked;
    /** 오류 글이 늘거나 줄면 창 크기를 다시 맞춘다. 창 없이 만들 때는 아무것도 하지 않는다. */
    Runnable relayout = () -> { };
    private int row;

    public TaskForm(Task t, LocalDate defaultDate, java.time.LocalDateTime now) {
        editing = t != null;
        editingTask = t;
        copy = t != null && t.seriesId != null;
        Ui.limit(title, 100);
        Ui.limit(memo, 1000);
        ButtonGroup kinds = new ButtonGroup();
        kinds.add(task);
        kinds.add(fixed);
        Ui.dayEditor(date, "yyyy-MM-dd (E)");
        Ui.dayEditor(repeatEnd, "yyyy-MM-dd");
        Ui.dayEditor(due, "yyyy-MM-dd");
        for (int i = 0; i < 7; i++) {
            days[i] = new JToggleButton(String.valueOf("월화수목금토일".charAt(i)));
            days[i].setMargin(new Insets(1, 4, 1, 4));
            days[i].addActionListener(e -> sync());
        }
        start.setValue(DefaultDates.start(defaultDate, now).toString());
        for (String k : new String[] {"제목", "마감", "시작 시각", "걸리는 시간", "메모", "반복"}) {
            JLabel l = new JLabel();
            l.setForeground(Theme.SUNDAY);
            l.setVisible(false);
            errors.put(k, l);
        }
        line("종류", Ui.flow(task, fixed));
        line("제목", title);
        line("", errors.get("제목"));
        line("유형", Ui.flow(category));
        line("날짜", date);
        line("기간", Ui.flow(period, dueLabel, due));
        line("", errors.get("마감"));
        line("시작 시각", Ui.flow(timed, start));
        line("", errors.get("시작 시각"));
        line("걸리는 시간 (분)", Ui.flow(minutes, endLabel));
        line("", errors.get("걸리는 시간"));
        line("", minutesHint);
        JComponent[] repeatParts = new JComponent[8];
        repeatParts[0] = repeat;
        System.arraycopy(days, 0, repeatParts, 1, 7);
        line("반복", Ui.flow(repeatParts));
        line("", Ui.flow(new JLabel("끝"), repeatEnd));
        line("", errors.get("반복"));
        line("", repeatHint);
        line("메모", new JScrollPane(memo));
        line("", errors.get("메모"));
        category.setSelectedItem(Categories.OTHER);
        title.setText(t == null ? "" : t.title);
        Ui.setDate(date, t == null ? defaultDate : t.date);
        Ui.setDate(due, t != null && t.dueDate != null ? t.dueDate : DefaultDates.due(t == null ? defaultDate : t.date));
        period.setSelected(t != null && t.dueDate != null);
        if (t != null) {
            fixed.setSelected(t.kind == Task.Kind.FIXED);
            timed.setSelected(t.timed());
            start.setValue(t.timed() ? t.start.toString() : "09:00");
            minutes.setValue(t.minutes);
            memo.setText(t.memo);
            category.setSelectedItem(t.category);
        }
        Ui.setDate(repeatEnd, DefaultDates.repeatEnd(fixed.isSelected() ? Task.Kind.FIXED : Task.Kind.TASK, Ui.dateOf(date)));
        task.addActionListener(e -> sync());
        fixed.addActionListener(e -> sync());
        timed.addActionListener(e -> sync());
        repeat.addActionListener(e -> {
            if (!syncing && repeat.getSelectedIndex() == 2) {
                // 요일을 고르면 날짜의 요일이 골라져 있다(05 일정 입력 창 7번).
                int dow = Ui.dateOf(date).getDayOfWeek().getValue() - 1;
                for (int i = 0; i < 7; i++) {
                    days[i].setSelected(i == dow);
                }
            }
            sync();
        });
        period.addActionListener(e -> sync());
        due.addChangeListener(e -> sync());
        start.addChangeListener(e -> sync());
        minutes.addChangeListener(e -> sync());
        title.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                sync();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                sync();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                sync();
            }
        });
        repeatEnd.addChangeListener(e -> sync());
        date.addChangeListener(e -> {
            if (!repeat.isSelected() && !editing) {
                Ui.setDate(repeatEnd, DefaultDates.repeatEnd(Ui.dateOf(date)));
            }
        });
        sync();
    }

    /** 고정 일정이면 시작 시각이 켜진 채 비활성, 반복은 고정 일정만(D-16, D-17). */
    public void sync() {
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
        repeatEnd.setEnabled(repeat.isSelected() && !editing);
        repeatHint.setVisible(!isFixed);
        minutesHint.setVisible(!editing && (int) minutes.getValue() == 30);
        LocalTime s = startTime();
        int end = s == null ? 0 : s.getHour() * 60 + s.getMinute() + (int) minutes.getValue();
        endLabel.setText(s == null ? "" : String.format("끝 %02d:%02d", end / 60, end % 60));
        if (checked) {
            showErrors(TaskCheck.check(draft(), editing));
        }
    }

    public boolean editing() {
        return editing;
    }

    /** 칸에 쓴 값. 반복이 꺼져 있으면 반복 끝은 null이다. */
    public TaskDraft draft() {
        return new TaskDraft(fixed.isSelected() ? Task.Kind.FIXED : Task.Kind.TASK, title.getText(), Ui.dateOf(date),
                startTime(), (int) minutes.getValue(), memo.getText(), repeat.isSelected() ? Ui.dateOf(repeatEnd) : null,
                (String) category.getSelectedItem());
    }

    /** D-42: 반복 사본을 고칠 때 반복 칸을 켠 채(비활성) 그 반복의 마지막 날짜를 보인다. */
    public void showSeries(LocalDate end) {
        repeat.setSelected(true);
        Ui.setDate(repeatEnd, end);
        sync();
    }

    /** 검사 결과를 칸 아래에 보인다. 고친 칸의 오류는 지운다. */
    public void showErrors(java.util.List<String[]> problems) {
        checked = true;
        errors.values().forEach(l -> {
            l.setText("");
            l.setVisible(false);
        });
        for (String[] p : problems) {
            errors.get(p[0]).setText(p[1]);
            errors.get(p[0]).setVisible(true);
        }
        relayout.run();
    }

    /** 보이는 오류 글을 칸 순서대로 " / "로 잇는다. */
    public String errorText() {
        return String.join(" / ", errors.values().stream().filter(JLabel::isVisible).map(JLabel::getText).toList());
    }

    private LocalTime startTime() {
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
