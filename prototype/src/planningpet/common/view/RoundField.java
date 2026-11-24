package planningpet.common.view;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JTextField;

/**
 * 알약 모양 입력칸(홈 빠른 추가, D-59). 흰 바탕에 CARD_EDGE 가장자리, 왼쪽에 선 아이콘, 비었을 때 흐린 안내 글.
 * 마우스를 올리면 가장자리가 MINT_LINE, 글을 쓰는 중(초점)이면 MINT 2px다.
 */
public final class RoundField extends JTextField {
    private final Icons.Kind kind;
    private final String placeholder;
    private boolean over;

    public RoundField(Icons.Kind kind, String placeholder) {
        this.kind = kind;
        this.placeholder = placeholder;
        setOpaque(false);
        setFont(Theme.font(14));
        setBorder(BorderFactory.createEmptyBorder(8, 40, 8, 16));
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                over = true;
                repaint();
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                over = false;
                repaint();
            }
        });
        addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                repaint();
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                repaint();
            }
        });
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
        boolean focus = isFocusOwner();
        Card.shadow(g, 0, 0, w, h, h);
        g.setColor(isEnabled() ? Theme.CARD_TOP : new java.awt.Color(0xEDF3F5));
        g.fillRoundRect(0, 0, w, h, h, h);
        g.setColor(focus ? Theme.MINT : over ? Theme.MINT_LINE : Theme.CARD_LINE);
        g.setStroke(new java.awt.BasicStroke(focus ? 2f : 1f));
        g.drawRoundRect(focus ? 1 : 0, focus ? 1 : 0, w - (focus ? 2 : 0), h - (focus ? 2 : 0), h, h);
        int s = 18;
        Icons.draw(g, kind, 15, (h - s) / 2, s, focus ? Theme.ICON : Theme.INK_SUB);
        if (getText().isEmpty() && placeholder != null) {
            g.setColor(Theme.INK_DIM);
            g.setFont(getFont());
            java.awt.FontMetrics fm = g.getFontMetrics();
            g.drawString(placeholder, getInsets().left, (h + fm.getAscent() - fm.getDescent()) / 2);
        }
        g.dispose();
        super.paintComponent(graphics);
    }
}
