package planningpet.common.view;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JButton;

/** 동그란 아이콘 버튼(방 양옆 화살표, 펫 더하기, 상점 카드 넘기기, 닫기). 흰 바탕에 옅은 그림자다. */
public final class CircleButton extends JButton {
    private final Icons.Kind kind;
    private final boolean plain;

    /** plain이면 바탕 없이 아이콘만 그린다(상점 카드 줄의 화살표, 닫기). */
    public CircleButton(Icons.Kind kind, int size, String tip, boolean plain) {
        this.kind = kind;
        this.plain = plain;
        setPreferredSize(new Dimension(size, size));
        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setRolloverEnabled(true);
        setToolTipText(tip);
        getAccessibleContext().setAccessibleName(tip);
        setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int d = Math.min(getWidth(), getHeight()) - 4;
        int x = (getWidth() - d) / 2;
        int y = (getHeight() - d) / 2 - 1;
        if (!plain) {
            Card.shadow(g, x, y, d, d, d);
            g.setColor(getModel().isRollover() ? Theme.MINT_PALE : Theme.CARD_TOP);
            g.fillOval(x, y, d, d);
            g.setColor(Theme.CARD_EDGE);
            g.drawOval(x, y, d, d);
        }
        int s = d * 11 / 20;
        Icons.draw(g, kind, x + (d - s) / 2, y + (d - s) / 2, s, isEnabled() ? Theme.ICON : Theme.INK_DIM);
        g.dispose();
    }
}
