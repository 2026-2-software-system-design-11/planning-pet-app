package planningpet.proto;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** 할 일 줄 목록. 메인의 하루 시간표와 일정관리의 시각 없는 할 일 목록이 같이 쓴다. */
public final class TaskRows extends JPanel {
    private final Actions actions;

    public TaskRows(Actions actions) {
        this.actions = actions;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    /** 메인(D-31): 시각 있는 일정을 시각과 함께, 그 아래 "시간 안 정한 할 일". */
    public void showPlan(Store.DayPlan plan) {
        removeAll();
        plan.timed().forEach(t -> add(row(t, Store.timeText(t), false)));
        if (!plan.untimed().isEmpty()) {
            JLabel head = Ui.hint("시간 안 정한 할 일");
            head.setBorder(BorderFactory.createEmptyBorder(10, 0, 2, 0));
            add(head);
            plan.untimed().forEach(t -> add(row(t, "", false)));
        }
        revalidate();
        repaint();
    }

    /** 일정관리 오른쪽 목록: 날짜를 붙인다. */
    public void show(List<Task> tasks, boolean withDate) {
        removeAll();
        tasks.forEach(t -> add(row(t, null, withDate)));
        revalidate();
        repaint();
    }

    /** time이 null이면 시각 칸이 없고, ""이면 빈 시각 칸이다. 끝낸 할 일은 제자리에서 회색 취소선이 된다. */
    private JPanel row(Task t, String time, boolean withDate) {
        JPanel row = new JPanel(new BorderLayout(6, 0));
        row.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        row.setAlignmentX(LEFT_ALIGNMENT);
        JComponent mark;
        if (t.kind == Task.Kind.FIXED) {
            JLabel tag = new JLabel("고정");
            tag.setFont(Theme.font(11));
            tag.setOpaque(true);
            tag.setBackground(Theme.TRACK);
            tag.setBorder(BorderFactory.createLineBorder(Theme.CARD_LINE));
            mark = tag;
        } else {
            JCheckBox box = new JCheckBox();
            box.setSelected(t.done);
            box.addActionListener(e -> actions.check(t));
            mark = box;
        }
        JPanel left = Ui.flow();
        if (time != null) {
            JLabel when = new JLabel(time);
            when.setPreferredSize(new Dimension(92, 20));
            when.setForeground(t.done ? Theme.INK_DIM : Theme.INK);
            left.add(when);
        }
        left.add(mark);
        String date = withDate ? t.date.getMonthValue() + "/" + t.date.getDayOfMonth() + " " : "";
        JLabel label = new JLabel("<html>" + date + (t.done ? "<s>" + t.title + "</s>" : t.title) + "</html>");
        label.setFont(Theme.font(15));
        JLabel minutes = new JLabel(t.minutes + "분");
        label.setForeground(t.done ? Theme.INK_DIM : Theme.INK);
        minutes.setForeground(t.done ? Theme.INK_DIM : Theme.INK);
        row.add(left, BorderLayout.WEST);
        row.add(label, BorderLayout.CENTER);
        row.add(minutes, BorderLayout.EAST);
        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    actions.menu(t).show(e.getComponent(), e.getX(), e.getY());
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                mousePressed(e);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    actions.edit(t);
                }
            }
        };
        label.addMouseListener(mouse);
        row.addMouseListener(mouse);
        return row;
    }
}
