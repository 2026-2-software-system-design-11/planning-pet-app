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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import planningpet.common.model.AppDay;
import planningpet.common.view.Theme;
import planningpet.common.view.Ui;
import planningpet.schedule.model.Category;
import planningpet.schedule.model.DefaultDates;
import planningpet.schedule.model.FieldError;
import planningpet.schedule.model.Kind;
import planningpet.schedule.model.Repeat;
import planningpet.schedule.model.Task;
import planningpet.schedule.model.TaskDraft;

/**
 * 일정 입력 창의 칸(05 일정 입력 창 1~11번). 창 없이 만들 수 있다. 추가 창과 수정 창의 칸과 처음 값, 반복 사본의 반복 칸
 * showSeries, 칸끼리 켜고 끄기 sync(), 손댄 값 지키기와 날짜 따라가기, 칸에 쓴 값 draft(), 칸 아래 오류 글이 있다. 수정 창의 잠금은 뒤 몫이 더한다(docs/porting/task-dialog.md
 * S4f~S4j).
 * 쳐 넣은 글 읽기(C1)의 칸 아래 글 줄은 두지 않는다.
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
    /** 오류 글이 늘거나 줄면 창 크기를 다시 맞춘다. 창 없이 만들 때는 아무것도 하지 않는다. */
    Runnable relayout = () -> { };
    /** 저장을 한 번 누른 뒤에는 칸을 고칠 때마다 다시 검사한다(D-43). */
    private boolean checked;
    /** 날짜를 바꾼 그 순간의 시계(D-101). 처음은 창을 연 때다. */
    Supplier<LocalDateTime> clock;
    /** D-101: 고정 일정으로 가기 앞의 시작 시각 정하기, 기간 안에 하기, 마감. 할 일로 돌아오면 되돌리고 null이 된다. */
    private Boolean timedBefore;
    private boolean periodBefore;
    private LocalDate dueBefore;
    /** D-101: 기간 때문에 앱이 끄기 앞의 정하기. 기간을 끄면 되돌리고 null이 된다. */
    private Boolean timedBeforePeriod;
    /** D-101: 반복 때문에 앱이 끈 기간 할 일의 마감. 반복을 안 함으로 되돌리면 기간과 함께 되돌리고 null이 된다. */
    private LocalDate dueBeforeRepeat;
    /** D-101: 사용자가 시작 시각을 만졌는지. 만지기 전에는 날짜의 기본 시작 시각을 따라간다. */
    private boolean startTouched;
    /** 시작 시각을 마지막으로 잰 날짜. 날짜가 바뀐 때만 다시 잰다. 처음 sync에서 잰다. */
    private LocalDate startDay;
    /** 지난 sync에서 시작 칸을 쓸 수 있었는지. 쓸 수 있게 되는 순간에도 다시 잰다(D-101). */
    private boolean startOpen;
    /** D-101: 요일을 한 번 고른 뒤에는 다른 반복을 거쳐 돌아와도 단추를 그대로 둔다. */
    private boolean daysShown;
    /** D-101: 앱이 골라 준 요일. 사용자가 요일 단추를 누르면 null이 되고 날짜를 따라가지 않는다. */
    private DayOfWeek autoDay;
    /** 반복 끝 날짜를 사용자가 고쳤는가. 고치기 전에는 시작 날짜를 따라간다(D-79). */
    private boolean endTouched;
    /** 마감을 사용자가 골랐는가. 고른 뒤에는 날짜나 종류를 바꾸고 기간을 껐다 켜도 다시 채우지 않는다(D-96). */
    private boolean dueTouched;
    /** 마감 기본값이 쓰는 한 주의 시작 요일(D-91). */
    private final DayOfWeek weekStart;
    /** 수정 창이면 참. 종류와 반복 칸이 비활성이다(05 일정 입력 창 수정 줄). */
    private final boolean editing;
    /** 반복 사본을 고치는 중(기간 칸은 꺼진 채 비활성, D-65). */
    private final boolean copy;
    /** sync 안에서 칸 값을 바꿀 때 다시 sync하지 않는다. */
    private boolean syncing;
    private int row;

    /** 추가 창의 칸. date는 06 14번 forAdd가 고른 날, weekStart는 마감 기본값과 요일 단추 차례의 한 주 시작(D-91, D-94). */
    TaskForm(LocalDate date, LocalDateTime now, DayOfWeek weekStart) {
        this(null, date, now, weekStart);
    }

    /** 수정 창의 칸. 칸은 t의 값으로 열린다. 반복 사본의 반복 칸은 showSeries가 채운다(D-42). */
    TaskForm(Task t, LocalDateTime now, DayOfWeek weekStart) {
        this(t, null, now, weekStart);
    }

    /** t가 null이면 date로 여는 추가 창이다. */
    private TaskForm(Task t, LocalDate date, LocalDateTime now, DayOfWeek weekStart) {
        this.weekStart = weekStart;
        editing = t != null;
        copy = t != null && t.getSeriesId() != null;
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
            days[i].addActionListener(e -> {
                autoDay = null;
                sync();
            });
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
        category.setSelectedItem((t == null ? Category.OTHER : t.getCategory()).label());
        // D-66: 기간 할 일의 날짜 칸은 기간 첫날이다(계획한 날이 아니다).
        LocalDate first = t == null ? date : t.period() ? t.getStartDay() : t.getDate();
        Ui.setDate(this.date, first);
        // D-96: 수정 창에서 저장해 둔 마감도 고른 마감이다.
        dueTouched = t != null && t.period();
        Ui.setDate(due, dueTouched ? t.getDueDate() : DefaultDates.dueFor(first, weekStart));
        start.setValue(Times.value(DefaultDates.defaultStart(first, now)));
        if (t != null) {
            title.setText(t.getTitle());
            fixed.setSelected(t.getKind() == Kind.FIXED);
            period.setSelected(t.period());
            timed.setSelected(t.timed() && !t.period());
            // D-101: 저장해 둔 시각은 사용자 값이다. 시각 없는 일정의 시작 칸은 앱이 채운 값이라 날짜를 따라간다.
            startTouched = t.timed();
            if (t.timed()) {
                start.setValue(Times.value(t.startMinute()));
            }
            minutes.setValue(t.getDurationMinutes());
            memo.setText(t.getMemo());
        }
        Ui.setDate(repeatEnd, DefaultDates.repeatEndFor(fixed.isSelected() ? Kind.FIXED : Kind.TASK, first));
        for (AbstractButton b : new AbstractButton[] {task, fixed, timed, period}) {
            b.addActionListener(e -> sync());
        }
        repeat.addActionListener(e -> {
            if (!syncing && repeat.getSelectedIndex() == 2 && !daysShown) {
                // 요일을 처음 고르면 날짜의 요일이 골라져 있다(D-93, 05 일정 입력 창 7번).
                daysShown = true;
                autoDay = Ui.dateOf(this.date).getDayOfWeek();
                pick(autoDay);
            }
            sync();
        });
        start.addChangeListener(e -> {
            if (!syncing) {
                startTouched = true;
            }
            sync();
        });
        due.addChangeListener(e -> {
            if (!syncing) {
                dueTouched = true;
            }
            sync();
        });
        repeatEnd.addChangeListener(e -> {
            if (!syncing) {
                endTouched = true;
            }
            sync();
        });
        for (JSpinner s : new JSpinner[] {this.date, minutes}) {
            s.addChangeListener(e -> sync());
        }
        title.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                sync();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                sync();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                sync();
            }
        });
        sync();
    }

    /**
     * 수정 창이면 종류와 반복 칸이 비활성이고 반복 끝이 날짜를 따라가지 않으며 30분 안내가 없다. 반복 사본은 기간이 꺼진 채 비활성이다.
     * 고정 일정이면 시작 시각이 켜진 채 비활성이고 기간은 꺼진 채 비활성이다(D-16, D-65). 반복과 기간은 함께 고를 수 없다(D-65). 기간을
     * 켜면 시작 시각이 꺼진 채 비활성이다(D-68). 시작 시각을 정하면 끝 시각 글과 다음 날 값의 실제 날과 시각을 적고(D-73, D-77),
     * 걸리는 시간이 30이면 30분 안내를 보인다(D-43). 고정 일정, 기간, 반복 때문에 앱이 바꾼 칸은 맥락이 돌아오면 되돌린다(D-101).
     * 손대지 않은 시작 시각은 날짜를 바꾸거나 시작 칸이 열리는 그 순간의 시계로 다시 잰다(D-101). 앱이 골라 준 요일, 손대지 않은 반복
     * 끝(D-79), 고르지 않은 마감(D-96)도 날짜를 따라간다. 저장을 누른 뒤에는 다시 검사해 칸 아래 글을 고친다(D-43).
     * 마감 칸만은 저장 전에도 값이 바뀔 때마다 바로 검사한다(D-101). 오류 줄이나 마감 칸이 나타나고 사라지면 창 크기를 다시 맞춘다.
     */
    void sync() {
        if (syncing) {
            return;
        }
        syncing = true;
        boolean dueShown = due.isVisible();
        task.setEnabled(!editing);
        fixed.setEnabled(!editing);
        boolean isFixed = fixed.isSelected();
        // D-101: 고정 일정이라서 앱이 바꾼 칸은 할 일로 돌아오면 사용자가 둔 값으로 되돌린다.
        if (isFixed && !editing && timedBefore == null) {
            timedBefore = timedBeforePeriod != null ? timedBeforePeriod : timed.isSelected();
            periodBefore = period.isSelected();
            dueBefore = Ui.dateOf(due);
        } else if (!isFixed && timedBefore != null) {
            timed.setSelected(timedBefore);
            period.setSelected(periodBefore);
            Ui.setDate(due, dueBefore);
            timedBefore = null;
        }
        boolean repeating = repeat.getSelectedIndex() != 0;
        boolean periodAllowed = !isFixed && !copy && !repeating;
        if (!isFixed && !copy && repeating && period.isSelected()) {
            dueBeforeRepeat = Ui.dateOf(due);
        } else if (periodAllowed && dueBeforeRepeat != null) {
            period.setSelected(true);
            Ui.setDate(due, dueBeforeRepeat);
            dueBeforeRepeat = null;
        }
        // 기간을 먼저 끈다. 기간 할 일에서 고정 일정으로 가도 정하기가 켜진다(05 일정 입력 창 4번).
        if (!periodAllowed) {
            period.setSelected(false);
        }
        // D-68: 기간 할 일의 시각은 목록의 "시각 정하기"와 자동 배치로만 정한다. 기간을 끄면 앞의 정하기로 되돌린다(D-101).
        if (period.isSelected()) {
            if (timedBeforePeriod == null) {
                timedBeforePeriod = timed.isSelected();
            }
            timed.setSelected(false);
        } else if (timedBeforePeriod != null) {
            timed.setSelected(timedBeforePeriod);
            timedBeforePeriod = null;
        }
        if (isFixed) {
            timed.setSelected(true);
        }
        timed.setEnabled(!isFixed && !period.isSelected());
        timed.setToolTipText(period.isSelected() ? "기간 할 일의 시각은 목록에서 정해요" : null);
        start.setEnabled(timed.isSelected());
        period.setEnabled(periodAllowed);
        period.setToolTipText(isFixed ? "기간은 할 일만 정할 수 있어요" : null);
        periodHint.setVisible(repeating && !isFixed && !copy);
        dateLabel.setText(period.isSelected() ? "시작일" : "날짜");
        dueLabel.setVisible(period.isSelected());
        due.setVisible(period.isSelected());
        repeat.setEnabled(!editing && !period.isSelected());
        for (JToggleButton d : days) {
            d.setEnabled(!editing && repeat.getSelectedIndex() == 2);
        }
        repeatEnd.setEnabled(repeating && !editing);
        repeatHint.setVisible(period.isSelected());
        LocalDate day = Ui.dateOf(date);
        boolean opened = start.isEnabled() && !startOpen;
        startOpen = start.isEnabled();
        if (!startTouched && (!day.equals(startDay) || opened)) {
            start.setValue(Times.value(DefaultDates.defaultStart(day, clock.get())));
        }
        startDay = day;
        // D-101: 앱이 골라 준 요일은 사용자가 단추를 누르기 전까지 날짜를 바꾸는 그 순간 따라간다.
        if (autoDay != null && autoDay != day.getDayOfWeek()) {
            boolean untouched = true;
            for (int i = 0; i < 7; i++) {
                untouched &= days[i].isSelected() == (i == autoDay.getValue() - 1);
            }
            autoDay = untouched ? day.getDayOfWeek() : null;
            if (untouched) {
                pick(autoDay);
            }
        }
        // D-79: 끝 날짜를 손대지 않았으면 반복을 고른 뒤에도 시작 날짜를 따라간다.
        if (!editing && !endTouched) {
            Ui.setDate(repeatEnd, DefaultDates.repeatEndFor(isFixed ? Kind.FIXED : Kind.TASK, day));
        }
        if (!dueTouched) {
            Ui.setDate(due, DefaultDates.dueFor(day, weekStart));
        }
        minutesHint.setVisible(!editing && (int) minutes.getValue() == 30);
        Integer s = startTime();
        endLabel.setText(s == null ? "" : Times.endHint(s, (int) minutes.getValue()));
        realTime.setText(s == null ? "" : Times.real(day, s));
        syncing = false;
        if (checked) {
            showErrors(draft().validate());
            return;
        }
        // 기간이 켜져 있으면 종류는 늘 할 일이라, 마감 오류는 마감이 시작일, 오늘, 정해 둔 날보다 전일 때만 난다(M2b).
        JLabel dueError = errors.get("마감");
        String late = draft().validate().stream().filter(p -> p.field().equals("마감")).map(FieldError::message).findFirst().orElse("");
        if (!late.isEmpty() || dueError.isVisible()) {
            fill(dueError, late);
            relayout.run();
        } else if (dueShown != due.isVisible()) {
            relayout.run();
        }
    }

    private void pick(DayOfWeek d) {
        for (int i = 0; i < 7; i++) {
            days[i].setSelected(i == d.getValue() - 1);
        }
    }

    /**
     * 칸에 쓴 값. 반복이 안 함이면 반복 방식과 끝은 null, 기간 안에 하기가 꺼져 있으면 마감은 null이다. 오늘은 앱의 오늘이고 editing은
     * 수정 창이면 참이다.
     */
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
        Integer s = startTime();
        return new TaskDraft(fixed.isSelected() ? Kind.FIXED : Kind.TASK, title.getText(),
                Category.values()[category.getSelectedIndex()], Ui.dateOf(date), period.isSelected() ? Ui.dateOf(due) : null,
                s == null ? null : LocalTime.of(s % Task.DAY / 60, s % 60), (int) minutes.getValue(), memo.getText(), how,
                how == null ? null : Ui.dateOf(repeatEnd), s != null && s >= Task.DAY, null, AppDay.of(clock.get()), editing);
    }

    /** D-42: 반복 사본을 고칠 때 반복 칸을 그 반복의 방식인 채(비활성) 그 반복의 마지막 날짜 end와 함께 보인다. */
    void showSeries(Repeat how, LocalDate end) {
        syncing = true;
        repeat.setSelectedIndex(how == null ? 0 : how.daily() ? 1 : 2);
        for (int i = 0; i < 7; i++) {
            days[i].setSelected(how != null && how.days().contains(DayOfWeek.of(i + 1)));
        }
        Ui.setDate(repeatEnd, end);
        syncing = false;
        sync();
    }

    /** 검사 결과를 칸 아래에 보인다. 고친 칸의 오류는 지운다. 이 뒤로는 칸을 고칠 때마다 다시 검사한다(D-43). */
    void showErrors(List<FieldError> problems) {
        checked = true;
        errors.values().forEach(l -> fill(l, ""));
        for (FieldError p : problems) {
            fill(errors.get(p.field()), p.message());
        }
        relayout.run();
    }

    /** 오류 줄 하나를 채운다. 빈 글이면 숨긴다. 원래 문장은 plain 속성에 둔다(errorText, error가 읽는다). */
    private void fill(JLabel l, String text) {
        // 보이는 제목 칸 폭이다. 창에 놓이기 전(창 없는 검사)에는 선호 폭을 쓴다.
        int room = title.getWidth() > 0 ? title.getWidth() : title.getPreferredSize().width;
        l.setText(text.isEmpty() ? "" : Ui.wrap(text, l, room));
        l.putClientProperty("plain", text);
        l.setVisible(!text.isEmpty());
    }

    /** 보이는 오류 글을 칸 차례대로 " / "로 잇는다. */
    String errorText() {
        return String.join(" / ", errors.keySet().stream().filter(k -> errors.get(k).isVisible()).map(this::error).toList());
    }

    /** 칸 아래 오류 글. 줄을 바꿔 그린 글도 원래 문장으로 돌려준다. 없으면 빈 글. */
    String error(String field) {
        Object plain = errors.get(field).getClientProperty("plain");
        return plain == null ? "" : (String) plain;
    }

    /** 시작 칸의 앱 하루의 분(D-77). 시각을 정하지 않았으면 null. */
    private Integer startTime() {
        return timed.isSelected() ? Times.minuteOf(start.getValue()) : null;
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
