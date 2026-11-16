package planningpet.proto;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** 할 일 줄 목록. 메인 목록과 일정관리의 시각 없는 할 일 목록이 같이 쓴다. */
public final class TaskRows extends JPanel {
    private final Actions actions;

    public TaskRows(Actions actions) {
        this.actions = actions;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    /** 끝낸 할 일도 제자리에 남고 회색 취소선이 된다(05 메인). */
    public void show(List<Task> tasks, boolean withDate) {
        removeAll();
        for (Task t : tasks) {
            JPanel row = new JPanel(new BorderLayout(6, 0));
            row.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
            row.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 30));
            JCheckBox box = new JCheckBox();
            box.setSelected(t.done);
            box.addActionListener(e -> actions.check(t));
            String when = withDate ? t.date.getMonthValue() + "/" + t.date.getDayOfMonth() + " " : "";
            String title = t.done ? "<s>" + t.title + "</s>" : t.title;
            JLabel label = new JLabel("<html>" + when + title + "</html>");
            JLabel minutes = new JLabel(t.minutes + "분");
            label.setFont(Theme.font(15));
            if (t.done) {
                label.setForeground(Theme.INK_DIM);
                minutes.setForeground(Theme.INK_DIM);
            }
            row.add(box, BorderLayout.WEST);
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
            add(row);
        }
        revalidate();
        repaint();
    }
}
