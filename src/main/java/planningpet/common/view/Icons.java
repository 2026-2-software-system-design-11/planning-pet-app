package planningpet.common.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import javax.swing.Icon;

/**
 * 디자인 시스템 9절의 선 아이콘 한 벌. 24 칸 격자에 그리고 크기만큼 늘린다. 선 굵기는 격자 2(크기의 1/12), 끝은 둥글다.
 * 창 틀이 쓰는 것만 옮겼고, 다른 종은 그것을 쓰는 화면의 단위가 더한다(docs/porting/icons.md).
 */
public final class Icons {
    /** 아이콘의 종류. */
    public enum Kind { HOME, CALENDAR, CHART, PAW }

    private Icons() {
    }

    /** Swing 부품에 붙이는 아이콘. */
    public static Icon icon(Kind kind, int size, Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                draw((Graphics2D) g, kind, x, y, size, color);
            }

            @Override
            public int getIconWidth() {
                return size;
            }

            @Override
            public int getIconHeight() {
                return size;
            }
        };
    }

    /** (x, y)에서 size 크기로 그린다. 채우는 아이콘(발자국)은 color로 채운다. */
    public static void draw(Graphics2D graphics, Kind kind, int x, int y, int size, Color color) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.translate(x, y);
        double k = size / 24.0;
        g.scale(k, k);
        g.setColor(color);
        g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        switch (kind) {
            case HOME -> {
                g.draw(path(3, 11, 12, 3.5, 21, 11));
                g.draw(new RoundRectangle2D.Double(5.5, 9.5, 13, 11, 3, 3));
                g.draw(new RoundRectangle2D.Double(10, 14, 4, 6.5, 2, 2));
            }
            case CALENDAR -> {
                g.draw(new RoundRectangle2D.Double(3.5, 5, 17, 15.5, 4, 4));
                g.draw(path(3.5, 9.5, 20.5, 9.5));
                g.draw(path(8, 3, 8, 6.5));
                g.draw(path(16, 3, 16, 6.5));
                g.draw(path(8.5, 14.8, 11, 17, 15.5, 12.8));
            }
            case CHART -> {
                g.draw(new RoundRectangle2D.Double(3.5, 13, 4, 7.5, 2.5, 2.5));
                g.draw(new RoundRectangle2D.Double(10, 9, 4, 11.5, 2.5, 2.5));
                g.draw(new RoundRectangle2D.Double(16.5, 4, 4, 16.5, 2.5, 2.5));
            }
            case PAW -> paw(g, 12, 13.5, 1.6);
        }
        g.dispose();
    }

    /** 발자국: 가운데 (cx, cy), 비율 s. 채워서 그린다. */
    private static void paw(Graphics2D g, double cx, double cy, double s) {
        g.fill(new Ellipse2D.Double(cx - 3.2 * s, cy - 1.2 * s, 6.4 * s, 5.2 * s));
        double[][] toes = {{-4.2, -3.2}, {-1.5, -5.4}, {1.5, -5.4}, {4.2, -3.2}};
        for (double[] t : toes) {
            g.fill(new Ellipse2D.Double(cx + t[0] * s - 1.25 * s, cy + t[1] * s - 1.4 * s, 2.5 * s, 2.8 * s));
        }
    }

    private static Path2D path(double... xy) {
        Path2D p = new Path2D.Double();
        p.moveTo(xy[0], xy[1]);
        for (int i = 2; i < xy.length; i += 2) {
            p.lineTo(xy[i], xy[i + 1]);
        }
        return p;
    }
}
