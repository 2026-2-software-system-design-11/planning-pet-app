package planningpet.stats.view;

import planningpet.app.view.App;
import planningpet.common.view.Card;
import planningpet.common.view.Icons;
import planningpet.common.view.Images;
import planningpet.common.view.RoundButtonUI;
import planningpet.common.view.Theme;
import planningpet.stats.model.Stats;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.time.LocalDate;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

/** 요약 상자: 위 띠(이번 주나 이번 달), 왼쪽 달성률, 오른쪽 얻은 포인트. */
final class SummaryBox extends JComponent {
    private final boolean monthly;
    private String name = "";
    private String range = "";
    private Stats.Rate rate = new Stats.Rate(0, 0);
    private int points;

    SummaryBox(boolean monthly) {
        this.monthly = monthly;
    }

    void set(String name, String range, Stats.Rate rate, int points) {
        this.name = name;
        this.range = range;
        this.rate = rate;
        this.points = points;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        StatsArt.smooth(g);
        int w = getWidth() - 4;
        int h = getHeight() - 6;
        int r = Theme.CARD_RADIUS;
        Card.shadow(g, 2, 1, w, h, r);
        g.setColor(Color.WHITE);
        g.fillRoundRect(2, 1, w, h, r, r);
        g.setColor(Theme.CARD_EDGE);
        g.drawRoundRect(2, 1, w - 1, h - 1, r, r);

        // 위 띠
        int bandH = Math.min(42, h / 3);
        g.setColor(monthly ? StatsScreen.MONTH_BAND : Theme.MINT_PALE);
        g.fill(new RoundRectangle2D.Double(9, 8, w - 14, bandH, 14, 14));
        g.setFont(Theme.bold(15));
        FontMetrics fm = g.getFontMetrics();
        g.setFont(Theme.font(14));
        FontMetrics fr = g.getFontMetrics();
        int textW = 22 + 8 + fm.stringWidth(name + " ") + fr.stringWidth(range);
        int x = 2 + (w - textW) / 2;
        int base = 8 + (bandH + fm.getAscent() - fm.getDescent()) / 2;
        Icons.draw(g, Icons.Kind.CALENDAR, x, 8 + (bandH - 22) / 2, 22, monthly ? Theme.SATURDAY : Theme.ICON);
        x += 30;
        g.setFont(Theme.bold(15));
        g.setColor(Theme.INK);
        g.drawString(name + " ", x, base);
        x += fm.stringWidth(name + " ");
        g.setFont(Theme.font(14));
        g.setColor(monthly ? Theme.INK : Theme.TASK_TEXT);
        g.drawString(range, x, base);
