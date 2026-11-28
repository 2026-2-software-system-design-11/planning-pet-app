package planningpet.schedule.view;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerListModel;
import planningpet.common.model.AppDay;
import planningpet.common.view.Theme;
import planningpet.common.view.Ui;
import planningpet.schedule.model.Category;
import planningpet.schedule.model.DefaultDates;
import planningpet.schedule.model.Kind;
import planningpet.schedule.model.Repeat;
import planningpet.schedule.model.Task;
import planningpet.schedule.model.TaskDraft;

/**
 * 일정 입력 창의 칸(05 일정 입력 창 1~11번). 창 없이 만들 수 있다. 지금은 추가 창의 칸과 처음 값, 칸끼리 켜고 끄기 sync(), 칸에 쓴 값
 * draft()가 있고, 손댄 값 지키기와 따라가기, 오류 글, 수정 창은 뒤 몫이 더한다(docs/porting/task-dialog.md S4f, S4g). 쳐 넣은 글
 * 읽기(C1)의 칸 아래 글 줄은 두지 않는다.
 */
final class TaskForm {
    final JPanel panel = new JPanel(new GridBagLayout());
    final JRadioButton task = new JRadioButton("할 일", true);
    final JRadioButton fixed = new JRadioButton("고정 일정");
    final JTextField title = new JTextField(22);
    /** D-54 유형. 칸은 label()을 보이고 고른 차례가 Category의 차례다. */
    final JComboBox<String> category = new JComboBox<>(Arrays.stream(Category.values()).map(Category::label).toArray(String[]::new));
    final JSpinner date = new JSpinner(new SpinnerDateModel());
    /** 날짜 칸 이름. 기간 안에 하기를 켜면 "시작일"이다(D-73). */
    final JLabel dateLabel;
    /** 11번 기간 안에 하기와 마감(D-65). */
    final JCheckBox period = new JCheckBox("기간 안에 하기");
    final JLabel periodHint = Ui.hint("반복 일정은 기간을 정할 수 없어요");
    final JLabel dueLabel = new JLabel("마감일");
    final JSpinner due = new JSpinner(new SpinnerDateModel());
    final JCheckBox timed = new JCheckBox("정하기");
    final JSpinner start = new JSpinner(new SpinnerListModel(Times.values()));
    /** 다음 날 새벽 값이면 시작 칸 옆에 적는 실제 날과 시각 "(토 01:30)"(D-77). */
    final JLabel realTime = new JLabel();
    final JSpinner minutes = Ui.minutes(30);
    final JLabel endLabel = new JLabel();
    /** D-43: 추가 창에서 걸리는 시간이 30일 때만 보인다. */
    final JLabel minutesHint = Ui.hint("안 바꾸면 30분으로 들어가요");
    /** 7번 반복(D-64): 안 함, 매일, 요일. 요일 단추 days[i]는 월요일부터다(DayOfWeek.of(i + 1)). */
    final JComboBox<String> repeat = new JComboBox<>(new String[] {"안 함", "매일", "요일"});
    final JToggleButton[] days = new JToggleButton[7];
    final JSpinner repeatEnd = new JSpinner(new SpinnerDateModel());
    final JLabel repeatHint = Ui.hint("기간 안에 하는 할 일은 반복할 수 없어요");
    final JTextArea memo = new JTextArea(3, 22);
    /** 칸 아래 빨간 오류 글(05). 칸 차례다. */
    final Map<String, JLabel> errors = new LinkedHashMap<>();
    /** 날짜를 바꾼 그 순간의 시계(D-101). 처음은 창을 연 때다. */
    Supplier<LocalDateTime> clock;
    private int row;

