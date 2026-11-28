package planningpet.common.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JComponent;

/** 디자인 시스템 5절의 진행 막대. 양 끝이 둥근 TRACK 바탕에 민트 채움(docs/porting/home.md S3i). */
public final class Bar extends JComponent {
    private double ratio;
    private Color track = Theme.TRACK;

    public Bar(int width, int height) {
        setPreferredSize(new Dimension(width, height));
        setMinimumSize(new Dimension(20, height));
    }

    /** 0부터 1까지. */
    public void set(double ratio) {
        this.ratio = Math.max(0, Math.min(1, ratio));
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

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int h = Math.min(getHeight(), getPreferredSize().height);
        int y = (getHeight() - h) / 2;
        int w = getWidth();
        g.setColor(track);
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
