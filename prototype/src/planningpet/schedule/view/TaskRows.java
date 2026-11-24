package planningpet.schedule.view;

import planningpet.common.view.*;
import planningpet.schedule.controller.TaskController;
import planningpet.schedule.model.*;
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
public final class TaskRows extends JPanel implements javax.swing.Scrollable {
    private final TaskController control;

    public TaskRows(TaskController control) {
        this.control = control;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    /** 메인(D-31): 시각 있는 일정을 시각과 함께, 그 아래 "시각 없는 할 일"(D-41). */
    public void showPlan(TaskBook.DayPlan plan) {
        removeAll();
        if (plan.timed().isEmpty() && plan.untimed().isEmpty()) {
            JLabel none = Ui.hint("아직 일정이 없어요. 작은 계획을 추가해 보세요.");
            none.setFont(Theme.font(13));
            none.setForeground(Theme.INK_SUB);
            none.setBorder(BorderFactory.createEmptyBorder(16, 6, 0, 0));
            add(none);
        }
        plan.timed().forEach(t -> add(row(t, TaskBook.timeText(t), false)));
        if (!plan.untimed().isEmpty()) {
            JLabel head = Ui.hint("시각 없는 할 일");
            head.setFont(Theme.bold(12));
            head.setForeground(Theme.INK_SUB);
            head.setBorder(BorderFactory.createEmptyBorder(14, 6, 4, 0));
            add(head);
            plan.untimed().forEach(t -> add(row(t, "", false)));
        }
        revalidate();
        repaint();
    }

    /** 일정관리 오른쪽 목록: 날짜를 붙인다. */
    public void show(List<Task> tasks, boolean withDate) {
        removeAll();
        if (tasks.isEmpty()) {
            add(Ui.hint("이번 주에 시각 없는 할 일이 없어요."));
        }
        tasks.forEach(t -> add(row(t, null, withDate)));
        revalidate();
        repaint();
    }

    /**
     * time이 null이면 시각 알약이 없고(일정관리 목록), ""이면 "시각 정하기" 알약이다. 끝낸 할 일은 제자리에서 회색 취소선이 된다.
     * 줄 순서는 둥근 체크, (고정 표시), 제목, 시각 알약, 걸리는 시간, 연필, 휴지통이다(D-59).
     * 모양은 2026-11-24 홈 예시의 할 일 줄: 줄 사이 가는 선, 끝낸 줄은 민트빛 바탕, 마우스를 올린 줄은 DAY 바탕.
     */
    private JPanel row(Task t, String time, boolean withDate) {
        boolean[] over = {false};
        JPanel row = new JPanel(new BorderLayout(6, 0)) {
            @Override
            protected void paintComponent(java.awt.Graphics graphics) {
                java.awt.Graphics2D g = (java.awt.Graphics2D) graphics.create();
                g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                if (t.done || over[0]) {
                    g.setColor(over[0] ? Theme.DAY : Theme.ROW_DONE);
                    g.fillRoundRect(0, 1, getWidth(), getHeight() - 3, 12, 12);
                }
                g.setColor(Theme.CARD_EDGE);
                g.fillRect(6, getHeight() - 1, getWidth() - 12, 1);
                g.dispose();
            }
        };
        row.setOpaque(false);
        row.setBorder(withDate ? BorderFactory.createEmptyBorder(3, 6, 3, 4) : BorderFactory.createEmptyBorder(5, 6, 6, 4));
        row.setAlignmentX(LEFT_ALIGNMENT);
        // D-35: 고정 일정도 체크한다. 체크박스 옆에 "고정" 표시를 둔다.
        JCheckBox box = RoundCheck.style(new JCheckBox());
        box.setSelected(t.done);
        box.putClientProperty(Task.class, t);
        box.getAccessibleContext().setAccessibleName((t.done ? "끝냄 취소: " : "끝냄: ") + t.title);
        box.addActionListener(e -> control.check(t));
        JPanel left = t.kind == Task.Kind.FIXED ? middle(box, fixedTag()) : middle(box);
        String date = withDate ? t.date.getMonthValue() + "/" + t.date.getDayOfMonth() + " " : "";
        String memoMark = t.memo.isBlank() ? "" : " <font size='2' color='#8A96A0'>메모</font>";
        JLabel label = title(t, date, "<html>" + date + (t.done ? "<s>" + Ui.esc(t.title) + "</s>" : Ui.esc(t.title))
                + memoMark + "</html>");
        label.setToolTipText(memoTip(t));
        row.setToolTipText(memoTip(t));
        JLabel minutes = new JLabel(t.minutes + "분", JLabel.RIGHT);
        minutes.setFont(Theme.font(13));
        minutes.setForeground(t.done ? Theme.INK_DIM : Theme.INK_SUB);
        minutes.setPreferredSize(new Dimension(Math.max(34, minutes.getPreferredSize().width), 20));
        row.add(left, BorderLayout.WEST);
        row.add(label, BorderLayout.CENTER);
        int iconButton = withDate ? 26 : 28;
        CircleButton edit = new CircleButton(Icons.Kind.PENCIL, iconButton, "수정", true);
        edit.addActionListener(e -> control.edit(t));
        CircleButton delete = new CircleButton(Icons.Kind.TRASH, iconButton, "삭제", true);
        delete.addActionListener(e -> control.delete(t));
        java.util.List<JComponent> watched = new java.util.ArrayList<>(java.util.List.of(row, label, box, edit, delete));
        if (withDate) {
            // 일정관리 오른쪽 목록은 좁아서 버튼을 둘째 줄 오른쪽에 둔다.
            row.add(middle(minutes), BorderLayout.EAST);
            JPanel buttons = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 2, 0));
            buttons.add(edit);
            buttons.add(delete);
            // D-44: 오늘보다 앞선 날짜는 둘째 줄 왼쪽에 "지난 날". 제목 줄에 붙이면 좁은 목록에서 제목이 잘린다.
            JPanel second = new JPanel(new BorderLayout());
            if (t.date.isBefore(control.today())) {
                JLabel past = new JLabel("지난 날");
                past.setForeground(new java.awt.Color(0xC0392B));
                past.setFont(Theme.font(12));
                past.setBorder(BorderFactory.createEmptyBorder(0, RoundCheck.SIZE + 10, 0, 0));
                second.add(past, BorderLayout.WEST);
            }
            second.add(buttons, BorderLayout.EAST);
            row.add(second, BorderLayout.SOUTH);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));
        } else {
            JComponent[] right;
            if (time != null) {
                TimePill when = new TimePill(time, t.done);
                when.addActionListener(e -> control.editTime(t, when));
                watched.add(when);
                right = new JComponent[] {when, minutes, edit, delete};
            } else {
                right = new JComponent[] {minutes, edit, delete};
            }
            row.add(middle(right), BorderLayout.EAST);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        }
        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    TaskMenu.of(control, t).show(e.getComponent(), e.getX(), e.getY());
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                mousePressed(e);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    control.edit(t);
                }
            }
        };
        label.addMouseListener(mouse);
        row.addMouseListener(mouse);
        // 줄 위에 마우스가 있는 동안(줄 안 버튼 위 포함) 줄 바탕을 칠한다.
        MouseAdapter hover = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                over[0] = true;
                row.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                over[0] = row.isShowing() && row.getMousePosition(true) != null;
                row.repaint();
            }
        };
        watched.forEach(c -> c.addMouseListener(hover));
        return row;
    }

    }

    /** 메모가 있으면 마우스를 올렸을 때 200자까지 보인다(D-33). */
    static String memoTip(Task t) {
        if (t.memo.isBlank()) {
            return null;
        }
        String m = t.memo.length() > 200 ? t.memo.substring(0, 200) + "…" : t.memo;
        return "<html><div style='width:280px'>" + Ui.esc(m).replace("\n", "<br>") + "</div></html>";
    }

    /** 목록 너비를 보이는 칸에 맞춰서 긴 제목이 버튼을 밀어내지 않게 한다. 제목은 남는 폭에서 잘린다. */
    @Override
    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(java.awt.Rectangle r, int orientation, int direction) {
        return 16;
    }

    @Override
    public int getScrollableBlockIncrement(java.awt.Rectangle r, int orientation, int direction) {
        return 64;
    }
}
