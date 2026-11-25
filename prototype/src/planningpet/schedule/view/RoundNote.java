package planningpet.schedule.view;

import planningpet.common.view.Ui;
import javax.swing.JLabel;
import javax.swing.JSpinner;

/**
 * D-95: 칸에 5분 단위가 아닌 값을 쳐 넣으면 칸이 가까운 5분 값으로 맞추고, 이 글이 칸 아래에 "5분 단위로 맞췄어요(25분)"를
 * 보인다. 칸 값이 다시 바뀌면 숨는다.
 */
public final class RoundNote {
    public final JLabel label = Ui.hint(" ");
    /** 시작 시각 칸이 맞춘 값. 칸 값이 이 값으로 바뀌면 글을 보인다. */
    private String pending;
    /** 글이 보이거나 숨으면 창 크기를 다시 맞춘다. */
    private final Runnable relayout;

    private RoundNote(JSpinner field, Runnable relayout) {
        this.relayout = relayout;
        label.setVisible(false);
        field.addChangeListener(e -> {
            set(field.getValue().equals(pending) ? pending : null);
            pending = null;
        });
    }

    /** 시작 시각 칸(Times.values 목록). 맞춘 값이 지금 값과 같으면 칸 값이 바뀌지 않으므로 바로 보인다. */
    public static RoundNote start(JSpinner field, Runnable relayout) {
        RoundNote n = new RoundNote(field, relayout);
        Times.roundTyped(field, v -> {
            if (v.equals(field.getValue())) {
                n.set(v);
            } else {
                n.pending = v;
            }
        });
        return n;
    }

    /** 걸리는 시간 칸(Ui.minutes). 맞춘 값이 칸에 들어간 뒤에 글을 보인다. */
    public static RoundNote minutes(JSpinner field, Runnable relayout) {
        RoundNote n = new RoundNote(field, relayout);
        ((Ui.Minutes) field.getModel()).rounded = m -> n.set(m + "분");
        return n;
    }

    /** value가 null이면 숨긴다. */
    private void set(String value) {
        boolean show = value != null;
        if (show) {
            label.setText("5분 단위로 맞췄어요(" + value + ")");
        }
        if (label.isVisible() != show) {
            label.setVisible(show);
            relayout.run();
        }
    }
}
