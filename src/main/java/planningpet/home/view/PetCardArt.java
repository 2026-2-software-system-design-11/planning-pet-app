package planningpet.home.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import planningpet.common.view.Images;
import planningpet.common.view.Theme;

/** 홈 펫 카드(D-59)의 그림 도구: 하늘과 풀밭 장면과 그것이 쓰는 색 섞기, 알파 바꾸기. 값은 디자인 시스템 10절 "홈"에 있다(docs/porting/home.md S3j). */
final class PetCardArt {
    private PetCardArt() {
    }

    /** 하늘 그라데이션, 구름 둘, 먼 언덕과 가까운 풀밭, 풀잎(홈 예시 펫 카드의 장면). */
    static void scene(Graphics2D g, Rectangle f, int ground) {
        scene(g, f, ground, Images.CLOUD);
    }

    /** 구름 그림을 받는다. 그림이 없으면(null) 구름 없이 그린다. 그림 파일을 못 읽은 때를 검사하려고 연다(home.md S3j). */
    static void scene(Graphics2D g, Rectangle f, int ground, BufferedImage cloud) {
        int x = f.x;
        int y = f.y;
        int w = f.width;
        int h = f.height;
        g.setPaint(new GradientPaint(0, y, Theme.SKY_TOP, 0, ground, Theme.SKY_BOTTOM));
        g.fillRect(x, y, w, h);
        if (cloud != null) {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            int cw = Math.max(60, w / 6);
            int ch = cw * cloud.getHeight() / cloud.getWidth();
            g.drawImage(cloud, x + w / 14, y + h / 12, cw, ch, null);
            // 둘째 구름은 글이 없는 곳(펫 머리 위쪽 오른편)에 작게 둔다. 오른쪽 열에 두면 이름과 레벨 글을 가린다.
            int cw2 = cw * 3 / 5;
            g.drawImage(cloud, x + (int) (w * 0.25), y + h / 5, cw2, cw2 * cloud.getHeight() / cloud.getWidth(), null);
        }
        // 먼 언덕: 화면 오른쪽으로 갈수록 낮아지는 흐린 초록 둔덕 둘.
        Path2D far = new Path2D.Double();
        far.moveTo(x, ground - h * 0.20);
        far.curveTo(x + w * 0.18, ground - h * 0.30, x + w * 0.40, ground - h * 0.22, x + w * 0.55, ground - h * 0.17);
        far.curveTo(x + w * 0.72, ground - h * 0.12, x + w * 0.86, ground - h * 0.21, x + w, ground - h * 0.16);
        far.lineTo(x + w, y + h);
        far.lineTo(x, y + h);
        far.closePath();
        g.setPaint(new GradientPaint(0, (float) (ground - h * 0.30), withAlpha(Theme.GRASS_FAR, 200), 0, ground,
                withAlpha(Theme.GRASS_FAR, 120)));
        g.fill(far);
        // 가까운 풀밭: 펫이 앉은 왼쪽이 조금 높고 오른쪽으로 내려가는 둔덕.
        Path2D near = new Path2D.Double();
        near.moveTo(x, ground - h * 0.06);
        near.curveTo(x + w * 0.25, ground - h * 0.10, x + w * 0.45, ground - h * 0.04, x + w * 0.62, ground + h * 0.01);
        near.curveTo(x + w * 0.80, ground + h * 0.05, x + w * 0.92, ground + h * 0.02, x + w, ground + h * 0.03);
        near.lineTo(x + w, y + h);
        near.lineTo(x, y + h);
        near.closePath();
        // 예시의 풀밭은 노란빛(#E0EC9C)이라 아래로 가며 먼 언덕 색을 조금만 섞는다.
        g.setPaint(new GradientPaint(0, (float) (ground - h * 0.10), Theme.GRASS, 0, y + h, mix(Theme.GRASS, Theme.GRASS_FAR, 0.35)));
        g.fill(near);
        // 풀잎: 왼쪽 풀밭 가장자리에 세 갈래 잎 몇 개.
        g.setColor(Theme.GRASS_DEEP);
        g.setStroke(new BasicStroke(Math.max(1.4f, h / 140f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        double[] tufts = {0.06, 0.33, 0.47};
        for (double tx : tufts) {
            double bx = x + w * tx;
            double by = ground - h * 0.03 + (tx > 0.4 ? h * 0.02 : 0);
            double s = h / 26.0;
            g.draw(new java.awt.geom.Line2D.Double(bx, by, bx - s * 0.5, by - s));
            g.draw(new java.awt.geom.Line2D.Double(bx + s * 0.25, by, bx + s * 0.3, by - s * 1.3));
            g.draw(new java.awt.geom.Line2D.Double(bx + s * 0.5, by, bx + s * 1.0, by - s * 0.9));
        }
    }

    static Color mix(Color a, Color b, double k) {
        return new Color((int) (a.getRed() * (1 - k) + b.getRed() * k), (int) (a.getGreen() * (1 - k) + b.getGreen() * k),
                (int) (a.getBlue() * (1 - k) + b.getBlue() * k));
    }

    static Color withAlpha(Color c, int a) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), a);
    }
}
