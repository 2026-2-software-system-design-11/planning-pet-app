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
 * 예시(2026-11-23 나의 방)의 진한 청록 선 아이콘을 따랐다.
 */
public final class Icons {
    public enum Kind { HOME, ROOM, CALENDAR, CHART, GEAR, PAW, COIN, CART, SOFA, INFO, CHECK, HEART, SPARK, FLAME, BOX,
        GAMEPAD, SEARCH, LEFT, RIGHT, PLUS, PENCIL, LOCK, SHIRT, HAT, BOW, COLLAR, BALL, GRID, DOTS, PLANT, CLOSE, EYE }

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

    /** (x, y)에서 size 크기로 그린다. 채우는 아이콘(동전, 하트, 체크, 발자국)은 color를 바탕색으로 쓴다. */
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
            case GAMEPAD -> {
                g.draw(new RoundRectangle2D.Double(2.5, 7, 19, 11, 9, 9));
                g.draw(path(7, 10.5, 7, 14.5));
                g.draw(path(5, 12.5, 9, 12.5));
                g.fill(new Ellipse2D.Double(15, 10, 2.4, 2.4));
                g.fill(new Ellipse2D.Double(17, 13, 2.4, 2.4));
            }
            case SEARCH -> {
                g.draw(new Ellipse2D.Double(4, 4, 12, 12));
                g.draw(path(14.5, 14.5, 20, 20));
            }
            case LEFT -> g.draw(path(15, 5, 8, 12, 15, 19));
            case RIGHT -> g.draw(path(9, 5, 16, 12, 9, 19));
            case PLUS -> {
                g.draw(path(12, 5, 12, 19));
                g.draw(path(5, 12, 19, 12));
            }
            case CLOSE -> {
                g.draw(path(6, 6, 18, 18));
                g.draw(path(18, 6, 6, 18));
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
            case BALL -> {
                g.draw(new Ellipse2D.Double(3.5, 3.5, 17, 17));
                g.draw(new Arc2D.Double(-3, 6, 14, 12, -60, 120, Arc2D.OPEN));
                g.draw(new Arc2D.Double(13, 6, 14, 12, 120, 120, Arc2D.OPEN));
            }
            case GRID -> {
                g.fill(new RoundRectangle2D.Double(4, 4, 7, 7, 2, 2));
                g.fill(new RoundRectangle2D.Double(13, 4, 7, 7, 2, 2));
                g.fill(new RoundRectangle2D.Double(4, 13, 7, 7, 2, 2));
                g.fill(new RoundRectangle2D.Double(13, 13, 7, 7, 2, 2));
            }
            case DOTS -> {
                g.fill(new Ellipse2D.Double(3, 10, 4, 4));
                g.fill(new Ellipse2D.Double(10, 10, 4, 4));
                g.fill(new Ellipse2D.Double(17, 10, 4, 4));
            }
            case PLANT -> {
                g.draw(path(7, 15, 17, 15, 15.5, 21, 8.5, 21, 7, 15));
                g.draw(path(12, 15, 12, 9));
                g.draw(path(12, 11, 6, 6.5, 6.5, 11, 12, 11));
                g.draw(path(12, 9, 17.5, 4, 17.5, 8.5, 12, 9));
            }
            case EYE -> {
                g.draw(path(2.5, 12, 7, 6.5, 12, 5.5, 17, 6.5, 21.5, 12, 17, 17.5, 12, 18.5, 7, 17.5, 2.5, 12));
                g.draw(new Ellipse2D.Double(9, 9, 6, 6));
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
            case CART -> {
                g.draw(path(2.5, 4, 5.5, 4, 8, 15.5, 18.5, 15.5, 20.5, 8, 6.6, 8));
