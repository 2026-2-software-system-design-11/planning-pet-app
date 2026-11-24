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
