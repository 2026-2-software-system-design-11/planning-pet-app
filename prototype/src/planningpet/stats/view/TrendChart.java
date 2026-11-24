package planningpet.stats.view;

import planningpet.common.view.Card;
import planningpet.common.view.Theme;
import planningpet.stats.model.Stats;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;

/**
 * 날짜별 일정 완료 추이(D-55). 칸은 주간이면 하루, 월간이면 한 주다. 막대는 끝낸 수, 선은 완료율이다.
 * 오늘이 든 칸은 막대를 진하게, 아래 날짜를 알약으로 강조한다. 마우스를 올린 칸에 말풍선을 띄운다.
 * 오늘 뒤의 칸은 세지 않으므로(D-55) 막대와 점이 없고 날짜를 흐리게 쓴다.
 */
final class TrendChart extends JComponent {
    static final String WEEK = "월화수목금토일";
    /** 막대 채움. 예시의 옅은 민트와 오늘 칸의 진한 민트. */
    private static final Color BAR_TOP = new Color(0xCDF3E6);
    private static final Color BAR_BOTTOM = new Color(0xB4ECDA);
    private static final Color LINE = new Color(0x22BE8E);
    private static final int LEFT = 46;
    private static final int RIGHT = 10;
    private static final int TOP = 44;
    private static final int BOTTOM = 44;

    private List<Stats.Bucket> buckets = List.of();
    private LocalDate today = LocalDate.now();
    private boolean monthly;
    private int hover = -1;

