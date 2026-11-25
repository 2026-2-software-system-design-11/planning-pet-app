package planningpet.schedule.view;

import planningpet.common.view.Icons;
import planningpet.common.view.Theme;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JButton;

/**
 * 할 일 줄의 시각 알약 버튼(05 메인 7번, D-59). 시계 아이콘과 "10:00~11:00", 시각이 없으면 민트 "시각 정하기".
 * 끝낸 일은 흐린 글과 옅은 테두리다. 폭은 줄마다 같아서 목록에서 세로로 줄이 맞는다.
 * 모양은 2026-11-24 홈 예시의 "예정" 알약(옅은 바탕, 가는 테두리, 시계 아이콘)을 디자인 시스템 색으로 옮긴 것이다.
 */
final class TimePill extends JButton {
    private final boolean empty;
    private final boolean done;

    TimePill(String time, boolean done) {
        super(time.isEmpty() ? "시각 정하기" : time);
        this.empty = time.isEmpty();
        this.done = done;
        setFont(Theme.bold(12));
        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setRolloverEnabled(true);
        setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        setToolTipText("눌러서 시각을 바꿔요");
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
        Color fill = down ? Theme.MINT_SOFT : over ? Theme.MINT_PALE : empty ? Theme.MINT_PALE : done ? Theme.CARD_TOP : Theme.DAY;
        Color line = over || down ? Theme.MINT : empty ? Theme.MINT_LINE : done ? Theme.CARD_EDGE : Theme.CARD_LINE;
        Color ink = done ? Theme.INK_DIM : empty ? Theme.ICON : Theme.INK;
        g.setColor(fill);
        g.fillRoundRect(0, 0, w, h, h, h);
        g.setColor(line);
        g.setStroke(new BasicStroke(1f));
        g.drawRoundRect(0, 0, w, h, h, h);
        if (isFocusOwner()) {
            g.setColor(new Color(0x1F, 0xB9, 0x9C, 110));
            g.drawRoundRect(2, 2, w - 4, h - 4, h - 4, h - 4);
        }
        FontMetrics fm = g.getFontMetrics(getFont());
        int icon = 14;
        int tw = fm.stringWidth(getText());
        int x = (w - (icon + 5 + tw)) / 2;
        Icons.draw(g, Icons.Kind.CLOCK, x, (h - icon) / 2, icon, done ? Theme.INK_DIM : Theme.ICON);
        g.setFont(getFont());
        g.setColor(ink);
        g.drawString(getText(), x + icon + 5, (h + fm.getAscent() - fm.getDescent()) / 2 + 1);
        g.dispose();
    }
}
