package planningpet.common.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import javax.swing.Icon;

/**
 * 디자인 시스템 9절의 선 아이콘 한 벌. 24 칸 격자에 그리고 크기만큼 늘린다. 선 굵기는 격자 2(크기의 1/12), 끝은 둥글다.
 * 창 틀, 고르기 창, 홈 화면, 캐릭터 창이 쓰는 것만 옮겼고, 다른 종은 그것을 쓰는 화면의 단위가 더한다
 * (docs/porting/icons.md, pick.md, home.md, character.md).
 */
public final class Icons {
    /** 아이콘의 종류. */
    public enum Kind { HOME, ROOM, CALENDAR, CHART, GEAR, PAW, COIN, INFO, CHECK, HEART, SPARK, FLAME, LEFT, RIGHT, DOWN, PLUS, PENCIL,
        LOCK, SHIRT, HAT, BOW, COLLAR, TRASH, CLOCK, CHECKLIST }

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

    /** (x, y)에서 size 크기로 그린다. 채우는 아이콘(동전, 체크, 발자국)은 color를 바탕색으로 쓴다. */
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
            case ROOM -> {
                g.draw(path(3, 11, 12, 3.5, 21, 11));
                g.draw(new RoundRectangle2D.Double(5.5, 9.5, 13, 11, 3, 3));
                paw(g, 12, 15.6, 0.62);
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
            case GEAR -> {
                Path2D p = new Path2D.Double();
                for (int i = 0; i < 16; i++) {
                    double a = Math.PI * 2 * i / 16 - Math.PI / 16;
                    double r = (i % 2 == 0) ? 9.3 : 7.2;
                    double a2 = a + Math.PI * 2 / 16;
                    if (i == 0) {
                        p.moveTo(12 + r * Math.cos(a), 12 + r * Math.sin(a));
                    } else {
                        p.lineTo(12 + r * Math.cos(a), 12 + r * Math.sin(a));
                    }
                    p.lineTo(12 + r * Math.cos(a2), 12 + r * Math.sin(a2));
                }
                p.closePath();
                g.draw(p);
                g.draw(new Ellipse2D.Double(9, 9, 6, 6));
            }
            case PAW -> paw(g, 12, 13.5, 1.6);
            case LEFT -> g.draw(path(15, 5, 8, 12, 15, 19));
            case RIGHT -> g.draw(path(9, 5, 16, 12, 9, 19));
            case DOWN -> g.draw(path(5, 9, 12, 16, 19, 9));
            case PLUS -> {
                g.draw(path(12, 5, 12, 19));
                g.draw(path(5, 12, 19, 12));
            }
            case PENCIL -> {
                g.draw(path(4, 20, 5, 15.5, 15.5, 5, 19, 8.5, 8.5, 19, 4, 20));
                g.draw(path(13, 7.5, 16.5, 11));
            }
            case LOCK -> {
                g.draw(new RoundRectangle2D.Double(5, 10.5, 14, 10, 3, 3));
                g.draw(new Arc2D.Double(8, 4, 8, 12, 0, 180, Arc2D.OPEN));
                g.draw(path(8, 10, 8, 10.5));
                g.draw(path(16, 10, 16, 10.5));
            }
            case SHIRT -> g.draw(path(8.5, 4, 3.5, 7, 5.5, 11, 7.5, 10, 7.5, 20, 16.5, 20, 16.5, 10, 18.5, 11, 20.5, 7, 15.5, 4,
                    14, 6, 10, 6, 8.5, 4));
            case HAT -> {
                g.draw(new Arc2D.Double(6, 6, 12, 14, 0, 180, Arc2D.OPEN));
                g.draw(new RoundRectangle2D.Double(3, 13, 18, 4, 4, 4));
            }
            case BOW -> {
                g.draw(path(12, 12, 4, 7, 4, 17, 12, 12, 20, 7, 20, 17, 12, 12));
                g.fill(new Ellipse2D.Double(10.3, 10.3, 3.4, 3.4));
            }
            case COLLAR -> {
                g.draw(new Ellipse2D.Double(3, 6, 18, 8));
                g.fill(new Ellipse2D.Double(10, 13, 4, 4));
            }
            case INFO -> {
                g.draw(new Ellipse2D.Double(3, 3, 18, 18));
                g.draw(path(12, 11, 12, 16.5));
                g.fill(new Ellipse2D.Double(10.8, 6.6, 2.4, 2.4));
            }
            case TRASH -> {
                g.draw(path(4, 6.5, 20, 6.5));
                g.draw(path(9.5, 6.5, 10, 3.8, 14, 3.8, 14.5, 6.5));
                g.draw(path(6, 6.5, 7.2, 19.5, 7.6, 20.5, 16.4, 20.5, 16.8, 19.5, 18, 6.5));
                g.draw(path(10, 10.5, 10, 16.5));
                g.draw(path(14, 10.5, 14, 16.5));
            }
            case CLOCK -> {
                g.draw(new Ellipse2D.Double(3.5, 3.5, 17, 17));
                g.draw(path(12, 7.5, 12, 12, 15.2, 14));
            }
            case CHECKLIST -> {
                g.draw(new RoundRectangle2D.Double(4.5, 4.5, 15, 16.5, 4, 4));
                g.draw(new RoundRectangle2D.Double(8.5, 2.5, 7, 4, 2, 2));
                g.draw(path(7.8, 11.5, 9.3, 13, 11.6, 10.5));
                g.draw(path(13.5, 11.8, 16.5, 11.8));
                g.draw(path(7.8, 16.5, 9.3, 18, 11.6, 15.5));
                g.draw(path(13.5, 16.8, 16.5, 16.8));
            }
            case COIN -> {
                g.setColor(Theme.COIN_RIM);
                g.fill(new Ellipse2D.Double(1, 1, 22, 22));
                g.setColor(color);
                g.fill(new Ellipse2D.Double(2.6, 2.2, 18.8, 18.8));
                g.setColor(new Color(255, 255, 255, 120));
                g.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.draw(new Arc2D.Double(5, 4.5, 14, 14, 100, 80, Arc2D.OPEN));
                g.setColor(Theme.COIN_RIM);
                paw(g, 12, 12.6, 0.82);
            }
            case CHECK -> {
                g.fill(new Ellipse2D.Double(1, 1, 22, 22));
                g.setColor(Color.WHITE);
                g.setStroke(new BasicStroke(2.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.draw(path(7, 12.4, 10.6, 16, 17.2, 8.6));
            }
            case HEART -> {
                Path2D h = new Path2D.Double();
                h.moveTo(12, 21);
                h.curveTo(4, 15.5, 1.5, 11, 3.2, 7);
                h.curveTo(4.8, 3.4, 9.8, 2.8, 12, 7.2);
                h.curveTo(14.2, 2.8, 19.2, 3.4, 20.8, 7);
                h.curveTo(22.5, 11, 20, 15.5, 12, 21);
                h.closePath();
                g.fill(h);
                g.setColor(new Color(255, 255, 255, 150));
                g.fill(new Ellipse2D.Double(5.6, 7, 3.4, 2.6));
            }
            case SPARK -> {
                g.setStroke(new BasicStroke(2.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.draw(path(4, 14, 9, 16));
                g.draw(path(7.5, 6.5, 11, 10.5));
                g.draw(path(15, 3.5, 15.5, 8.5));
            }
            case FLAME -> {
                Path2D f = new Path2D.Double();
                f.moveTo(12, 21);
                f.curveTo(6.5, 21, 4.5, 17, 5.5, 13.5);
                f.curveTo(6.3, 10.8, 8.5, 10, 9, 7.5);
                f.curveTo(11, 9, 11.2, 10.5, 11, 12);
                f.curveTo(13, 10, 13.5, 6, 12.5, 3);
                f.curveTo(17, 5.5, 19.5, 10.5, 18.6, 15);
                f.curveTo(18, 18.5, 15.5, 21, 12, 21);
                f.closePath();
                g.draw(f);
            }
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
