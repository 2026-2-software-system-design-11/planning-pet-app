package planningpet.schedule.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JButton;
import planningpet.common.view.Icons;
import planningpet.common.view.Theme;

/**
 * 할 일 줄의 시각 알약 버튼(05 입력 항목 7, D-59). 시계 아이콘과 "10:00~11:00", 시각이 없으면 민트 "시각 정하기"다. 끝낸 일과
 * 잠근 알약은 흐린 글과 옅은 테두리다. 폭은 줄마다 같아서 목록에서 세로로 맞는다(docs/porting/task-rows.md S3s).
 */
final class TimePill extends JButton {
    private final boolean empty;
    private final boolean done;

    /** locked면 누를 수 없는 흐린 알약이고(D-94) 마우스를 올렸을 때 why를 보인다. 끝낸 줄과 지난 날 목록의 기간 할 일이 그렇다. */
    TimePill(String time, boolean done, boolean locked, String why) {
        super(time.isEmpty() ? "시각 정하기" : time);
        this.empty = time.isEmpty();
        this.done = done || locked;
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

    @Override
    public Dimension getPreferredSize() {
        FontMetrics fm = getFontMetrics(getFont());
        // 폭은 줄마다 같다. 자정을 넘는 "23:30~01:00(다음 날)"(D-77)만 글에 맞춰 넓어진다.
        int w = Math.max(Math.max(fm.stringWidth("00:00~00:00"), fm.stringWidth("시각 정하기")), fm.stringWidth(getText()))
                + 14 + 5 + 18;
        return new Dimension(w, 28);
    }

    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        boolean over = getModel().isRollover();
        boolean down = getModel().isPressed() && getModel().isArmed();
        int w = getWidth() - 1;
        int h = getHeight() - 1;
        Color fill = down ? Theme.MINT_SOFT : over ? Theme.MINT_PALE : empty ? Theme.MINT_PALE : Theme.DAY;
        Color line = over || down ? Theme.MINT : empty ? Theme.MINT_LINE : Theme.CARD_LINE;
        if (done) {
            // 흐린 알약은 바탕과 테두리도 끝낸 줄처럼 옅다(민트를 쓰지 않는다).
            fill = Theme.CARD_TOP;
            line = Theme.CARD_EDGE;
        }
        g.setColor(fill);
        g.fillRoundRect(0, 0, w, h, h, h);
        g.setColor(line);
        g.setStroke(new BasicStroke(1f));
        g.drawRoundRect(0, 0, w, h, h, h);
        FontMetrics fm = g.getFontMetrics(getFont());
        int icon = 14;
        int x = (w - (icon + 5 + fm.stringWidth(getText()))) / 2;
        Icons.draw(g, Icons.Kind.CLOCK, x, (h - icon) / 2, icon, done ? Theme.INK_DIM : Theme.ICON);
        g.setFont(getFont());
        g.setColor(done ? Theme.INK_DIM : empty ? Theme.ICON : Theme.INK);
        g.drawString(getText(), x + icon + 5, (h + fm.getAscent() - fm.getDescent()) / 2 + 1);
        g.dispose();
    }
}
