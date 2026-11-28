package planningpet.schedule.view;

import java.awt.BorderLayout;
import java.time.LocalDate;
import java.time.LocalDateTime;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerListModel;
import planningpet.common.model.AppDay;
import planningpet.common.view.Card;
import planningpet.common.view.Theme;
import planningpet.common.view.Ui;
import planningpet.schedule.model.DefaultDates;
import planningpet.schedule.model.Kind;
import planningpet.schedule.model.Task;

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

    /** 할 일 task를 창을 연 목록의 날 day와 앱 시계의 지금 now로 연다. */
    TimeEditor(Task task, LocalDate day, LocalDateTime now) {
        this.day = day;
        // D-67: 정한 날에 못 끝낸 기간 할 일은 시각 없는 할 일처럼 연다.
        boolean placed = task.onTimetable(AppDay.of(now));
        int from = placed ? task.startMinute() : DefaultDates.defaultStart(day, now);
        start.setValue(Times.value(from));
        base = placed ? task.getDate() : day;
        realTime.setText(Times.real(base, from));
        minutes = Ui.minutes(task.getDurationMinutes());
        start.addChangeListener(e -> realTime.setText(Times.real(base, Times.minuteOf(start.getValue()))));
        clear.setVisible(placed && task.getKind() == Kind.TASK);
    }
}
