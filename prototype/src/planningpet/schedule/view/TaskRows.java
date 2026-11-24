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
        row.setAlignmentX(LEFT_ALIGNMENT);
        // D-35: 고정 일정도 체크한다. 체크박스 옆에 "고정" 표시를 둔다.
        JCheckBox box = new JCheckBox();
        box.setSelected(t.done);
        box.putClientProperty(Task.class, t);
        box.addActionListener(e -> control.check(t));
        JComponent mark = box;
        if (t.kind == Task.Kind.FIXED) {
            JLabel tag = new JLabel("고정");
            tag.setFont(Theme.font(11));
            tag.setOpaque(true);
            tag.setBackground(Theme.TRACK);
            tag.setBorder(BorderFactory.createLineBorder(Theme.CARD_LINE));
            mark = Ui.flow(box, tag);
        }
        JPanel left = Ui.flow();
        if (time != null) {
            // D-40: 시각 칸은 밑줄을 그어 누르는 곳임을 보인다.
            // HTML 글은 setForeground를 따르지 않아 흐린 색(메모 표시와 같은 색)을 HTML 안에 넣는다.
            String color = t.done || time.isEmpty() ? "<font color='#8A96A0'>" : "<font>";
            JLabel when = new JLabel("<html>" + color + "<u>" + (time.isEmpty() ? "시각 정하기" : time) + "</u></font></html>");
            when.setPreferredSize(new Dimension(92, 20));
            when.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
            when.setToolTipText("눌러서 시각을 바꿔요");
            when.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    control.editTime(t, when);
                }
            });
            left.add(when);
        }
        left.add(mark);
        String date = withDate ? t.date.getMonthValue() + "/" + t.date.getDayOfMonth() + " " : "";
        String memoMark = t.memo.isBlank() ? "" : " <font size='2' color='#8A96A0'>메모</font>";
        JLabel label = new JLabel("<html>" + date + (t.done ? "<s>" + Ui.esc(t.title) + "</s>" : Ui.esc(t.title))
                + memoMark + "</html>");
        label.setToolTipText(memoTip(t));
        row.setToolTipText(memoTip(t));
        label.setMinimumSize(new Dimension(0, 0));
        label.setFont(Theme.font(15));
        JLabel minutes = new JLabel(t.minutes + "분");
        label.setForeground(t.done ? Theme.INK_DIM : Theme.INK);
        minutes.setForeground(t.done ? Theme.INK_DIM : Theme.INK);
        row.add(left, BorderLayout.WEST);
        row.add(label, BorderLayout.CENTER);
        JButton edit = small("수정");
        edit.addActionListener(e -> control.edit(t));
        JButton delete = small("삭제");
        delete.addActionListener(e -> control.delete(t));
        if (withDate) {
            // 일정관리 오른쪽 목록은 좁아서 버튼을 둘째 줄 오른쪽에 둔다.
            row.add(minutes, BorderLayout.EAST);
            JPanel buttons = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 4, 0));
            buttons.add(edit);
            buttons.add(delete);
            // D-44: 오늘보다 앞선 날짜는 둘째 줄 왼쪽에 "지난 날". 제목 줄에 붙이면 좁은 목록에서 제목이 잘린다.
            JPanel second = new JPanel(new BorderLayout());
            if (t.date.isBefore(control.today())) {
                JLabel past = new JLabel("지난 날");
                past.setForeground(new java.awt.Color(0xC0392B));
                past.setFont(Theme.font(12));
                second.add(past, BorderLayout.WEST);
            }
            second.add(buttons, BorderLayout.EAST);
            row.add(second, BorderLayout.SOUTH);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));
        } else {
            row.add(Ui.flow(minutes, edit, delete), BorderLayout.EAST);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
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
        return row;
    }

    private static JButton small(String text) {
        JButton b = new JButton(text);
        b.setFont(Theme.bold(12));
        b.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
        return b;
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
