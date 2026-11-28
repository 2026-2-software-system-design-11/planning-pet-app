package planningpet.schedule.view;

import java.awt.BorderLayout;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.function.BiPredicate;
import java.util.function.Function;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerListModel;
import planningpet.common.model.AppDay;
import planningpet.common.view.Card;
import planningpet.common.view.Theme;
import planningpet.common.view.Ui;
import planningpet.schedule.model.DefaultDates;
import planningpet.schedule.model.EditScope;
import planningpet.schedule.model.Kind;
import planningpet.schedule.model.Task;
import planningpet.schedule.model.TaskDraft;

/**
 * 시각 고치기 창(05 시각 고치기 창, D-32, UC-05). 시작 시각과 걸리는 시간만 고친다. 창 내용은 창 없이 만들어 검사하고, 띄우기는
 * S5f다(docs/porting/schedule.md S5e). 쳐 넣은 글 읽기(C1)의 칸 아래 글은 두지 않아 두 칸은 Swing 기본 편집기다.
 */
public final class TimeEditor {
    final JSpinner start = new JSpinner(new SpinnerListModel(Times.values()));
    /** 다음 날 새벽 값이면 칸 옆에 실제 날과 시각 "(토 01:30)"(D-77). */
    final JLabel realTime = new JLabel();
    final JSpinner minutes;
    final JLabel error = new JLabel(" ");
    final JButton clear = new JButton("시각 빼기");
    final JButton cancel = new JButton("취소");
    final JButton save = new JButton("저장");
    final Card panel = new Card(new BorderLayout(0, 6), Theme.BUBBLE_RADIUS);
    /** 창을 연 목록의 날. 시각 없는 기간 할 일에 시각을 정하면 날짜가 이 날로 옮겨진다(D-65). */
    final LocalDate day;
    /** 실제 시각 글의 날. 시간표에 놓인 일은 그 날짜, 아니면 창을 연 날이다. */
    private final LocalDate base;
    private final Task task;
    /**
     * 저장 함수. 할 일과 창 값을 받아 창을 닫아도 되면 참이다. 겹침이나 범위 물음에서 취소하면 거짓이다. S5f가 TaskController.update를
     * 넣고, 저장에 실패하면 거짓을 돌려 창을 남긴다(O1, 05, 06이 정하지 않음, 페르소나 투표 전 잠정).
     */
    private final BiPredicate<Task, TaskDraft> onSave;
    /** 시각 빼기 함수. 할 일과 범위를 받아 창을 닫아도 되면 참이다. S5f가 TaskController.clearTime을 넣는다(저장 실패는 onSave와 같다). */
    private final BiPredicate<Task, EditScope> onClear;
    /** 반복 사본의 시각 빼기 범위를 묻는다. 취소나 창 닫기는 null이다. 검사는 답을 바꿔 넣는다. */
    Function<Task, EditScope> askClear = t -> TaskDialog.chooseClear(panel, t);
    /** 창을 닫는다. 띄우는 쪽(S5f)이 넣는다. */
    Runnable close = () -> { };

    /** 할 일 task를 창을 연 목록의 날 day와 앱 시계의 지금 now로 연다. */
    TimeEditor(Task task, LocalDate day, LocalDateTime now, BiPredicate<Task, TaskDraft> onSave,
            BiPredicate<Task, EditScope> onClear) {
        this.day = day;
        this.task = task;
        this.onSave = onSave;
        this.onClear = onClear;
        // D-67: 정한 날에 못 끝낸 기간 할 일은 시각 없는 할 일처럼 연다.
        boolean placed = task.onTimetable(AppDay.of(now));
        int from = placed ? task.startMinute() : DefaultDates.defaultStart(day, now);
        start.setValue(Times.value(from));
        base = placed ? task.getDate() : day;
        realTime.setText(Times.real(base, from));
        minutes = Ui.minutes(task.getDurationMinutes());
        start.addChangeListener(e -> realTime.setText(Times.real(base, Times.minuteOf(start.getValue()))));
        error.setForeground(Theme.SUNDAY);
        error.setFont(Theme.font(12));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        // D-43: 맨 위에 할 일 제목, 단추는 오른쪽 아래에 취소, 저장 차례이고 시각 빼기는 왼쪽 아래다.
        JPanel fields = new JPanel(new BorderLayout(0, 4));
        fields.add(new JLabel("<html><b>" + Ui.esc(task.getTitle()) + "</b></html>"), BorderLayout.NORTH);
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.add(Ui.flow(new JLabel("시작 시각"), start, realTime), BorderLayout.WEST);
        row.add(Ui.flow(new JLabel("걸리는 시간"), minutes, new JLabel("분")), BorderLayout.CENTER);
        fields.add(row, BorderLayout.CENTER);
        panel.add(fields, BorderLayout.NORTH);
        panel.add(error, BorderLayout.CENTER);
        clear.setVisible(placed && task.getKind() == Kind.TASK);
        JPanel buttons = new JPanel(new BorderLayout());
        buttons.add(Ui.flow(clear), BorderLayout.WEST);
        buttons.add(Ui.flow(cancel, save), BorderLayout.EAST);
        panel.add(buttons, BorderLayout.SOUTH);
        cancel.addActionListener(e -> close.run());
        save.addActionListener(e -> save());
        clear.addActionListener(e -> clear());
    }

    /** 끝이 다음 날 04:00을 넘으면 문장, 아니면 null(D-77). */
    String check() {
        return Times.minuteOf(start.getValue()) + (int) minutes.getValue() > Task.DAY_END ? TaskDraft.END_PROBLEM : null;
    }

    /**
     * 창 값(timeOnly). 날짜는 옮겨 갈 날이라 기간 할 일은 창을 연 날, 그 밖은 그 일의 날짜다. 마감 칸은 비운다(task-edit.md "07 안에서
     * 정한 것"). 제목, 유형, 메모는 그 일의 값이고 update의 timeOnly 길은 쓰지 않는다.
     */
    TaskDraft draft() {
        int s = Times.minuteOf(start.getValue());
        return new TaskDraft(task.getKind(), task.getTitle(), task.getCategory(), task.period() ? day : task.getDate(), null,
                LocalTime.of(s % Task.DAY / 60, s % 60), (int) minutes.getValue(), task.getMemo(), null, null, s >= Task.DAY,
                null, null, true, true);
    }

    /** 저장 단추. 검사에 걸리면 창 아래 문장만 채우고, 통과하면 저장 함수가 참일 때 닫는다. */
    private void save() {
        String problem = check();
        if (problem != null) {
            error.setText(problem);
        } else if (onSave.test(task, draft())) {
            close.run();
        }
    }

    /** 시각 빼기 단추. 반복 사본이 아니면 묻지 않고 ONLY_THIS다. 범위 물음에서 취소하면 아무것도 하지 않고 창이 남는다(D-72). */
    private void clear() {
        EditScope scope = task.getSeriesId() == null ? EditScope.ONLY_THIS : askClear.apply(task);
        if (scope != null && onClear.test(task, scope)) {
            close.run();
        }
    }
}
