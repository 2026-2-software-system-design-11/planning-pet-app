package planningpet.pet.view;

import planningpet.common.view.Icons;
import planningpet.common.view.Theme;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
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

    /** 풀 덤불: 아래 가운데 (cx, bottom), 폭 s. 둥근 잎 뭉치 여섯을 세 가지 초록으로 겹친다. */
    static void bush(Graphics2D g, double cx, double bottom, double s) {
        // 뒤 잎은 진하게, 앞 잎은 밝게. 예시 덤불처럼 둥근 잎 뭉치가 겹친다.
        double[][] puffs = {{-0.36, 0.48, 0.46, 0}, {0.30, 0.52, 0.48, 0}, {-0.10, 0.80, 0.56, 1}, {0.16, 0.62, 0.50, 1},
            {-0.30, 0.34, 0.40, 2}, {0.36, 0.30, 0.38, 2}};
        Color[] tones = {mix(Theme.GRASS_DEEP, Theme.ICON, 0.18), Theme.GRASS_DEEP, mix(Theme.GRASS_DEEP, Theme.GRASS, 0.45)};
        for (double[] p : puffs) {
            double r = p[2] * s / 2;
            double px = cx + p[0] * s;
            double top = bottom - p[1] * s;
            g.setColor(tones[(int) p[3]]);
            g.fill(new Ellipse2D.Double(px - r, top, r * 2, bottom - top + r));
        }
        g.setColor(alpha(Color.WHITE, 70));
        g.fill(new Ellipse2D.Double(cx - s * 0.16, bottom - s * 0.74, s * 0.16, s * 0.09));
    }

    /** 데이지: 가운데 (cx, cy), 꽃잎 반지름 r, 기울기 turn(라디안). 꽃잎 여섯, 노란 가운데, 옅은 그림자 한 단. */
    static void daisy(Graphics2D g0, double cx, double cy, double r, double turn) {
        Graphics2D g = (Graphics2D) g0.create();
        g.translate(cx, cy);
        g.rotate(turn);
        Color line = mix(Theme.DAISY, Theme.INK_SUB, 0.16);
        for (int i = 0; i < 6; i++) {
            AffineTransform keep = g.getTransform();
            g.rotate(Math.PI * 2 * i / 6);
            Ellipse2D petal = new Ellipse2D.Double(-r * 0.30, -r, r * 0.60, r * 0.95);
            g.setColor(Theme.DAISY);
            g.fill(petal);
            g.setColor(line);
            g.setStroke(new BasicStroke((float) Math.max(0.6, r / 14)));
            g.draw(petal);
            g.setTransform(keep);
        }
        g.setColor(Theme.DAISY_HEART);
        g.fill(new Ellipse2D.Double(-r * 0.32, -r * 0.32, r * 0.64, r * 0.64));
        g.setColor(mix(Theme.DAISY_HEART, Theme.COIN_RIM, 0.5));
        g.fill(new Ellipse2D.Double(-r * 0.12, r * 0.02, r * 0.30, r * 0.22));
        g.dispose();
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

    /**
     * 리본: 가운데 띠(x, y, w, h)가 위로 살짝 굽고, 양 끝에서 꼬리가 뒤로 접혀 아래로 나온다.
     * 띠 아래 옅은 그림자, 띠 위 가장자리에 밝은 선.
     */
    static void ribbon(Graphics2D g, double x, double y, double w, double h) {
        double bend = h * 0.16;
        double tail = h * 0.95;
        double drop = h * 0.32;
        for (int side = 0; side < 2; side++) {
            double dir = side == 0 ? -1 : 1;
            double ex = side == 0 ? x : x + w;
            // 꼬리: 띠 끝 뒤에서 바깥으로 뻗고 끝이 V로 파였다.
            Path2D t = new Path2D.Double();
            t.moveTo(ex - dir * h * 0.35, y + drop);
            t.lineTo(ex + dir * tail, y + drop - h * 0.02);
            t.lineTo(ex + dir * tail * 0.72, y + drop + h * 0.50);
            t.lineTo(ex + dir * tail, y + drop + h * 1.0);
            t.lineTo(ex - dir * h * 0.35, y + drop + h * 1.0);
            t.closePath();
            g.setColor(Theme.RIBBON_TAIL);
            g.fill(t);
            // 접힌 안쪽: 띠 끝 아래의 진한 삼각형.
            Path2D fold = new Path2D.Double();
            fold.moveTo(ex, y + h - bend * 0.2);
            fold.lineTo(ex - dir * h * 0.35, y + drop + h * 1.0);
            fold.lineTo(ex - dir * h * 0.35, y + h - bend * 0.2);
            fold.closePath();
            g.setColor(Theme.RIBBON_DEEP.darker());
            g.fill(fold);
        }
        Path2D band = new Path2D.Double();
        band.moveTo(x, y + bend);
        band.quadTo(x + w / 2, y - bend, x + w, y + bend);
        band.lineTo(x + w, y + h + bend * 0.2);
        band.quadTo(x + w / 2, y + h - bend * 1.6, x, y + h + bend * 0.2);
        band.closePath();
        g.setColor(alpha(Theme.RIBBON_DEEP, 40));
        g.fill(AffineTransform.getTranslateInstance(0, h * 0.10).createTransformedShape(band));
        g.setPaint(new GradientPaint(0, (float) y, Theme.RIBBON_TOP, 0, (float) (y + h), Theme.RIBBON_DEEP));
        g.fill(band);
        Path2D shine = new Path2D.Double();
        shine.moveTo(x + h * 0.3, y + bend + 2);
        shine.quadTo(x + w / 2, y - bend + 2, x + w - h * 0.3, y + bend + 2);
        g.setColor(alpha(Color.WHITE, 90));
        g.setStroke(new BasicStroke((float) Math.max(1, h / 22), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(shine);
    }

    /** 하늘의 발자국 장식. */
    static void skyPaw(Graphics2D g0, double cx, double cy, int size, double turn) {
        Graphics2D g = (Graphics2D) g0.create();
        g.rotate(turn, cx, cy);
        Icons.draw(g, Icons.Kind.PAW, (int) (cx - size / 2.0), (int) (cy - size / 2.0), size, Theme.SKY_PAW);
        g.dispose();
    }

    static Color alpha(Color c, int a) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), a);
    }

    static Color mix(Color a, Color b, double k) {
        return new Color((int) (a.getRed() * (1 - k) + b.getRed() * k), (int) (a.getGreen() * (1 - k) + b.getGreen() * k),
                (int) (a.getBlue() * (1 - k) + b.getBlue() * k));
    }
}
