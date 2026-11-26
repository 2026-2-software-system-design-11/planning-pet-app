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
    public final JSpinner start = new JSpinner(new SpinnerListModel(Times.values()));
    /** 다음 날 새벽 값이면 시작 칸 옆에 적는 실제 날과 시각 "(토 01:30)"(D-77). */
    public final JLabel realTime = new JLabel();
    public final JSpinner minutes = Ui.minutes(30);
    /** 시작 시각 칸에 쳐 넣은 값을 맞췄거나 받지 않았을 때 칸 아래 글(D-98). */
    private final TypedField startTyped = new TypedField(start, x -> moved(Typed.start(x, night() != null ? this.nightFrom : Ui.dateOf(date))),
            () -> this.relayout.run());
    public final JLabel startNotice = startTyped.notice;
    /** "밤 4시"로 날짜를 옮기기 전의 날(D-98). moving은 그 날짜를 옮기는 동안이다. */
    private LocalDate nightFrom;
    private boolean moving;
    /** 걸리는 시간 칸에 쳐 넣은 값을 맞췄거나 받지 않았을 때 칸 아래 글(D-98). 쳐 넣은 뒤에는 30분 안내를 숨긴다. */
    private final TypedField minutesTyped = new TypedField(minutes, Typed::minutes, () -> { sync(); this.relayout.run(); });
    public final JLabel minutesNotice = minutesTyped.notice;
    /** 7번 반복(D-64): 안 함, 매일, 요일. 요일이면 요일 단추 일곱(월부터 일)에서 하나 이상 고른다. */
    public final JComboBox<String> repeat = new JComboBox<>(new String[] {"안 함", "매일", "요일"});
    public final JToggleButton[] days = new JToggleButton[7];
    public final JSpinner repeatEnd = new JSpinner(new SpinnerDateModel());
    /** 11번 기간 안에 하기와 마감(D-65). 마감 칸은 켤 때만 보인다. */
    public final JCheckBox period = new JCheckBox("기간 안에 하기");
    public final JLabel dueLabel = new JLabel("마감일");
    /** 날짜 칸 이름. 기간 안에 하기를 켜면 "시작일"이다(D-73). */
    public JLabel dateLabel;
    /** 반복을 골라 기간 안에 하기가 흐려지면 옆에 늘 보이는 까닭(D-73). */
    public final JLabel periodHint = Ui.hint("반복 일정은 기간을 정할 수 없어요");
    /** 반복 끝 날짜를 사용자가 고쳤는가. 고치기 전에는 시작 날짜를 따라간다(D-79). */
    private boolean endTouched;
    /** 마감을 사용자가 골랐는가(저장해 둔 마감 포함). 고른 뒤에는 날짜나 종류를 바꾸고 기간을 껐다 켜도 다시 채우지 않는다(D-96). */
    private boolean dueTouched;
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
    /** 반복 사본을 고치는 중(기간 칸은 꺼진 채 비활성, D-65). */
    private final boolean copy;
    /** sync 안에서 칸 값을 바꿀 때 다시 sync하지 않는다. */
    private boolean syncing;
    /** 저장을 한 번 누른 뒤에는 칸을 고칠 때마다 다시 검사한다(D-43). */
    private boolean checked;
    /** D-101: 요일을 한 번 고른 뒤에는 다른 반복을 거쳐 돌아와도 단추를 그대로 둔다. */
    private boolean daysShown;
    /** D-101: 앱이 골라 준 요일. 사용자가 요일 단추를 누르면 null이 되고 날짜를 따라가지 않는다. */
    private java.time.DayOfWeek autoDay;
    /** D-101: 고정 일정으로 가기 앞의 시작 시각 정하기, 기간 안에 하기, 마감. 할 일로 돌아오면 되돌리고 null이 된다. */
    private Boolean timedBefore;
    private boolean periodBefore;
    private LocalDate dueBefore;
    /** D-101: 기간 때문에 앱이 끄기 앞의 정하기. 기간을 끄면 되돌리고 null이 된다. */
    private Boolean timedBeforePeriod;
    /** D-101: 반복 때문에 앱이 끈 기간 할 일의 마감. 반복을 안 함으로 되돌리면 기간과 함께 되돌리고 null이 된다. */
    private LocalDate dueBeforeRepeat;
    /** 오류 글이 늘거나 줄면 창 크기를 다시 맞춘다. 창 없이 만들 때는 아무것도 하지 않는다. */
    public Runnable relayout = () -> { };
    private int row;

    /** S129(D-101): 날짜를 바꾼 그 순간의 시계. 앱 창은 앱 시계(Store.now)를 넣는다. 처음은 창을 연 때다. */
    public java.util.function.Supplier<java.time.LocalDateTime> clock;
    /** S129(D-101): 사용자가 시작 시각을 만졌는지. 만지기 전에는 날짜의 기본 시작 시각을 따라간다. */
    private boolean startTouched;
    /** S129: 시작 시각을 마지막으로 잰 날짜. 날짜가 바뀐 때만 다시 잰다. 처음 sync에서 잰다. */
    private LocalDate startDay;
    /** S144: 지난 sync에서 시작 칸을 쓸 수 있었는지. 쓸 수 있게 되는 순간에도 다시 잰다. */
    private boolean startOpen;

    /** 마감 기본값이 쓰는 한 주의 시작 요일(D-91). */
    private final java.time.DayOfWeek weekStart;

    /** 한 주 시작이 월요일일 때. 앱의 창은 설정 값을 받는 아래 생성자를 쓴다. */
    public TaskForm(Task t, LocalDate defaultDate, java.time.LocalDateTime now) {
        this(t, defaultDate, now, java.time.DayOfWeek.MONDAY);
    }

    public TaskForm(Task t, LocalDate defaultDate, java.time.LocalDateTime now, java.time.DayOfWeek weekStart) {
        this.weekStart = weekStart;
        clock = () -> now;
        // D-101: 수정 창에서 저장해 둔 시각은 사용자 값이다. 시각 없는 일정의 시작 칸은 앱이 채운 값이라 날짜를 따라간다.
        startTouched = t != null && t.timed();
        editing = t != null;
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
            days[i].addActionListener(e -> {
                autoDay = null;
                sync();
            });
        }
        start.setValue(Times.value(DefaultDates.start(defaultDate, now)));
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
        dateLabel = line("날짜", date);
        line("기간", Ui.flow(period, periodHint, dueLabel, due));
        line("", errors.get("마감"));
        line("시작 시각", Ui.flow(timed, start, realTime));
        // 칸 아래 글 줄은 이름 칸을 숨겨, 글이 없을 때 줄 여백도 차지하지 않는다.
        line("", startNotice).setVisible(false);
        line("", errors.get("시작 시각"));
        line("걸리는 시간 (분)", Ui.flow(minutes, endLabel));
        line("", minutesNotice).setVisible(false);
        line("", errors.get("걸리는 시간"));
        line("", minutesHint);
        JComponent[] repeatParts = new JComponent[8];
        repeatParts[0] = repeat;
        // D-94: 요일 단추는 한 주 시작 요일부터 놓는다. days[i]는 그대로 월요일부터다(DayOfWeek.of(i + 1)).
        for (int k = 0; k < 7; k++) {
            repeatParts[k + 1] = days[(weekStart.getValue() - 1 + k) % 7];
        }
        line("반복", Ui.flow(repeatParts));
        line("", Ui.flow(new JLabel("반복 끝나는 날"), repeatEnd));
        line("", errors.get("반복"));
        line("", repeatHint);
        line("메모", new JScrollPane(memo));
        line("", errors.get("메모"));
        category.setSelectedItem(Categories.OTHER);
        title.setText(t == null ? "" : t.title);
        // D-66: 기간 할 일의 날짜 칸은 기간 첫날이다(계획한 날이 아니다).
        LocalDate first = t == null ? defaultDate : t.period() ? t.startDay : t.date;
        Ui.setDate(date, first);
        Ui.setDate(due, t != null && t.dueDate != null ? t.dueDate : DefaultDates.due(first, weekStart));
        period.setSelected(t != null && t.dueDate != null);
        // D-96: 수정 창에서 저장해 둔 마감도 고른 마감이다.
        dueTouched = t != null && t.dueDate != null;
        if (t != null) {
            fixed.setSelected(t.kind == Task.Kind.FIXED);
            timed.setSelected(t.timed() && !t.period());
            if (t.timed()) {
                start.setValue(Times.value(t.startMinute()));
            }
            minutes.setValue(t.minutes);
            memo.setText(t.memo);
            category.setSelectedItem(t.category);
        }
        Ui.setDate(repeatEnd, DefaultDates.repeatEnd(fixed.isSelected() ? Task.Kind.FIXED : Task.Kind.TASK, Ui.dateOf(date)));
        task.addActionListener(e -> sync());
        fixed.addActionListener(e -> sync());
        timed.addActionListener(e -> sync());
        repeat.addActionListener(e -> {
            if (!syncing && repeat.getSelectedIndex() == 2 && !daysShown) {
                // 요일을 처음 고르면 날짜의 요일이 골라져 있다(D-93, 05 일정 입력 창 7번).
                daysShown = true;
                autoDay = Ui.dateOf(date).getDayOfWeek();
                pick(autoDay);
            }
            sync();
        });
        period.addActionListener(e -> sync());
        due.addChangeListener(e -> {
            if (!syncing) {
                dueTouched = true;
            }
            sync();
        });
        start.addChangeListener(e -> {
            if (!syncing) {
                startTouched = true;
            }
            sync();
        });
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
        repeatEnd.addChangeListener(e -> {
            if (!syncing) {
                endTouched = true;
            }
            sync();
        });
        date.addChangeListener(e -> {
            // D-98: 사용자가 날짜 칸을 손으로 바꾸면 그 뒤로는 옮긴 날로 다시 돌려 놓아도 사용자가 고른 날이다.
            if (!moving && !syncing) {
                nightFrom = null;
            }
            sync();
        });
        sync();
    }

    /**
     * 고정 일정이면 시작 시각이 켜진 채 비활성이고 기간은 꺼진 채 비활성이다(D-16, D-65). 반복과 기간은 함께 고를 수 없다(D-65).
     * 반복이 안 함인 동안 끝 날짜는, 마감을 고르기 전에는 기간을 켰든 껐든 마감이 날짜를 따라간다(06의 14번). 고른 마감은 따라가지 않는다(D-96).
     */
    public void sync() {
        if (syncing) {
            return;
        }
        syncing = true;
        boolean dueShown = due.isVisible();
        // D-98, D-101: "밤 4시"로 옮긴 날짜는 "정하기"를 끄거나 시작 칸을 다른 값으로 바꾸면 원래 날로 돌아간다.
        if (!moving && night() != null && (!timed.isSelected() || startTyped.days(start.getValue()) == 0)) {
            Ui.setDate(date, nightFrom);
            nightFrom = null;
            if (!timed.isSelected()) {
                startTyped.clear();
            }
        }
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
            start.setValue(Times.value(DefaultDates.start(day, clock.get())));
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
            Ui.setDate(repeatEnd, DefaultDates.repeatEnd(isFixed ? Task.Kind.FIXED : Task.Kind.TASK, day));
        }
        if (!dueTouched) {
            Ui.setDate(due, DefaultDates.due(day, weekStart));
        }
        minutesHint.setVisible(!editing && (int) minutes.getValue() == 30 && !minutesTyped.typed);
        Integer s = startTime();
        endLabel.setText(s == null ? "" : Times.endHint(s, (int) minutes.getValue()));
        realTime.setText(s == null ? "" : Times.real(day, s));
        // S102, S128(D-101): 마감 칸은 저장 전에도 값이 바뀔 때마다 바로 검사한다. 다른 칸은 저장 뒤다(D-43).
        boolean dueLate = period.isSelected() && Ui.dateOf(due).isBefore(day);
        syncing = false;
        JLabel dueError = errors.get("마감");
        if (checked) {
            showErrors(problems());
        } else if (dueLate || dueError.isVisible()) {
            fill(dueError, problems().stream().filter(p -> dueLate && p[0].equals("마감")).map(p -> p[1]).findFirst().orElse(""));
            relayout.run();
        } else if (dueShown != due.isVisible()) {
            relayout.run();
        }
    }

    private void pick(java.time.DayOfWeek d) {
        for (int i = 0; i < 7; i++) {
            days[i].setSelected(i == d.getValue() - 1);
        }
    }

    /** "밤 4시"처럼 다음 날 시각으로 읽으면 날짜 칸을 그만큼 뒤로 옮긴다(D-98). */
    private Typed.Read moved(Typed.Read r) {
        if (r.days() > 0) {
            // D-98: 다시 쳐도 처음 옮긴 날 그대로다.
            nightFrom = night() != null ? nightFrom : Ui.dateOf(date);
            moving = true;
            Ui.setDate(date, nightFrom.plusDays(r.days()));
            moving = false;
        }
        return r;
    }

    /** "밤 4시"로 옮긴 날짜가 그대로면 옮기기 전의 날, 아니면(사용자가 날짜를 바꾼 적이 있음) null. */
    private LocalDate night() {
        return nightFrom != null && Ui.dateOf(date).equals(nightFrom.plusDays(1)) ? nightFrom : null;
    }

    /** 06의 2번 검사. */
    public java.util.List<String[]> problems() {
        return TaskCheck.check(draft(), editing);
    }

    public boolean editing() {
        return editing;
    }

    /** 칸에 쓴 값. 반복이 안 함이면 반복 방식과 끝은 null, 기간 안에 하기가 꺼져 있으면 마감은 null이다. */
    public TaskDraft draft() {
        Repeat how = null;
        if (repeat.getSelectedIndex() == 1) {
            how = Repeat.DAILY;
        } else if (repeat.getSelectedIndex() == 2) {
            java.util.List<java.time.DayOfWeek> picked = new java.util.ArrayList<>();
            for (int i = 0; i < 7; i++) {
                if (days[i].isSelected()) {
                    picked.add(java.time.DayOfWeek.of(i + 1));
                }
            }
            how = Repeat.weekly(picked);
        }
        Integer s = startTime();
        return new TaskDraft(fixed.isSelected() ? Task.Kind.FIXED : Task.Kind.TASK, title.getText(),
                (String) category.getSelectedItem(), Ui.dateOf(date), period.isSelected() ? Ui.dateOf(due) : null,
                s == null ? null : LocalTime.of(s % Task.DAY / 60, s % 60), (int) minutes.getValue(), memo.getText(), how,
                how == null ? null : Ui.dateOf(repeatEnd), s != null && s >= Task.DAY);
    }

    /** D-42: 반복 사본을 고칠 때 반복 칸을 그 반복의 방식인 채(비활성) 그 반복의 마지막 날짜와 함께 보인다. */
    public void showSeries(Repeat how, LocalDate end) {
        syncing = true;
        repeat.setSelectedIndex(how == null ? 0 : how.daily() ? 1 : 2);
        for (int i = 0; i < 7; i++) {
            days[i].setSelected(how != null && how.days().contains(java.time.DayOfWeek.of(i + 1)));
        }
        Ui.setDate(repeatEnd, end);
        syncing = false;
        sync();
    }

    /** 검사 결과를 칸 아래에 보인다. 고친 칸의 오류는 지운다. */
    public void showErrors(java.util.List<String[]> problems) {
        checked = true;
        errors.values().forEach(l -> fill(l, ""));
        for (String[] p : problems) {
            fill(errors.get(p[0]), p[1]);
        }
        relayout.run();
    }

    /** 오류 줄 하나를 채운다. 빈 글이면 숨긴다. 원래 문장은 plain 속성에 둔다(errorText, error가 읽는다). */
    private void fill(JLabel l, String text) {
        // 보이는 제목 칸 폭이다. 창에 놓이기 전(창 없는 검사)에는 선호 폭을 쓴다.
        int room = title.getWidth() > 0 ? title.getWidth() : title.getPreferredSize().width;
        l.setText(text.isEmpty() ? "" : Ui.wrap(text, l.getFontMetrics(l.getFont()), room));
        l.putClientProperty("plain", text);
        l.setVisible(!text.isEmpty());
    }

    /** 보이는 오류 글을 칸 순서대로 " / "로 잇는다. */
    public String errorText() {
        return String.join(" / ", errors.keySet().stream().filter(k -> errors.get(k).isVisible()).map(this::error).toList());
    }

    /** 칸 아래 오류 글. 줄을 바꿔 그린 글도 원래 문장으로 돌려준다. 없으면 빈 글. */
    public String error(String field) {
        Object plain = errors.get(field).getClientProperty("plain");
        return plain == null ? "" : (String) plain;
    }

    /** 시작 칸의 앱 하루의 분(D-77). 시각을 정하지 않았으면 null. */
    private Integer startTime() {
        return timed.isSelected() ? Times.minuteOf(start.getValue()) : null;
    }

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
