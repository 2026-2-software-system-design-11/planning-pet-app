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

/** 일정 유형별 달성률: 유형마다 아이콘, 이름, 색 막대, 백분율. 셀 일정이 있는 유형만(D-55). */
final class KindBars extends JComponent {
    private Map<String, Stats.Rate> rates = Map.of();

    void set(Map<String, Stats.Rate> rates) {
        this.rates = rates;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        StatsArt.smooth(g);
        if (rates.isEmpty()) {
            g.setFont(Theme.font(14));
            FontMetrics fm = g.getFontMetrics();
            g.setColor(Theme.INK_SUB);
            g.drawString(StatsArt.NONE, (getWidth() - fm.stringWidth(StatsArt.NONE)) / 2, getHeight() / 2 + fm.getAscent() / 2);
            g.dispose();
            return;
        }
        int n = rates.size();
        int rowH = Math.min(46, getHeight() / Math.max(n, 4));
        int y = Math.max(0, (getHeight() - rowH * n) / 2 - 4);
        int w = getWidth();
        // 예시처럼 줄 묶음 뒤에 옅은 판을 깐다.
        g.setColor(new Color(0xF7FBFC));
        g.fill(new RoundRectangle2D.Double(0, y - 4, w - 1, rowH * n + 8, 16, 16));
        g.setColor(new Color(0xE8F2F5));
        g.draw(new RoundRectangle2D.Double(0, y - 4, w - 1, rowH * n + 8, 16, 16));
        g.setFont(Theme.bold(15));
        int nameW = getFontMetrics(Theme.bold(15)).stringWidth("개인/취미");
        int barX = 12 + 26 + 14 + nameW + 18;
        g.setFont(Theme.heavy(17));
        int pctW = g.getFontMetrics().stringWidth("100%");
        int barW = Math.max(30, w - barX - pctW - 22);
        int i = 0;
        for (Map.Entry<String, Stats.Rate> e : rates.entrySet()) {
            StatsArt.Look look = StatsArt.look(e.getKey());
            int mid = y + rowH / 2;
            if (i > 0) {
                g.setColor(new Color(0xEAF3F6));
                g.drawLine(6, y, w - 6, y);
            }
            StatsArt.draw(g, look.icon(), 12, mid - 13, 26, look.ink());
            g.setFont(Theme.bold(15));
            FontMetrics fm = g.getFontMetrics();
            g.setColor(Theme.INK);
            g.drawString(e.getKey(), 12 + 26 + 14, mid + fm.getAscent() / 2 - 2);
            int bh = 14;
            g.setColor(Theme.TRACK);
            g.fill(new RoundRectangle2D.Double(barX, mid - bh / 2.0, barW, bh, bh, bh));
            double share = e.getValue().total() == 0 ? 0 : (double) e.getValue().done() / e.getValue().total();
            if (share > 0) {
                double fw = Math.max(bh, barW * share);
                g.setPaint(new GradientPaint(barX, 0, look.top(), barX + barW, 0, look.bottom()));
                g.fill(new RoundRectangle2D.Double(barX, mid - bh / 2.0, fw, bh, bh, bh));
            }
            g.setFont(Theme.heavy(17));
            fm = g.getFontMetrics();
            String pct = e.getValue().percent() + "%";
            g.setColor(Theme.INK);
            g.drawString(pct, w - 8 - fm.stringWidth(pct), mid + fm.getAscent() / 2 - 2);
            y += rowH;
            i++;
        }
        g.dispose();
    }
}
