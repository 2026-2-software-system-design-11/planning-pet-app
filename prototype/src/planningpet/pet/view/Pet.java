package planningpet.pet.view;

import planningpet.app.model.Store;
import planningpet.common.model.Rules;
import planningpet.common.view.Images;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;

/** 펫 임시 그림. 진짜 그림은 Q-28에서 정한다. 모습 단계(D-20)만큼 커진다. */
public final class Pet {
    private Pet() {
    }

    /** 고양이는 디자인 시스템의 그림(모습 단계별), 강아지는 임시 그림. size는 그림 상자의 한 변. */
    public static void drawAt(Graphics2D g, Store s, int x, int ground, int size) {
        int stage = Rules.stage(Rules.level(s.exp));
        java.awt.image.BufferedImage im = "CAT".equals(s.petKind) ? Images.cat(stage) : null;
        if (im != null) {
            Images.fit(g, im, x - size / 2, ground - size, size, size);
        } else {
            Graphics2D h = (Graphics2D) g.create();
            h.translate(x, ground);
            double k = size / 50.0;
            h.scale(k, k);
            draw(h, s.petKind, 0, 0, stage, false);
            h.dispose();
        }
    }

    /** (x, ground)는 발이 닿는 가운데 점이다. walking이면 다리를 벌린다. */
    public static void draw(Graphics2D g, String kind, int x, int ground, int stage, boolean walking) {
        int s = 14 + stage * 6;
        boolean cat = "CAT".equals(kind);
        Color body = cat ? new Color(0xF2A65A) : new Color(0xC8935A);
        Color dark = body.darker();
        g.setColor(dark);
        int legGap = walking ? s / 3 : s / 6;
        g.fillRect(x - legGap - 2, ground - s / 3, 4, s / 3);
        g.fillRect(x + legGap - 2, ground - s / 3, 4, s / 3);
        g.setColor(body);
        g.fillOval(x - s * 2 / 3, ground - s, s * 4 / 3, s * 2 / 3);
        int headX = x + s / 2;
        int headY = ground - s * 3 / 2;
        int head = s * 3 / 4;
        g.fillOval(headX - head / 2, headY, head, head);
        g.setColor(dark);
        if (cat) {
            g.fillPolygon(new Polygon(new int[] {headX - head / 2, headX - head / 4, headX - head / 2},
                    new int[] {headY + head / 3, headY, headY - head / 4}, 3));
            g.fillPolygon(new Polygon(new int[] {headX + head / 2, headX + head / 4, headX + head / 2},
                    new int[] {headY + head / 3, headY, headY - head / 4}, 3));
            g.drawLine(x - s * 2 / 3, ground - s * 2 / 3, x - s, ground - s * 4 / 3);
        } else {
            g.fillOval(headX - head / 2 - head / 6, headY + head / 6, head / 3, head / 2);
            g.fillOval(headX + head / 2 - head / 6, headY + head / 6, head / 3, head / 2);
        }
        g.setColor(Color.BLACK);
        g.fillOval(headX - head / 5, headY + head / 3, 3, 3);
        g.fillOval(headX + head / 6, headY + head / 3, 3, 3);
    }
}