    /** 추가 창의 칸. date는 06 14번 forAdd가 고른 날, weekStart는 마감 기본값과 요일 단추 차례의 한 주 시작(D-91, D-94). */
    TaskForm(LocalDate date, LocalDateTime now, DayOfWeek weekStart) {
        clock = () -> now;
        Ui.limit(title, 100);
        Ui.limit(memo, 1000);
        ButtonGroup kinds = new ButtonGroup();
        kinds.add(task);
        kinds.add(fixed);
        Ui.dayEditor(this.date, "yyyy-MM-dd (E)");
        Ui.dayEditor(repeatEnd, "yyyy-MM-dd");
        Ui.dayEditor(due, "yyyy-MM-dd");
        for (int i = 0; i < 7; i++) {
            days[i] = new JToggleButton(String.valueOf("월화수목금토일".charAt(i)));
            days[i].setMargin(new Insets(1, 4, 1, 4));
        }
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
        dateLabel = line("날짜", this.date);
        line("기간", Ui.flow(period, periodHint, dueLabel, due));
        line("", errors.get("마감"));
        line("시작 시각", Ui.flow(timed, start, realTime));
        line("", errors.get("시작 시각"));
        line("걸리는 시간 (분)", Ui.flow(minutes, endLabel));
        line("", errors.get("걸리는 시간"));
        line("", minutesHint);
        JComponent[] repeatParts = new JComponent[8];
        repeatParts[0] = repeat;
        // D-94: 요일 단추는 한 주 시작 요일부터 놓는다.
        for (int k = 0; k < 7; k++) {
            repeatParts[k + 1] = days[(weekStart.getValue() - 1 + k) % 7];
        }
        line("반복", Ui.flow(repeatParts));
        line("", Ui.flow(new JLabel("반복 끝나는 날"), repeatEnd));
        line("", errors.get("반복"));
        line("", repeatHint);
        line("메모", new JScrollPane(memo));
        line("", errors.get("메모"));
        category.setSelectedItem(Category.OTHER.label());
        Ui.setDate(this.date, date);
        Ui.setDate(due, DefaultDates.dueFor(date, weekStart));
        Ui.setDate(repeatEnd, DefaultDates.repeatEndFor(Kind.TASK, date));
        start.setValue(Times.value(DefaultDates.defaultStart(date, now)));
        for (AbstractButton b : new AbstractButton[] {task, fixed, timed, period}) {
            b.addActionListener(e -> sync());
        }
        for (JSpinner s : new JSpinner[] {this.date, start, minutes}) {
            s.addChangeListener(e -> sync());
        }
        sync();
    }

    /**
     * 고정 일정이면 시작 시각이 켜진 채 비활성이고 기간은 꺼진 채 비활성이다(D-16, D-65). 기간을 켜면 시작 시각이 꺼진 채
     * 비활성이다(D-68). 앱이 끈 값을 되돌리기와 날짜 따라가기(D-101)는 S4h다.
     */
    void sync() {
        boolean isFixed = fixed.isSelected();
        // 기간을 먼저 끈다. 기간 할 일에서 고정 일정으로 가도 정하기가 켜진다(05 일정 입력 창 4번).
        if (isFixed) {
            period.setSelected(false);
        }
        if (period.isSelected()) {
            timed.setSelected(false);
        }
        if (isFixed) {
            timed.setSelected(true);
        }
        timed.setEnabled(!isFixed && !period.isSelected());
        timed.setToolTipText(period.isSelected() ? "기간 할 일의 시각은 목록에서 정해요" : null);
        start.setEnabled(timed.isSelected());
        period.setEnabled(!isFixed);
        period.setToolTipText(isFixed ? "기간은 할 일만 정할 수 있어요" : null);
        dateLabel.setText(period.isSelected() ? "시작일" : "날짜");
        dueLabel.setVisible(period.isSelected());
        due.setVisible(period.isSelected());
    }

    /** 칸에 쓴 값. 반복이 안 함이면 반복 방식과 끝은 null, 기간 안에 하기가 꺼져 있으면 마감은 null이다. 오늘은 앱의 오늘이다. */
    TaskDraft draft() {
        Repeat how = null;
        if (repeat.getSelectedIndex() == 1) {
            how = Repeat.DAILY;
        } else if (repeat.getSelectedIndex() == 2) {
            List<DayOfWeek> picked = new ArrayList<>();
            for (int i = 0; i < 7; i++) {
                if (days[i].isSelected()) {
                    picked.add(DayOfWeek.of(i + 1));
                }
            }
            how = Repeat.weekly(picked.toArray(DayOfWeek[]::new));
        }
        Integer s = timed.isSelected() ? Times.minuteOf(start.getValue()) : null;
        return new TaskDraft(fixed.isSelected() ? Kind.FIXED : Kind.TASK, title.getText(),
                Category.values()[category.getSelectedIndex()], Ui.dateOf(date), period.isSelected() ? Ui.dateOf(due) : null,
                s == null ? null : LocalTime.of(s % Task.DAY / 60, s % 60), (int) minutes.getValue(), memo.getText(), how,
                how == null ? null : Ui.dateOf(repeatEnd), s != null && s >= Task.DAY, null, AppDay.of(clock.get()), false);
    }

    /** 이름 칸과 칸을 한 줄에 놓는다. 이름 칸을 돌려준다. */
    private JLabel line(String label, JComponent field) {
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 8, 3, 8);
        c.gridy = row++;
        c.anchor = GridBagConstraints.WEST;
        JLabel name = new JLabel(label);
        panel.add(name, c);
        c.gridx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        panel.add(field, c);
        return name;
    }
}
