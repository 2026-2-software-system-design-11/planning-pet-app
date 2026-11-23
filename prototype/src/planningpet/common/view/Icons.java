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
