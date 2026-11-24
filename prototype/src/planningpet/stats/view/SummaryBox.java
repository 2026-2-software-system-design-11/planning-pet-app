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

        // 아래: 달성률 | 포인트
        int top = 8 + bandH;
        int bodyH = h - top;
        int half = w / 2;
        int cy = top + bodyH / 2;
        g.setColor(Theme.CARD_EDGE);
        g.setStroke(new BasicStroke(1.4f));
        g.drawLine(2 + half, cy - bodyH / 4, 2 + half, cy + bodyH / 4);
        if (rate.total() == 0) {
            g.setFont(Theme.font(12));
            fm = g.getFontMetrics();
            g.setColor(Theme.INK_SUB);
            // 칸이 좁으면 두 줄로 쓴다.
            String[] lines = fm.stringWidth(StatsArt.NONE) <= half - 16 ? new String[] {StatsArt.NONE} : new String[] {"아직 셀 일정이", "없어요"};
            int ly = cy - (lines.length - 1) * fm.getHeight() / 2 + fm.getAscent() / 2;
            for (String line : lines) {
                g.drawString(line, 2 + (half - fm.stringWidth(line)) / 2, ly);
                ly += fm.getHeight();
            }
        } else {
            g.setFont(Theme.heavy(30));
            fm = g.getFontMetrics();
            String pct = rate.percent() + "%";
            g.setColor(Theme.INK);
            g.drawString(pct, 2 + (half - fm.stringWidth(pct)) / 2, cy + 4);
            g.setFont(Theme.font(14));
            FontMetrics fs = g.getFontMetrics();
            String count = "(" + rate.done() + " / " + rate.total() + ")";
            g.setColor(Theme.INK_SUB);
            g.drawString(count, 2 + (half - fs.stringWidth(count)) / 2, cy + 4 + fs.getHeight() + 2);
        }
        int coin = 30;
        int rx = 2 + half;
        Icons.draw(g, Icons.Kind.COIN, rx + (half - coin) / 2, cy - coin - 2, coin, Theme.COIN);
        g.setFont(Theme.heavy(22));
        fm = g.getFontMetrics();
        String p = String.format("+%,dP", points);
        g.setColor(Theme.INK);
        g.drawString(p, rx + (half - fm.stringWidth(p)) / 2, cy + fm.getAscent() - 2);
        g.dispose();
    }
}
