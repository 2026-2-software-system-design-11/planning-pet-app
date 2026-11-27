package planningpet.common.view;

import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/** 디자인 시스템 5절의 카드. 화면의 큰 영역을 담는다. */
public class Card extends JPanel {
    private final int radius;
    private java.awt.Color fill;

    public Card(LayoutManager layout) {
        this(layout, Theme.CARD_RADIUS);
        setBorder(new EmptyBorder(14, 16, 18, 16));
    }

    public Card(LayoutManager layout, int radius) {
        super(layout);
        this.radius = radius;
        setOpaque(false);
    }

    /** 그라데이션 대신 한 색으로 칠한다(내비게이션 바의 SIDE). */
    public Card filled(java.awt.Color color) {
        fill = color;
        return this;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();
        shadow(g, 2, 1, w - 4, h - 8, radius);
        g.setPaint(fill != null ? fill : new GradientPaint(0, 0, Theme.CARD_TOP, 0, h, Theme.CARD_BOTTOM));
        g.fillRoundRect(2, 1, w - 4, h - 8, radius, radius);
        g.setColor(fill != null ? new java.awt.Color(255, 255, 255, 170) : Theme.CARD_EDGE);
        g.drawRoundRect(2, 1, w - 5, h - 9, radius, radius);
        g.dispose();
    }

    /** 아래로 옅게 퍼지는 그림자(디자인 시스템 4절). 카드와 버튼이 같이 쓴다. */
    public static void shadow(Graphics2D g, int x, int y, int w, int h, int radius) {
        for (int i = 3; i >= 1; i--) {
            g.setColor(Theme.CARD_SHADOW);
            g.fillRoundRect(x - i + 1, y + i, w + 2 * i - 2, h + i, radius + 2 * i, radius + 2 * i);
        }
    }
}
