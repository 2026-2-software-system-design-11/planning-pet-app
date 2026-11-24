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
