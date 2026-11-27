package planningpet.pet.view;

import planningpet.common.view.Icons;
import planningpet.common.view.Images;
import planningpet.common.view.Theme;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;

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

    /** 창 바탕: 하늘, 구름, 먼 산, 언덕, 바닥 꽃밭, 반짝이 별. 글과 카드가 놓이는 가운데는 비워 둔다. */
    static void backdrop(Graphics2D g, int w, int h) {
        hints(g);
        g.setPaint(new GradientPaint(0, 0, Theme.SKY_TOP, 0, h * 0.62f, Theme.SKY_BOTTOM));
        g.fillRect(0, 0, w, h);
        cloud(g, w * 0.04, h * 0.10, w * 0.15);
        cloud(g, w * 0.17, h * 0.21, w * 0.08);
        cloud(g, w * 0.80, h * 0.08, w * 0.14);
        cloud(g, w * 0.86, h * 0.24, w * 0.10);
        cloud(g, w * 0.70, h * 0.06, w * 0.07);
        cloud(g, w * 0.88, h * 0.42, w * 0.09);
        cloud(g, w * 0.02, h * 0.36, w * 0.09);
        // 먼 산: 양옆이 높고 가운데로 낮아지는 하늘빛 능선.
        Path2D far = new Path2D.Double();
        far.moveTo(0, h * 0.58);
        far.curveTo(w * 0.06, h * 0.50, w * 0.12, h * 0.52, w * 0.18, h * 0.57);
        far.curveTo(w * 0.30, h * 0.66, w * 0.70, h * 0.66, w * 0.82, h * 0.57);
        far.curveTo(w * 0.88, h * 0.51, w * 0.94, h * 0.50, w, h * 0.56);
        far.lineTo(w, h);
        far.lineTo(0, h);
        far.closePath();
        g.setPaint(new GradientPaint(0, h * 0.50f, Theme.MOUNTAIN, 0, h * 0.72f, Theme.SKY_BOTTOM));
        g.fill(far);
        // 언덕: 양옆에서 올라오는 연두 둔덕.
        Path2D hill = new Path2D.Double();
        hill.moveTo(0, h * 0.66);
        hill.curveTo(w * 0.10, h * 0.62, w * 0.20, h * 0.68, w * 0.30, h * 0.76);
        hill.curveTo(w * 0.42, h * 0.84, w * 0.58, h * 0.84, w * 0.70, h * 0.76);
        hill.curveTo(w * 0.80, h * 0.68, w * 0.90, h * 0.62, w, h * 0.65);
        hill.lineTo(w, h);
        hill.lineTo(0, h);
        hill.closePath();
        g.setPaint(new GradientPaint(0, h * 0.62f, alpha(Theme.GRASS_FAR, 230), 0, h, alpha(Theme.GRASS, 255)));
        g.fill(hill);
        // 바닥 풀 띠: 가운데는 얇고 양 아래 모서리는 두껍다.
        Path2D near = new Path2D.Double();
        near.moveTo(0, h * 0.80);
        near.curveTo(w * 0.12, h * 0.78, w * 0.22, h * 0.88, w * 0.34, h * 0.95);
        near.curveTo(w * 0.45, h * 0.985, w * 0.55, h * 0.985, w * 0.66, h * 0.95);
        near.curveTo(w * 0.78, h * 0.88, w * 0.88, h * 0.78, w, h * 0.79);
        near.lineTo(w, h);
        near.lineTo(0, h);
        near.closePath();
        g.setPaint(new GradientPaint(0, h * 0.78f, Theme.GRASS, 0, h, mix(Theme.GRASS, Theme.GRASS_DEEP, 0.5)));
        g.fill(near);
        double u = h / 600.0;
        // 양 아래 모서리 덤불과 꽃. 바깥 모서리가 두껍고 가운데로 갈수록 작아진다.
        for (int side = 0; side < 2; side++) {
            boolean right = side == 1;
            double x0 = right ? w : 0;
            double dir = right ? -1 : 1;
            bush(g, x0 + dir * 20 * u, h - 70 * u, 74 * u);
            bush(g, x0 + dir * 62 * u, h - 28 * u, 80 * u);
            bush(g, x0 + dir * 128 * u, h - 6 * u, 60 * u);
            daisy(g, x0 + dir * 24 * u, h - 118 * u, 13 * u, -0.2 * dir);
            daisy(g, x0 + dir * 58 * u, h - 86 * u, 12 * u, 0.3 * dir);
            daisy(g, x0 + dir * 22 * u, h - 52 * u, 14 * u, 0.1);
            daisy(g, x0 + dir * 92 * u, h - 46 * u, 11 * u, -0.4);
            daisy(g, x0 + dir * 60 * u, h - 16 * u, 12 * u, 0.2);
            daisy(g, x0 + dir * 140 * u, h - 30 * u, 9 * u, 0.5);
            daisy(g, x0 + dir * 196 * u, h - 12 * u, 8 * u, -0.2);
            bud(g, x0 + dir * 96 * u, h - 96 * u, 6 * u);
            bud(g, x0 + dir * 118 * u, h - 66 * u, 5 * u);
            bud(g, x0 + dir * 170 * u, h - 20 * u, 5 * u);
            bud(g, x0 + dir * 236 * u, h - 8 * u, 4 * u);
        }
        star(g, w * 0.14, h * 0.75, 12 * u);
        star(g, w * 0.86, h * 0.74, 12 * u);
    }

    /** 구름: (x, y)가 왼쪽 위, 폭 cw. 그림이 있으면 예시 구름 그림, 없으면 도형. */
    private static void cloud(Graphics2D g, double x, double y, double cw) {
        BufferedImage im = Images.CLOUD;
        if (im != null) {
            int ch = (int) (cw * im.getHeight() / im.getWidth());
            g.drawImage(im, (int) x, (int) y, (int) cw, ch, null);
            return;
        }
        g.setColor(new Color(255, 255, 255, 220));
        g.fill(new Ellipse2D.Double(x, y + cw * 0.15, cw, cw * 0.3));
        g.fill(new Ellipse2D.Double(x + cw * 0.25, y, cw * 0.45, cw * 0.4));
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
    private static void bud(Graphics2D g, double cx, double cy, double r) {
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
