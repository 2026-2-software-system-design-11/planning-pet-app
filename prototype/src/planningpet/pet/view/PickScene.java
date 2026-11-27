package planningpet.pet.view;

import planningpet.common.view.Images;
import planningpet.common.view.Theme;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;

/**
 * 반려동물 고르기 창의 그림 도구(2026-11-23 고르기 예시, 디자인 시스템 10절 "고르기 창").
 * 하늘 언덕 장면을 도형으로 그린다. 도형이라 2배 화면에서도 흐려지지 않는다.
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

    /** 창 바탕: 하늘, 구름, 먼 산, 언덕, 풀 띠. 글과 카드가 놓이는 가운데는 비워 둔다. */
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

    static Color alpha(Color c, int a) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), a);
    }

    static Color mix(Color a, Color b, double k) {
        return new Color((int) (a.getRed() * (1 - k) + b.getRed() * k), (int) (a.getGreen() * (1 - k) + b.getGreen() * k),
                (int) (a.getBlue() * (1 - k) + b.getBlue() * k));
    }
}
