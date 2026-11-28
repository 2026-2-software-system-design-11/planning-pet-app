package planningpet.schedule.view;

import java.awt.Cursor;
import javax.swing.JButton;
import planningpet.common.view.Theme;

/** 할 일 줄의 시각 알약 버튼(05 입력 항목 7, D-59). 글은 시각이고 없으면 "시각 정하기"다(docs/porting/task-rows.md S3s). */
final class TimePill extends JButton {
    /** locked면 누를 수 없는 흐린 알약이고(D-94) 마우스를 올렸을 때 why를 보인다. 끝낸 줄과 지난 날 목록의 기간 할 일이 그렇다. */
    TimePill(String time, boolean done, boolean locked, String why) {
        super(time.isEmpty() ? "시각 정하기" : time);
        setEnabled(!locked);
        setFont(Theme.bold(12));
        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setRolloverEnabled(true);
        if (!locked) {
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        setToolTipText(locked ? why : "눌러서 시각을 바꿔요");
    }
}
