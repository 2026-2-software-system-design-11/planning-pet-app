package planningpet.stats.view;

import planningpet.common.view.Theme;
import planningpet.stats.model.Stats;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import javax.swing.JComponent;

/** 계획 달성률 도넛. 위에서 시계 방향으로 끝낸 몫을 채우고 가운데에 백분율과 "(끝낸 수 / 셀 일정 수)"를 쓴다. */
final class Donut extends JComponent {
    private Stats.Rate rate = new Stats.Rate(0, 0);

    Donut() {
        setPreferredSize(new Dimension(180, 180));
        setMinimumSize(new Dimension(120, 120));
    }

    void set(Stats.Rate rate) {
        this.rate = rate;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        int d = Math.min(getWidth(), getHeight()) - 8;
        double ring = Math.max(14, d * 0.14);
        double x = (getWidth() - d) / 2.0 + ring / 2;
        double y = (getHeight() - d) / 2.0 + ring / 2;
        double r = d - ring;
        g.setStroke(new BasicStroke((float) ring));
        g.setColor(Theme.TRACK);
        g.draw(new Ellipse2D.Double(x, y, r, r));
        double share = rate.total() == 0 ? 0 : (double) rate.done() / rate.total();
        if (share > 0) {
            g.setPaint(new GradientPaint((float) (x + r), (float) y, Theme.MINT_TOP,
                    (float) x, (float) (y + r), Theme.MINT_DEEP));
            if (share >= 1) {
                g.draw(new Ellipse2D.Double(x, y, r, r));
            } else {
                g.setStroke(new BasicStroke((float) ring, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.draw(new Arc2D.Double(x, y, r, r, 90, -360 * share, Arc2D.OPEN));
            }
        }
        // 가운데 흰 원판과 옅은 그림자(예시의 도톰한 안쪽).
        double inner = r - ring * 1.1;
        double ix = x + (r - inner) / 2;
        double iy = y + (r - inner) / 2;
        for (int i = 3; i >= 1; i--) {
            g.setColor(Theme.CARD_SHADOW);
            g.fill(new Ellipse2D.Double(ix - i + 1, iy + i, inner + 2 * i - 2, inner + i));
        }
        g.setColor(Color.WHITE);
        g.fill(new Ellipse2D.Double(ix, iy, inner, inner));

        int cx = getWidth() / 2;
        int cy = getHeight() / 2;
        int big = (int) Math.max(22, inner * 0.3);
        g.setFont(Theme.heavy(big));
        FontMetrics fm = g.getFontMetrics();
        String pct = rate.percent() + "%";
        g.setColor(Theme.INK);
        g.drawString(pct, cx - fm.stringWidth(pct) / 2, cy + big / 6);
        g.setFont(Theme.font(Math.max(13, big * 2 / 5)));
        fm = g.getFontMetrics();
        String count = "(" + rate.done() + " / " + rate.total() + ")";
        g.setColor(Theme.INK_SUB);
        g.drawString(count, cx - fm.stringWidth(count) / 2, cy + big / 6 + fm.getHeight() + 2);
        g.dispose();
    }
}
