package planningpet.common.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JComponent;

/**
 * 디자인 시스템 5절의 진행 막대. 양 끝이 둥근 TRACK 바탕에 민트 채움.
 * 칸 수를 주면(segments) 칸마다 떨어진 둥근 조각으로 그리고, 끝낸 칸만 민트다(홈 펫 카드의 오늘 끝낸 일).
 */
public final class Bar extends JComponent {
    /** 칸으로 나눠 그리는 가장 큰 칸 수. 이보다 많으면 이어진 막대다. */
    public static final int MAX_SEGMENTS = 10;
    private double ratio;
    private Color track = Theme.TRACK;
    private int segments;
    private int done;

    public Bar(int width, int height) {
        setPreferredSize(new Dimension(width, height));
        setMinimumSize(new Dimension(20, height));
    }

    /** 0부터 1까지. */
    public void set(double ratio) {
        this.ratio = Math.max(0, Math.min(1, ratio));
        this.segments = 0;
        repaint();
    }

    /** done / total. total이 1부터 MAX_SEGMENTS까지면 칸으로, 0이면 빈 막대, 그보다 많으면 이어진 막대로 그린다. */
    public void set(int done, int total) {
        this.ratio = total == 0 ? 0 : Math.max(0, Math.min(1, done / (double) total));
        this.segments = total <= MAX_SEGMENTS ? total : 0;
        this.done = done;
        repaint();
    }

    /** 바탕 색. 흰 카드 밖(하늘 위)에서는 TRACK_DEEP을 준다. */
    public Bar track(Color color) {
        this.track = color;
        return this;
    }

    public double ratio() {
        return ratio;
    }

    /** 칸으로 그리는 중이면 칸 수, 아니면 0. */
    public int segments() {
        return segments;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int h = Math.min(getHeight(), getPreferredSize().height);
        int y = (getHeight() - h) / 2;
        int w = getWidth();
        g.fillRoundRect(0, y, w, h, h, h);
        int fw = (int) Math.round(w * ratio);
        if (fw > 0) {
            fw = Math.max(fw, h);
            g.setPaint(new GradientPaint(0, y, Theme.MINT_TOP, 0, y + h, Theme.MINT));
            g.fillRoundRect(0, y, fw, h, h, h);
        }
        g.dispose();
    }
}
