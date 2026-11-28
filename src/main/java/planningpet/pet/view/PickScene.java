package planningpet.pet.view;

import planningpet.common.view.Theme;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;

/**
 * 반려동물 고르기 창의 그림 도구(2026-11-23 고르기 예시, 디자인 시스템 10절 "고르기 창").
 * 하늘 언덕 장면, 리본, 데이지, 반짝이 별, 풀 덤불을 도형으로 그린다. 도형이라 2배 화면에서도 흐려지지 않는다.
 */
final class PickScene {
    private PickScene() {
    }

    static void hints(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    /** 노란 꽃봉오리. */
    static void bud(Graphics2D g, double cx, double cy, double r) {
        g.setColor(Theme.DAISY_HEART);
        g.fill(new Ellipse2D.Double(cx - r, cy - r, r * 2, r * 2));
        g.setColor(alpha(Color.WHITE, 140));
        g.fill(new Ellipse2D.Double(cx - r * 0.5, cy - r * 0.6, r * 0.7, r * 0.5));
    }

    /** 네 갈래 반짝이 별: 가운데 (cx, cy), 반지름 r. 흰 별에 노란 빛. */
    static void star(Graphics2D g, double cx, double cy, double r) {
        g.setColor(alpha(Theme.DAISY_HEART, 70));
        g.fill(new Ellipse2D.Double(cx - r * 0.7, cy - r * 0.7, r * 1.4, r * 1.4));
        Path2D s = new Path2D.Double();
        s.moveTo(cx, cy - r);
        s.curveTo(cx + r * 0.12, cy - r * 0.12, cx + r * 0.12, cy - r * 0.12, cx + r, cy);
        s.curveTo(cx + r * 0.12, cy + r * 0.12, cx + r * 0.12, cy + r * 0.12, cx, cy + r);
        s.curveTo(cx - r * 0.12, cy + r * 0.12, cx - r * 0.12, cy + r * 0.12, cx - r, cy);
        s.curveTo(cx - r * 0.12, cy - r * 0.12, cx - r * 0.12, cy - r * 0.12, cx, cy - r);
        s.closePath();
        g.setColor(Color.WHITE);
        g.fill(s);
    }

    static Color alpha(Color c, int a) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), a);
    }
}
