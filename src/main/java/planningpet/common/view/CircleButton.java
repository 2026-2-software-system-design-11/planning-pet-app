package planningpet.common.view;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JButton;

/** 원 아이콘 버튼(디자인 시스템 5절). 흰 바탕에 옅은 그림자다. 아이콘만 있으므로 툴팁과 접근 이름을 단다(docs/porting/home.md S3c). */
public final class CircleButton extends JButton {
    private final Icons.Kind kind;
    private final boolean plain;

    /** size는 알맞은 크기다. 그릴 때는 칸의 짧은 변에서 잰다. plain이면 바탕 없이 아이콘만 그린다. */
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
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int d = Math.min(getWidth(), getHeight()) - 4;
        int x = (getWidth() - d) / 2;
        int y = (getHeight() - d) / 2 - 1;
        boolean over = getModel().isRollover() && isEnabled();
        boolean down = getModel().isPressed() && getModel().isArmed();
        if (!plain) {
            Card.shadow(g, x, y, d, d, d);
            g.setColor(down ? Theme.MINT_SOFT : over ? Theme.MINT_PALE : Theme.CARD_TOP);
            g.fillOval(x, y, d, d);
            g.setColor(over || down ? Theme.MINT_LINE : Theme.CARD_EDGE);
            g.drawOval(x, y, d, d);
        } else if (over || down) {
            // 바탕 없는 버튼도 마우스를 올리면 연민트 원이 떠서 누르는 곳임을 보인다.
            g.setColor(down ? Theme.MINT_SOFT : Theme.MINT_PALE);
            g.fillOval(x, y, d, d);
        }
        if (isFocusOwner() && isEnabled()) {
            g.setColor(new Color(0x1F, 0xB9, 0x9C, 110));
            g.drawOval(x + 1, y + 1, d - 2, d - 2);
        }
        int s = d * 11 / 20;
        Icons.draw(g, kind, x + (d - s) / 2, y + (d - s) / 2, s, isEnabled() ? Theme.ICON : Theme.INK_DIM);
        g.dispose();
    }
}
