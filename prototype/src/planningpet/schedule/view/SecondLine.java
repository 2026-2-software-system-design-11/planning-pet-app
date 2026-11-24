package planningpet.schedule.view;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * 일정관리 목록 줄의 둘째 줄(D-44, D-94). 왼쪽에 표시("~11/22까지", "오늘 마감", "지난 날"), 오른쪽에 수정과 삭제 버튼이다. 목록이
 * 좁아 둘이 한 줄에 들지 않으면 표시를 한 줄 위에 두고 버튼을 그 아래 오른쪽에 둔다. 표시를 줄여 숨기지 않는다.
 * 일정관리 오른쪽 칸은 폭이 240으로 늘 같으므로, 세로 막대가 생겨 가장 좁을 때의 줄 폭으로 정한다. 지금 폭으로 정하면 늘어놓을 때마다
 * 높이와 배치가 어긋났다.
 */
final class SecondLine implements LayoutManager {
    private final JComponent marks;
    private final JComponent buttons;
    /** 세로 막대가 있을 때 일정관리 목록 둘째 줄의 폭. */
    private static final int WIDTH = 170;

    /** marks는 없으면 null. 표시가 한 줄 폭보다 길면 그 폭에서 줄을 바꾼다. */
    static JPanel of(javax.swing.JLabel marks, JComponent buttons) {
        if (marks != null && marks.getPreferredSize().width > WIDTH) {
            int text = WIDTH - marks.getInsets().left;
            marks.setText(marks.getText().replace("<html>", "<html><body style='width:" + text + "px'>"));
        }
        JPanel line = new JPanel();
        line.setLayout(new SecondLine(marks, buttons));
        if (marks != null) {
            line.add(marks);
        }
        line.add(buttons);
        return line;
    }

    private SecondLine(JComponent marks, JComponent buttons) {
        this.marks = marks;
        this.buttons = buttons;
    }

    private boolean stacked() {
        return marks != null && marks.getPreferredSize().width + buttons.getPreferredSize().width > WIDTH;
    }

    @Override
    public Dimension preferredLayoutSize(Container c) {
        Dimension m = marks == null ? new Dimension() : marks.getPreferredSize();
        Dimension b = buttons.getPreferredSize();
        return stacked() ? new Dimension(Math.max(m.width, b.width), m.height + b.height)
                : new Dimension(m.width + b.width, Math.max(m.height, b.height));
    }

    @Override
    public Dimension minimumLayoutSize(Container c) {
        return preferredLayoutSize(c);
    }

    @Override
    public void layoutContainer(Container c) {
        int w = c.getWidth();
        Dimension b = buttons.getPreferredSize();
        if (marks == null) {
            buttons.setBounds(w - b.width, 0, b.width, b.height);
            return;
        }
        Dimension m = marks.getPreferredSize();
        if (stacked()) {
            marks.setBounds(0, 0, Math.min(m.width, w), m.height);
            buttons.setBounds(w - b.width, m.height, b.width, b.height);
        } else {
            int h = Math.max(m.height, b.height);
            marks.setBounds(0, (h - m.height) / 2, m.width, m.height);
            buttons.setBounds(w - b.width, (h - b.height) / 2, b.width, b.height);
        }
    }

    @Override
    public void addLayoutComponent(String name, Component comp) {
    }

    @Override
    public void removeLayoutComponent(Component comp) {
    }
}
