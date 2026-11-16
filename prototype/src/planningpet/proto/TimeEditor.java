package planningpet.proto;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Point;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.stream.IntStream;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.KeyStroke;
import javax.swing.SpinnerListModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

/** 시각 고치기 창(05, D-32). 메인 시각 칸 아래에 떠서 시작 시각과 걸리는 시간만 고친다. */
public final class TimeEditor {
    final JSpinner start = new JSpinner(new SpinnerListModel(IntStream.range(0, 144)
            .mapToObj(i -> String.format("%02d:%02d", i / 6, i % 6 * 10)).toList()));
    final JSpinner minutes;
    final JLabel error = new JLabel(" ");
    final JButton clear = new JButton("시간 빼기");
    final JButton cancel = new JButton("취소");
    final JButton save = new JButton("저장");
    public final Card panel = new Card(new BorderLayout(0, 6), Theme.BUBBLE_RADIUS);

    public TimeEditor(Task t, LocalDateTime now) {
        LocalTime from = t.timed() ? t.start : defaultStart(t.date, now);
        start.setValue(from.toString());
        minutes = new JSpinner(new SpinnerNumberModel(t.minutes, 10, 720, 10));
        error.setForeground(Theme.SUNDAY);
        error.setFont(Theme.font(12));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        panel.add(Ui.flow(new JLabel("시작"), start, new JLabel("걸리는 시간"), minutes, new JLabel("분")), BorderLayout.NORTH);
        panel.add(error, BorderLayout.CENTER);
        clear.setVisible(t.timed() && t.kind == Task.Kind.TASK);
        panel.add(Ui.flow(clear, cancel, save), BorderLayout.SOUTH);
    }

    /** 06의 14번. 오늘이면 지금을 10분 단위로 올린 시각과 09:00 중 늦은 것, 다른 날이면 09:00. */
    public static LocalTime defaultStart(LocalDate date, LocalDateTime now) {
        if (!date.equals(now.toLocalDate())) {
            return LocalTime.of(9, 0);
        }
        int m = now.getHour() * 60 + now.getMinute() + (now.getSecond() > 0 ? 1 : 0);
        m = Math.min(Math.max((m + 9) / 10 * 10, 9 * 60), 23 * 60 + 50);
        return LocalTime.of(m / 60, m % 60);
    }

    /** 끝이 24:00을 넘으면 문장, 아니면 null. */
    String check() {
        if ((int) minutes.getValue() % 10 != 0) {
            return "10분부터 720분까지, 10분 단위예요";
        }
        LocalTime s = LocalTime.parse((String) start.getValue());
        return s.getHour() * 60 + s.getMinute() + (int) minutes.getValue() > 24 * 60 ? "하루 안에 끝나야 해요" : null;
    }

    /** 저장 버튼과 같다. 통과하면 true, 아니면 창 아래 문장을 채우고 false. */
    static boolean save(App app, Task t, TimeEditor e) {
        String problem = e.check();
        if (problem != null) {
            e.error.setText(problem);
            return false;
        }
        t.start = LocalTime.parse((String) e.start.getValue());
        t.minutes = (int) e.minutes.getValue();
        app.store.changed();
        return true;
    }

    /** 시간 빼기 버튼과 같다. */
    static void clear(App app, Task t) {
        t.start = null;
        app.store.changed();
    }

    public static void open(App app, Task t, Component anchor) {
        TimeEditor e = new TimeEditor(t, app.store.now);
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
            clear(app, t);
            d.dispose();
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
