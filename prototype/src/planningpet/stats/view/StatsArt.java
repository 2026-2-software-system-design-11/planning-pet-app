package planningpet.stats.view;

import planningpet.common.view.Icons;
import planningpet.common.view.Theme;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.Map;
import javax.imageio.ImageIO;

/**
 * 통계 화면만 쓰는 그림: 카드 제목 아이콘, 일정 유형(D-54) 아이콘과 막대 색, 장식 고양이.
 * 아이콘은 common/view/Icons와 같이 24 칸 격자에 굵기 2의 둥근 선으로 그린다(디자인 시스템 9절).
 */
final class StatsArt {
    /** 셀 일정이 없을 때 카드에 쓰는 말(D-55). */
    static final String NONE = "아직 셀 일정이 없어요";

    enum Kind { TARGET, PIE, SWAP, BOOK, DUMBBELL, LAPTOP, CUP, TAG, HELP, LEFT, RIGHT, RING }

    /** 머리 오른쪽에서 내다보는 고양이. 파일이 없으면 null이고 그리지 않는다. */
    static final BufferedImage PEEK_CAT = load("peek-cat.png");
    /** 사이드바 아래에서 자는 고양이. 파일이 없으면 null이다. */
    static final BufferedImage SLEEP_CAT = load("sleep-cat.png");

    /** 유형마다 아이콘, 아이콘 색, 막대의 위와 아래 색. 예시의 유형별 달성률을 따랐다. */
    record Look(Kind icon, Color ink, Color top, Color bottom) {
    }

    private static final Map<String, Look> LOOKS = Map.of(
            "학업", new Look(Kind.BOOK, new Color(0x3A8EDB), new Color(0x62E2B6), new Color(0x2CC490)),
            "운동", new Look(Kind.DUMBBELL, new Color(0xE55C64), new Color(0x6CBBFF), new Color(0x3A8EF0)),
            "프로젝트", new Look(Kind.LAPTOP, new Color(0x8F55DD), new Color(0xC390FF), new Color(0x9B5DE5)),
            "개인/취미", new Look(Kind.CUP, new Color(0xEE9A1C), new Color(0xFFDA55), new Color(0xF6B921)));
    private static final Look OTHER = new Look(Kind.TAG, new Color(0x6F8296), new Color(0xC3D0DB), new Color(0x93A6B7));

    private StatsArt() {
    }

    static Look look(String category) {
        return LOOKS.getOrDefault(category, OTHER);
    }

    private static BufferedImage load(String name) {
        try (InputStream in = StatsArt.class.getResourceAsStream("/assets/stats/" + name)) {
            return in == null ? null : ImageIO.read(in);
        } catch (java.io.IOException e) {
            return null;
        }
    }

