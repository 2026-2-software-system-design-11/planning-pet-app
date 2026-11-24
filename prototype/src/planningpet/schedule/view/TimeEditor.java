package planningpet.schedule.view;

import planningpet.app.view.App;
import planningpet.common.view.*;
import planningpet.schedule.model.*;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Point;
import java.time.LocalDateTime;
import java.time.LocalTime;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.KeyStroke;
import javax.swing.SpinnerListModel;
import javax.swing.SwingUtilities;

/** 시각 고치기 창(05, D-32). 메인 시각 칸 아래에 떠서 시작 시각과 걸리는 시간만 고친다. */
public final class TimeEditor {
    public final JSpinner start = new JSpinner(new SpinnerListModel(Ui.times()));
    /** 다음 날 새벽 값이면 칸 옆에 실제 날과 시각 "(토 01:30)"(D-77). */
    public final JLabel realTime = new JLabel();
    public final JSpinner minutes;
    public final JLabel error = new JLabel(" ");
    public final JButton clear = new JButton("시각 빼기");
    final JButton cancel = new JButton("취소");
    final JButton save = new JButton("저장");
    public final Card panel = new Card(new BorderLayout(0, 6), Theme.BUBBLE_RADIUS);
    /** 창을 연 목록의 날. 시각 없는 기간 할 일에 시각을 정하면 날짜가 이 날로 옮겨진다(D-65). */
    final java.time.LocalDate day;

    /** 그 일정의 날짜 목록에서 연 창. */
    public TimeEditor(Task t, LocalDateTime now) {
        this(t, t.date, now);
    }

    public TimeEditor(Task t, java.time.LocalDate day, LocalDateTime now) {
        this.day = day;
        // D-67: 정한 날에 못 끝낸 기간 할 일은 시각 없는 할 일처럼 연다.
        boolean placed = t.onTimetable(planningpet.common.model.Rules.appDay(now));
        LocalTime from = placed ? t.start : DefaultDates.start(day, now);
        start.setValue(from.toString());
        minutes = Ui.minutes(t.minutes);
        error.setForeground(Theme.SUNDAY);
        error.setFont(Theme.font(12));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        // D-43: 맨 위에 할 일 제목, 버튼은 오른쪽 아래에 취소, 저장 순이고 시각 빼기는 왼쪽 아래다.
        JLabel name = new JLabel("<html><b>" + Ui.esc(t.title) + "</b></html>");
        JPanel fields = new JPanel(new BorderLayout(0, 4));
        fields.add(name, BorderLayout.NORTH);
        fields.add(Ui.flow(new JLabel("시작 시각"), start, realTime, new JLabel("걸리는 시간"), minutes, new JLabel("분")), BorderLayout.CENTER);
        panel.add(fields, BorderLayout.NORTH);
        panel.add(error, BorderLayout.CENTER);
        clear.setVisible(placed && t.kind == Task.Kind.TASK);
        JPanel buttons = new JPanel(new BorderLayout());
        buttons.add(Ui.flow(clear), BorderLayout.WEST);
        buttons.add(Ui.flow(cancel, save), BorderLayout.EAST);
        panel.add(buttons, BorderLayout.SOUTH);
    }

    /** 끝이 24:00을 넘으면 문장, 아니면 null. */
    public String check() {
        if ((int) minutes.getValue() % 5 != 0) {
            return "10분부터 720분까지, 5분 단위예요";
        }
        LocalTime s = LocalTime.parse((String) start.getValue());
        return s.getHour() * 60 + s.getMinute() + (int) minutes.getValue() > 24 * 60 ? "하루 안에 끝나야 해요" : null;
    }

    /** 저장 버튼과 같다. 통과하면 true, 아니면 창 아래 문장을 채우고 false. */
    public static boolean save(App app, Task t, TimeEditor e) {
        String problem = e.check();
        if (problem != null) {
            e.error.setText(problem);
            return false;
        }
        return app.schedule.saveTime(t, Times.minuteOf(e.start.getValue()), (int) e.minutes.getValue(), e.day);
    }

    /** 시각 빼기 버튼과 같다. 반복 사본의 범위 물음에서 취소하면 false(창은 열린 채다, D-72). */
    public static boolean clear(App app, Task t) {
        return app.schedule.clearTime(t);
    }

    public static void open(App app, Task t, java.time.LocalDate day, Component anchor) {
        TimeEditor e = new TimeEditor(t, day, app.store.now);
        JDialog d = new JDialog(SwingUtilities.getWindowAncestor(app.root), JDialog.ModalityType.APPLICATION_MODAL);
        d.setUndecorated(true);
        try {
            d.setBackground(new java.awt.Color(0, 0, 0, 0));
        } catch (UnsupportedOperationException | java.awt.IllegalComponentStateException ignored) {
            // 창 투명을 못 쓰는 환경이면 모서리 밖이 바탕색으로 보인다.
        }
        d.setContentPane(e.panel);
        e.cancel.addActionListener(x -> d.dispose());
        e.clear.addActionListener(x -> {
            if (clear(app, t)) {
                d.dispose();
            }
        });
        e.save.addActionListener(x -> {
            if (save(app, t, e)) {
                d.dispose();
            }
        });
        d.getRootPane().setDefaultButton(e.save);
        d.getRootPane().registerKeyboardAction(x -> d.dispose(),
                KeyStroke.getKeyStroke("ESCAPE"), JComponent.WHEN_IN_FOCUSED_WINDOW);
        d.pack();
        Point p = anchor.getLocationOnScreen();
        d.setLocation(p.x, p.y + anchor.getHeight() + 2);
        d.setVisible(true);
    }
}
