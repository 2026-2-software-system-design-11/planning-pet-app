package planningpet.common.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JToggleButton;

/**
 * 켜고 끄는 스위치(디자인 시스템 5절, D-82). 하나만 눌리는 탭과 모양이 달라야 해서 알약 바탕 없이
 * 아이콘, 글자, 오른쪽 트랙과 손잡이로 그린다. 켜면 트랙이 민트이고 손잡이가 오른쪽, 끄면 `TRACK` 회색이고 왼쪽이다.
 * 동작은 JToggleButton 그대로다(누르면 켜짐과 꺼짐이 바뀐다).
 */
public final class Switch extends JToggleButton {
    static final int TRACK_W = 36;
    static final int TRACK_H = 20;
    private final Icons.Kind kind;

    public Switch(String text, Icons.Kind kind, boolean on) {
        super(text, on);
        this.kind = kind;
        setFont(Theme.bold(13));
        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setRolloverEnabled(true);
        setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
    }

    @Override
    public Dimension getPreferredSize() {
        FontMetrics fm = getFontMetrics(getFont());
        return new Dimension(14 + 20 + 8 + fm.stringWidth(getText()) + 12 + TRACK_W + 12, 40);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();
        boolean on = isSelected();
        boolean over = getModel().isRollover() && isEnabled();
        if (over) {
            g.setColor(Theme.MINT_PALE);
            g.fillRoundRect(0, 2, w - 1, h - 5, h - 5, h - 5);
        }
        Color ink = isEnabled() ? Theme.INK : Theme.INK_DIM;
        Icons.draw(g, kind, 14, (h - 20) / 2, 20, isEnabled() ? Theme.ICON : Theme.INK_DIM);
        g.setFont(getFont());
        FontMetrics fm = g.getFontMetrics();
        g.setColor(ink);
        g.drawString(getText(), 14 + 20 + 8, (h + fm.getAscent() - fm.getDescent()) / 2);
        int tx = w - 12 - TRACK_W;
        int ty = (h - TRACK_H) / 2;
        if (on && isEnabled()) {
            g.setPaint(new java.awt.GradientPaint(0, ty, Theme.MINT_TOP, 0, ty + TRACK_H, Theme.MINT_DEEP));
        } else {
            g.setColor(Theme.TRACK);
        }
        g.fillRoundRect(tx, ty, TRACK_W, TRACK_H, TRACK_H, TRACK_H);
        g.setColor(on ? Theme.MINT_DEEP : Theme.CARD_LINE);
        g.drawRoundRect(tx, ty, TRACK_W, TRACK_H, TRACK_H, TRACK_H);
        int k = TRACK_H - 4;
        int kx = on ? tx + TRACK_W - 2 - k : tx + 2;
        Card.shadow(g, kx, ty + 2, k, k, k);
        g.setColor(Color.WHITE);
        g.fillOval(kx, ty + 2, k, k);
        if (isFocusOwner() && isEnabled()) {
            g.setColor(new Color(0x1F, 0xB9, 0x9C, 110));
            g.drawRoundRect(1, 3, w - 3, h - 7, h - 7, h - 7);
        }
        g.dispose();
    }
}
