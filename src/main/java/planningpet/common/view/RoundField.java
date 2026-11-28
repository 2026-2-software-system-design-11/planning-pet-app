package planningpet.common.view;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JTextField;

/**
 * 알약 모양 입력칸(디자인 시스템 9절, 07). 흰 바탕에 CARD_LINE 가장자리, 왼쪽에 선 아이콘, 비었을 때 흐린 예시 글.
 * 홈 빠른 추가와 반려동물 이름 칸이 쓴다.
 */
public final class RoundField extends JTextField {
    private final Icons.Kind kind;
    private final String placeholder;

    public RoundField(Icons.Kind kind, String placeholder) {
        this.kind = kind;
        this.placeholder = placeholder;
        setOpaque(false);
        setFont(Theme.font(14));
        setBorder(BorderFactory.createEmptyBorder(8, kind == null ? 18 : 40, 8, 16));
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(d.width, Math.max(d.height, 40));
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int w = getWidth() - 1;
        int h = getHeight() - 3;
        Card.shadow(g, 0, 0, w, h, h);
        g.setColor(Theme.CARD_TOP);
        g.fillRoundRect(0, 0, w, h, h, h);
        g.setColor(Theme.CARD_LINE);
        g.drawRoundRect(0, 0, w, h, h, h);
        int s = 18;
        if (kind != null) {
            Icons.draw(g, kind, 15, (h - s) / 2, s, Theme.INK_SUB);
        }
        if (getText().isEmpty() && placeholder != null) {
            g.setColor(Theme.INK_DIM);
            g.setFont(getFont());
            FontMetrics fm = g.getFontMetrics();
            g.drawString(placeholder, getInsets().left, (h + fm.getAscent() - fm.getDescent()) / 2);
        }
        g.dispose();
        super.paintComponent(graphics);
    }
}
