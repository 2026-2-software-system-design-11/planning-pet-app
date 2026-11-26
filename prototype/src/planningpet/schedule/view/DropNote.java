package planningpet.schedule.view;

import planningpet.common.view.*;
import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.Timer;

/** D-99: 놓은 자리 옆 작은 글 상자. 놓은 결과와 "되돌리기"는 5초, 놓지 못한 까닭은 3초 보이고, 마우스를 올린 동안은 머문다. */
public final class DropNote extends Card {
    public static final int PLACED_MILLIS = 5000;
    public static final int WHY_MILLIS = 3000;
    public final JLabel text = new JLabel();
    public final JButton undo = new JButton("되돌리기");
    public final Timer timer = new Timer(PLACED_MILLIS, e -> close());
    private Runnable undoing;

    DropNote() {
        super(new BorderLayout(8, 0), Theme.BUBBLE_RADIUS);
        setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 6));
        text.setFont(Theme.font(12));
        undo.putClientProperty(RoundButtonUI.NAV, Boolean.TRUE);
        undo.setFont(Theme.bold(12));
        undo.setForeground(Theme.TASK_TEXT);
        add(text, BorderLayout.CENTER);
        add(undo, BorderLayout.EAST);
        timer.setRepeats(false);
        undo.addActionListener(e -> {
            Runnable u = undoing;
            close();
            u.run();
        });
        MouseAdapter hover = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                timer.stop();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (isVisible() && getMousePosition(true) == null) {
                    timer.restart();
                }
            }
        };
        addMouseListener(hover);
        undo.addMouseListener(hover);
        setVisible(false);
    }

    /**
     * 글을 보인다. undo가 있으면 "되돌리기"와 함께 5초, 없으면 3초. x, y는 상자 왼쪽 위, right는 그 자리에 둘 수 없을 때 오른쪽 끝.
     * 놓인 블록 block이 있고 양쪽 다 자리가 없으면 블록 바로 아래(보이는 칸 안에 자리가 없으면 위)에 블록 왼쪽 끝을 맞춘다(S158).
     */
    void open(String message, Runnable undo, int x, int y, int right, java.awt.Rectangle block) {
        text.setText(message);
        undoing = undo;
        this.undo.setVisible(undo != null);
        java.awt.Dimension size = getPreferredSize();
        int maxX = getParent().getWidth() - size.width - 2;
        // 시간표의 보이는 아래 끝에서는 잘리지 않게 위로 올린다.
        java.awt.Rectangle seen = ((javax.swing.JComponent) getParent()).getVisibleRect();
        int maxY = (seen.isEmpty() ? getParent().getHeight() : seen.y + seen.height) - size.height - 2;
        x = x > maxX ? right - size.width : x;
        if (x < 0 && block != null) {
            x = Math.min(block.x, maxX);
            y = block.y + block.height + 2 <= maxY ? block.y + block.height + 2 : block.y - size.height - 2;
        }
        setBounds(Math.max(0, x), Math.max(0, Math.min(y, maxY)), size.width, size.height);
        timer.setInitialDelay(undo != null ? PLACED_MILLIS : WHY_MILLIS);
        timer.restart();
        setVisible(true);
        revalidate();
        repaint();
    }

    /** 상자를 닫는다. */
    public void close() {
        timer.stop();
        undoing = null;
        setVisible(false);
    }
}