    /** Swing 부품에 붙이는 아이콘. */
    static javax.swing.Icon icon(Kind kind, int size, Color color) {
        return new javax.swing.Icon() {
            @Override
            public void paintIcon(java.awt.Component c, java.awt.Graphics g, int x, int y) {
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

    static void draw(Graphics2D graphics, Kind kind, int x, int y, int size, Color color) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.translate(x, y);
        double k = size / 24.0;
        g.scale(k, k);
        g.setColor(color);
        g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        switch (kind) {
            case TARGET -> {
                g.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.draw(new Arc2D.Double(2.5, 3.5, 18, 18, 60, 300, Arc2D.OPEN));
                g.draw(new Ellipse2D.Double(7, 8, 9, 9));
                g.fill(new Ellipse2D.Double(10, 11, 3, 3));
                g.draw(path(11.5, 12.5, 19.5, 4.5));
                g.draw(path(17, 2.5, 17, 7, 21.5, 7));
            }
            case PIE -> {
                g.draw(new Arc2D.Double(3, 4, 17, 17, 90, 270, Arc2D.PIE));
                g.fill(new Arc2D.Double(5, 2, 17, 17, 0, 90, Arc2D.PIE));
            }
            case SWAP -> {
                g.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.draw(path(3, 8, 20, 8));
                g.draw(path(16, 4, 20, 8, 16, 12));
                g.draw(path(21, 16, 4, 16));
                g.draw(path(8, 12, 4, 16, 8, 20));
            }
            case BOOK -> {
                Path2D left = new Path2D.Double();
                left.moveTo(12, 6.5);
                left.curveTo(9.5, 4.8, 6, 4.6, 2.5, 5.5);
                left.lineTo(2.5, 18.5);
                left.curveTo(6, 17.6, 9.5, 17.8, 12, 19.5);
                left.closePath();
                g.draw(left);
                Path2D right = new Path2D.Double();
                right.moveTo(12, 6.5);
                right.curveTo(14.5, 4.8, 18, 4.6, 21.5, 5.5);
                right.lineTo(21.5, 18.5);
                right.curveTo(18, 17.6, 14.5, 17.8, 12, 19.5);
                right.closePath();
                g.draw(right);
                g.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.draw(path(5, 9, 9.5, 9.6));
                g.draw(path(5, 12.5, 9.5, 13.1));
                g.draw(path(14.5, 9.6, 19, 9));
                g.draw(path(14.5, 13.1, 19, 12.5));
            }
            case DUMBBELL -> {
                g.fill(new RoundRectangle2D.Double(1.5, 9, 3, 6, 1.5, 1.5));
                g.fill(new RoundRectangle2D.Double(4.5, 6, 3.6, 12, 2, 2));
                g.fill(new RoundRectangle2D.Double(19.5, 9, 3, 6, 1.5, 1.5));
                g.fill(new RoundRectangle2D.Double(15.9, 6, 3.6, 12, 2, 2));
                g.setStroke(new BasicStroke(2.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.draw(path(8, 12, 16, 12));
            }
            case LAPTOP -> {
                g.draw(new RoundRectangle2D.Double(4.5, 5, 15, 10.5, 2.5, 2.5));
                g.draw(path(2, 18.5, 22, 18.5));
                g.draw(path(4.5, 15.5, 2.5, 18.5));
                g.draw(path(19.5, 15.5, 21.5, 18.5));
            }
            case CUP -> {
                Path2D cup = new Path2D.Double();
                cup.moveTo(4, 9);
                cup.lineTo(16.5, 9);
                cup.lineTo(16, 16);
                cup.curveTo(15.8, 18.5, 14, 20, 11.5, 20);
                cup.lineTo(9, 20);
                cup.curveTo(6.5, 20, 4.7, 18.5, 4.5, 16);
                cup.closePath();
                g.draw(cup);
                g.draw(new Arc2D.Double(13.5, 10, 7, 6, -90, 180, Arc2D.OPEN));
                g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.draw(path(8, 6.5, 8.6, 4.8, 8, 3));
                g.draw(path(12, 6.5, 12.6, 4.8, 12, 3));
            }
            case TAG -> {
                g.draw(path(3.5, 4.5, 11.5, 4.5, 20.5, 13.5, 13.5, 20.5, 3.5, 11.5, 3.5, 4.5));
                g.fill(new Ellipse2D.Double(6.5, 7.5, 3, 3));
            }
            case HELP -> {
                g.setStroke(new BasicStroke(1.9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.draw(new Ellipse2D.Double(2.5, 2.5, 19, 19));
                Path2D q = new Path2D.Double();
                q.moveTo(9.2, 9.6);
                q.curveTo(9.2, 7.6, 10.6, 6.6, 12.1, 6.6);
                q.curveTo(13.8, 6.6, 15, 7.8, 15, 9.3);
                q.curveTo(15, 11.6, 12.2, 11.7, 12.2, 14);
                g.draw(q);
                g.fill(new Ellipse2D.Double(10.9, 16, 2.6, 2.6));
            }
            case LEFT -> {
                g.setStroke(new BasicStroke(2.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.draw(path(14.5, 5.5, 8, 12, 14.5, 18.5));
            }
            case RIGHT -> {
                g.setStroke(new BasicStroke(2.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.draw(path(9.5, 5.5, 16, 12, 9.5, 18.5));
            }
