package planningpet.proto;

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
        setBorder(new EmptyBorder(18, 18, 20, 18));
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
        g.setColor(Theme.CARD_SHADOW);
        g.fillRoundRect(1, 4, w - 2, h - 5, radius + 2, radius + 2);
        g.setPaint(fill != null ? fill : new GradientPaint(0, 0, Theme.CARD_TOP, 0, h, Theme.CARD_BOTTOM));
        g.fillRoundRect(1, 1, w - 3, h - 7, radius, radius);
        g.setColor(Theme.CARD_LINE);
        g.drawRoundRect(1, 1, w - 3, h - 7, radius, radius);
        g.dispose();
    }
}