    TrendChart() {
        setPreferredSize(new Dimension(320, 200));
        setMinimumSize(new Dimension(200, 150));
        MouseAdapter m = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                setHover(columnAt(e.getPoint()));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setHover(-1);
            }
        };
        addMouseListener(m);
        addMouseMotionListener(m);
    }

    void set(List<Stats.Bucket> buckets, LocalDate today, boolean monthly) {
        this.buckets = buckets;
        this.today = today;
        this.monthly = monthly;
        hover = -1;
        repaint();
    }

    /** 마우스를 올린 칸. 없으면 -1. */
    int hover() {
        return hover;
    }

    void setHover(int i) {
        if (i != hover) {
            hover = i;
            repaint();
        }
    }

    boolean empty() {
        return buckets.stream().allMatch(b -> b.rate().total() == 0);
    }

    private int columnAt(Point p) {
        if (buckets.isEmpty() || p.x < LEFT || p.x >= getWidth() - RIGHT) {
            return -1;
        }
        double col = (double) (getWidth() - LEFT - RIGHT) / buckets.size();
        int i = (int) ((p.x - LEFT) / col);
        return i >= 0 && i < buckets.size() && counted(buckets.get(i)) ? i : -1;
    }

    private boolean counted(Stats.Bucket b) {
        return !b.from().isAfter(today);
    }

    private boolean holdsToday(Stats.Bucket b) {
        return !today.isBefore(b.from()) && !today.isAfter(b.to());
    }

    /** 말풍선의 첫 줄. 주간은 "11/20 (목)", 월간은 "11/16 ~ 11/22". */
    String label(Stats.Bucket b) {
        if (!monthly) {
            return md(b.from()) + " (" + WEEK.charAt(b.from().getDayOfWeek().getValue() - 1) + ")";
        }
        return b.from().equals(b.to()) ? md(b.from()) : md(b.from()) + " ~ " + md(b.to());
    }

    /** 말풍선 글. QA와 접근성 이름이 같이 쓴다. */
    List<String> tip(int i) {
        Stats.Bucket b = buckets.get(i);
        Stats.Rate r = b.rate();
        if (r.total() == 0) {
            return List.of(label(b), "셀 일정이 없어요");
        }
        return List.of(label(b), "완료 " + r.done() + "개 / 전체 " + r.total() + "개", "완료율 " + r.percent() + "%");
    }

    private static String md(LocalDate d) {
        return d.getMonthValue() + "/" + d.getDayOfMonth();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        int w = getWidth();
        int h = getHeight();
        int x0 = LEFT;
        int x1 = w - RIGHT;
        int y0 = TOP;
        int y1 = h - BOTTOM;
        int n = Math.max(1, buckets.size());
        double col = (double) (x1 - x0) / n;

        legend(g, x1);

        // 왼쪽 백분율 눈금과 아래 바닥선.
        g.setFont(Theme.font(11));
        FontMetrics fm = g.getFontMetrics();
        for (int p = 0; p <= 100; p += 20) {
            int y = yOf(p, y0, y1);
            String s = p + "%";
            g.setColor(Theme.INK_SUB);
            g.drawString(s, x0 - 10 - fm.stringWidth(s), y + fm.getAscent() / 2 - 1);
            if (p > 0) {
                g.setColor(new Color(0xEEF5F8));
                g.drawLine(x0 + 1, y, x1, y);
            }
        }
        g.setColor(Theme.TRACK);
        g.setStroke(new BasicStroke(1.4f));
        g.drawLine(x0, y0 - 6, x0, y1);
        g.drawLine(x0, y1, x1, y1);

        // 아래 날짜. 오늘 칸은 알약을 깐다.
        for (int i = 0; i < buckets.size(); i++) {
            Stats.Bucket b = buckets.get(i);
            int cx = (int) (x0 + col * (i + 0.5));
            if (holdsToday(b)) {
                int pw = (int) Math.min(col - 6, 58);
                g.setColor(Theme.NAV_PILL);
                g.fill(new RoundRectangle2D.Double(cx - pw / 2.0, y1 + 4, pw, BOTTOM - 6, 14, 14));
            }
            String top;
            String bottom;
            Color bottomInk = Theme.INK;
            if (monthly) {
                top = md(b.from());
                bottom = b.from().equals(b.to()) ? "" : "~ " + md(b.to());
            } else {
                top = md(b.from());
                int dow = b.from().getDayOfWeek().getValue();
                bottom = "(" + WEEK.charAt(dow - 1) + ")";
                bottomInk = dow == 6 ? Theme.SATURDAY : dow == 7 ? Theme.SUNDAY : Theme.INK;
            }
            boolean later = !counted(b);
            g.setFont(Theme.font(12));
            fm = g.getFontMetrics();
            g.setColor(later ? Theme.INK_DIM : Theme.INK);
            g.drawString(top, cx - fm.stringWidth(top) / 2, y1 + 4 + fm.getAscent() + 2);
            g.setColor(later ? fade(bottomInk) : bottomInk);
            g.drawString(bottom, cx - fm.stringWidth(bottom) / 2, y1 + 4 + fm.getAscent() * 2 + 5);
        }

        if (empty()) {
            g.setFont(Theme.font(14));
            fm = g.getFontMetrics();
            String s = "아직 셀 일정이 없어요";
            g.setColor(Theme.INK_SUB);
            g.drawString(s, (x0 + x1 - fm.stringWidth(s)) / 2, (y0 + y1) / 2 + fm.getAscent() / 2);
            g.dispose();
            return;
        }

        // 막대: 끝낸 수. 가장 큰 막대가 높이의 3분의 2쯤 오게 한다(완료율 선과 겹치지 않게).
        int most = buckets.stream().mapToInt(b -> b.rate().done()).max().orElse(0);
        double unit = (y1 - y0) * 0.66 / Math.max(1, most);
        double bw = Math.min(col * 0.52, 44);
        for (int i = 0; i < buckets.size(); i++) {
            Stats.Bucket b = buckets.get(i);
            if (!counted(b) || b.rate().total() == 0) {
                continue;
            }
            int cx = (int) (x0 + col * (i + 0.5));
            int done = b.rate().done();
            double bh = done * unit;
            boolean mark = holdsToday(b);
            if (bh > 0) {
                Path2D bar = topRounded(cx - bw / 2, y1 - bh, bw, bh, Math.min(8, bh));
                g.setPaint(mark ? new GradientPaint(0, (float) (y1 - bh), Theme.MINT_TOP, 0, y1, Theme.MINT_DEEP)
                        : new GradientPaint(0, (float) (y1 - bh), BAR_TOP, 0, y1, BAR_BOTTOM));
                g.fill(bar);
            }
            if (done == 0) {
                continue;
            }
            g.setFont(Theme.bold(14));
            fm = g.getFontMetrics();
            String s = String.valueOf(done);
            boolean inside = bh >= fm.getAscent() + 10;
            g.setColor(inside && mark ? Color.WHITE : Theme.INK);
            int ty = inside ? (int) (countMiddle(bh, y1, yOf(b.rate().percent(), y0, y1)) + fm.getAscent() / 2.0 - 1)
                    : (int) (y1 - bh - 5);
            g.drawString(s, cx - fm.stringWidth(s) / 2, ty);
        }

        // 선: 완료율. 셀 일정이 있는 칸만 잇는다.
        List<double[]> pts = new ArrayList<>();
        List<Integer> idx = new ArrayList<>();
        for (int i = 0; i < buckets.size(); i++) {
            Stats.Bucket b = buckets.get(i);
            if (counted(b) && b.rate().total() > 0) {
                pts.add(new double[] {x0 + col * (i + 0.5), yOf(b.rate().percent(), y0, y1)});
                idx.add(i);
            }
        }
        if (hover >= 0 && idx.contains(hover)) {
            double[] p = pts.get(idx.indexOf(hover));
            g.setColor(Theme.MINT_LINE);
            g.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1f, new float[] {3, 3}, 0));
            g.drawLine((int) p[0], (int) p[1] + 7, (int) p[0], y1);
        }
        g.setColor(LINE);
        g.setStroke(new BasicStroke(2.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 1; i < pts.size(); i++) {
            g.draw(new java.awt.geom.Line2D.Double(pts.get(i - 1)[0], pts.get(i - 1)[1], pts.get(i)[0], pts.get(i)[1]));
        }
        g.setFont(Theme.bold(12));
        fm = g.getFontMetrics();
        for (int k = 0; k < pts.size(); k++) {
            double[] p = pts.get(k);
            int i = idx.get(k);
            boolean on = i == hover;
            double rr = on ? 6.5 : 5;
            if (on) {
                g.setColor(new Color(0x22, 0xBE, 0x8E, 50));
                g.fill(new Ellipse2D.Double(p[0] - 11, p[1] - 11, 22, 22));
            }
            g.setColor(Color.WHITE);
            g.fill(new Ellipse2D.Double(p[0] - rr, p[1] - rr, rr * 2, rr * 2));
            g.setColor(LINE);
            g.setStroke(new BasicStroke(on ? 3f : 2.4f));
            g.draw(new Ellipse2D.Double(p[0] - rr, p[1] - rr, rr * 2, rr * 2));
            if (!on) {
                String s = buckets.get(i).rate().percent() + "%";
                halo(g, s, (int) p[0] - fm.stringWidth(s) / 2, (int) (p[1] - rr - 6));
            }
        }
        if (hover >= 0 && hover < buckets.size()) {
            int k = idx.indexOf(hover);
            double px = x0 + col * (hover + 0.5);
            double py = k >= 0 ? pts.get(k)[1] : y1;
            bubble(g, tip(hover), px, py);
        }
        g.dispose();
    }

    /** 점 위에 꼬리 달린 말풍선. 위에 자리가 없으면 점 아래에 둔다. */
    private void bubble(Graphics2D g, List<String> lines, double px, double py) {
        g.setFont(Theme.font(12));
        FontMetrics fm = g.getFontMetrics();
        int lh = fm.getHeight();
        int tw = lines.stream().mapToInt(fm::stringWidth).max().orElse(0);
        int bw = tw + 24;
        int bh = lh * lines.size() + 14;
        int tail = 8;
        int bx = (int) Math.max(2, Math.min(getWidth() - bw - 2, px - bw / 2.0));
        boolean above = py - 12 - tail - bh >= 0;
        int by = above ? (int) (py - 12 - tail - bh) : (int) (py + 12 + tail);
        Card.shadow(g, bx, by, bw, bh, 12);
        Path2D shape = new Path2D.Double();
        shape.append(new RoundRectangle2D.Double(bx, by, bw, bh, 12, 12), false);
        Path2D t = new Path2D.Double();
        double tx = Math.max(bx + 12, Math.min(bx + bw - 12, px));
        if (above) {
            t.moveTo(tx - 7, by + bh - 1);
            t.lineTo(tx, by + bh + tail);
            t.lineTo(tx + 7, by + bh - 1);
        } else {
            t.moveTo(tx - 7, by + 1);
            t.lineTo(tx, by - tail);
            t.lineTo(tx + 7, by + 1);
        }
        t.closePath();
        java.awt.geom.Area a = new java.awt.geom.Area(shape);
        a.add(new java.awt.geom.Area(t));
        g.setColor(Color.WHITE);
        g.fill(a);
        g.setColor(new Color(0xCFE3EA));
        g.setStroke(new BasicStroke(1.1f));
        g.draw(a);
        int y = by + 7 + fm.getAscent();
        for (int i = 0; i < lines.size(); i++) {
            g.setFont(i == 0 ? Theme.bold(12) : Theme.font(12));
            g.setColor(Theme.INK);
            g.drawString(lines.get(i), bx + 12, y);
            y += lh;
        }
    }

    /** 막대 안 숫자의 세로 가운데. 아래쪽에 두되 완료율 점과 겹치면 막대 위쪽이나 바닥 쪽으로 비킨다. */
    private static double countMiddle(double bh, int y1, int pointY) {
        double[] tries = bh >= 40 ? new double[] {y1 - Math.min(bh * 0.42, 20), y1 - bh + 14, y1 - 10}
                : new double[] {y1 - Math.min(bh * 0.42, 20), y1 - 10};
        double best = tries[0];
        for (double t : tries) {
            if (Math.abs(t - pointY) >= 16) {
                return t;
            }
            if (Math.abs(t - pointY) > Math.abs(best - pointY)) {
                best = t;
            }
        }
        return best;
    }

    /** 막대 위에 겹쳐도 읽히게 흰 테두리를 두른 글자. */
    private static void halo(Graphics2D g, String s, int x, int y) {
        g.setColor(new Color(255, 255, 255, 220));
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx != 0 || dy != 0) {
                    g.drawString(s, x + dx, y + dy);
                }
            }
        }
        g.setColor(Theme.INK);
        g.drawString(s, x, y);
    }

    /** 오른쪽 위 범례: 완료율 점과 완료 일정 수 막대(예시의 범례). */
    private static void legend(Graphics2D g, int right) {
        g.setFont(Theme.font(12));
        FontMetrics fm = g.getFontMetrics();
        String a = "완료율(%)";
        String b = "완료 일정 수";
        int y = 10;
        int x = right - (15 + fm.stringWidth(a) + 14 + 19 + fm.stringWidth(b));
        g.setColor(LINE);
        g.fillOval(x, y - 5, 10, 10);
        g.setColor(Color.WHITE);
        g.fillOval(x + 3, y - 2, 4, 4);
        x += 15;
        g.setColor(Theme.INK_SUB);
        g.drawString(a, x, y + fm.getAscent() / 2 - 1);
        x += fm.stringWidth(a) + 14;
        g.setPaint(new GradientPaint(0, y - 6, BAR_TOP, 0, y + 6, BAR_BOTTOM));
        g.fillRoundRect(x, y - 6, 14, 12, 4, 4);
        x += 19;
        g.setColor(Theme.INK_SUB);
        g.drawString(b, x, y + fm.getAscent() / 2 - 1);
    }

    private static int yOf(int percent, int y0, int y1) {
        return (int) Math.round(y1 - (y1 - y0) * percent / 100.0);
    }

    private static Color fade(Color c) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), 102);
    }

    /** 위 두 모서리만 둥근 막대. */
    private static Path2D topRounded(double x, double y, double w, double h, double r) {
        Path2D p = new Path2D.Double();
        p.moveTo(x, y + h);
        p.lineTo(x, y + r);
        p.quadTo(x, y, x + r, y);
        p.lineTo(x + w - r, y);
        p.quadTo(x + w, y, x + w, y + r);
        p.lineTo(x + w, y + h);
        p.closePath();
        return p;
    }
}
