package planningpet.pet.view;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.Rectangle2D;
import java.util.Set;
import planningpet.pet.model.PetKind;
import planningpet.pet.model.WearId;

/** PetArt 펫 그림의 장신구(D-51): 목걸이와 그 장식, 머리 장신구, 옷(docs/porting/pet-art.md 5절, 6절, 7절). 머리와 몸이 이것을 물려받아 입힌다. */
abstract class PetPaintWear extends PetPaintBase {
    PetPaintWear(Graphics2D g, PetKind kind, Set<WearId> wear, double t) {
        super(g, kind, wear, t);
    }

    /** 입은 목걸이 하나. 여럿이면 왕관, 별, 리본, 기본 목걸이 차례로 앞의 것이고 없으면 null이다. */
    WearId collar() {
        for (WearId c : new WearId[] {WearId.CROWN_NECKLACE, WearId.STAR_NECKLACE, WearId.RIBBON_NECKLACE,
                WearId.BASIC_NECKLACE}) {
            if (has(c)) {
                return c;
            }
        }
        return null;
    }

    /**
     * 목 둘레를 목 가운데 (x, y), 목 반폭 w로 그린다. 스카프는 목에 두른 빨간 천이 가슴 가운데로 뾰족하게 내려오고 오른쪽에 매듭과
     * 끝자락이 있다. 목걸이는 스카프 위에 띠가 아래로 둥글게 걸치고 그 가운데 아래에 장식이 달린다. 기본은 청록 띠에 금색 방울, 리본은
     * 분홍 띠에 연분홍 리본, 별은 파랑 띠에 금색 별, 왕관은 금색 띠에 금색 왕관이다. 망토는 그 위 목 가운데에 금색 고리를 단다.
     */
    void neckWear(double x, double y, double w) {
        AffineTransform old = g.getTransform();
        push(x, y, 0);
        if (has(WearId.SCARF)) {
            Path2D s = new Path2D.Double();
            s.moveTo(-w * 0.95, -6);
            s.quadTo(0, 6, w * 0.95, -6);
            s.lineTo(w * 0.2, 44);
            s.quadTo(0, 52, -w * 0.2, 44);
            s.closePath();
            filled(s, RED);
            shadeIn(s, ell(0, -6, w, 8), RED_SHADE);
            redraw(s);
            filled(ell(w * 0.85, -6, 11, 9), RED);
            Path2D end = new Path2D.Double();
            end.moveTo(w * 0.9, 0);
            end.lineTo(w * 1.15, 18);
            end.lineTo(w * 0.7, 10);
            end.closePath();
            filled(end, RED);
        }
        WearId collar = collar();
        if (collar != null) {
            Path2D b = new Path2D.Double();
            b.moveTo(-w, -8);
            b.quadTo(0, 10, w, -8);
            tube(b, 13, switch (collar) {
                case BASIC_NECKLACE -> TEAL;
                case RIBBON_NECKLACE -> PINK;
                case STAR_NECKLACE -> BLUE;
                default -> GOLD;
            });
            double py = 6;
            switch (collar) {
                case BASIC_NECKLACE -> {
                    filled(ell(0, py + 9, 11, 11), GOLD);
                    stroke(new Line2D.Double(-7, py + 9, 7, py + 9), line * 0.5f, ink);
                    g.setColor(new Color(255, 255, 255, 200));
                    g.fill(ell(-4, py + 5, 3, 2.4));
                }
                case RIBBON_NECKLACE -> bow(0, py + 4, 1.0, PINK_LIGHT);
                case STAR_NECKLACE -> filled(star(0, py + 12, 15, 7), GOLD);
                default -> filled(crown(0, py + 14, 30, 22), GOLD);
            }
        }
        if (has(WearId.CAPE)) {
            filled(ell(0, -2, 9, 9), GOLD);
        }
        g.setTransform(old);
    }

    /**
     * 머리 장신구를 머리 좌표에 입은 것 모두 차례로 그린다: 두 눈의 안경, 오른쪽 머리 위 꽃 핀, 왼쪽 머리 위 리본 핀, 빨간 띠를 두른
     * 밀짚모자, 꼭지가 달린 베레모, 빨간 보석이 박힌 왕관. 뒤의 것이 앞의 것을 덮는다.
     */
    void headWear() {
        AffineTransform old = g.getTransform();
        if (has(WearId.GLASSES)) {
            for (int s : new int[] {-1, 1}) {
                g.setColor(new Color(255, 255, 255, 60));
                g.fill(ell(s * 40, -3, 23, 21));
                stroke(ell(s * 40, -3, 23, 21), line * 0.8f, ink);
            }
            stroke(new QuadCurve2D.Double(-17, -6, 0, -14, 17, -6), line * 0.8f, ink);
        }
        if (has(WearId.FLOWER_PIN)) {
            push(cat ? 56 : 54, cat ? -56 : -58, 0);
            for (int i = 0; i < 5; i++) {
                double a = i * Math.PI * 2 / 5 - Math.PI / 2;
                filled(ell(Math.cos(a) * 12, Math.sin(a) * 12, 9, 9), PINK_LIGHT);
            }
            filled(ell(0, 0, 7, 7), GOLD);
            g.setTransform(old);
        }
        if (has(WearId.RIBBON_PIN)) {
            bow(cat ? -56 : -54, cat ? -58 : -60, 1.3, PINK);
        }
        if (has(WearId.STRAW_HAT)) {
            push(0, -62, -4);
            filled(ell(0, 0, 96, 22), STRAW);
            Path2D dome = new Path2D.Double();
            dome.moveTo(-50, 2);
            dome.curveTo(-50, -58, 50, -58, 50, 2);
            dome.quadTo(0, 12, -50, 2);
            dome.closePath();
            filled(dome, STRAW);
            shadeIn(dome, ell(26, -18, 22, 30), STRAW_SHADE);
            Path2D band = new Path2D.Double();
            band.moveTo(-49, -10);
            band.quadTo(0, 0, 49, -10);
            tube(band, 11, RED);
            redraw(dome);
            g.setTransform(old);
        }
        if (has(WearId.BERET)) {
            push(14, -70, 12);
            Path2D b = new Path2D.Double();
            b.moveTo(-74, 8);
            b.curveTo(-80, -30, 70, -40, 76, 4);
            b.quadTo(0, 22, -74, 8);
            b.closePath();
            filled(b, BERET);
            shadeIn(b, ell(0, 14, 80, 10), new Color(0, 0, 0, 30));
            redraw(b);
            stroke(new Line2D.Double(0, -26, 2, -38), line * 1.2f, ink);
            g.setTransform(old);
        }
        if (has(WearId.CROWN)) {
            push(22, -78, 12);
            Shape c = crown(0, 0, 70, 46);
            filled(c, GOLD);
            shadeIn(c, new Rectangle2D.Double(-40, 8, 80, 20), GOLD_SHADE);
            redraw(c);
            filled(ell(0, 6, 7, 7), RED);
            g.setTransform(old);
        }
    }

    /**
     * 몸 모양 안쪽에 목 아래 top부터 bottom까지 민트색 띠를 칠한 조끼. 아래 끝에 짙은 단과 선을 긋고 몸 외곽선을 다시 그은 뒤 가운데에
     * 금색 단추 둘을 단다. 프로토타입은 앉은 자세들에서만 단추를 다는데 앉기만 그리므로 늘 단다(docs/porting/pet-art.md 5절, 7절).
     */
    void vest(Shape body, double top, double bottom) {
        Shape old = g.getClip();
        g.clip(body);
        Rectangle r = new Area(body).getBounds();
        double low = Math.max(top, bottom);
        // 프로토타입처럼 경로에 담아 칠한다. 네모를 바로 칠하면 크기 7에서 앤티에일리어싱 픽셀이 달라진다(E-CLO-4).
        Path2D v = new Path2D.Double();
        v.append(new Rectangle2D.Double(r.x - 10, Math.min(top, bottom) - 10, r.width + 20, Math.abs(bottom - top) + 10),
                false);
        g.setColor(MINT);
        g.fill(v);
        g.setColor(MINT_SHADE);
        g.fill(new Rectangle2D.Double(r.x - 10, low - 8, r.width + 20, 8));
        stroke(new Line2D.Double(r.x - 10, low, r.x + r.width + 10, low), line * 0.7f, ink);
        g.setClip(old);
        redraw(body);
        filled(ell(160, 232, 5, 5), GOLD);
        filled(ell(160, 248, 5, 5), GOLD);
    }

    /** 앉은 자세의 망토 뒤판: 목 가운데 (x, y), 목 반폭 w에서 몸 뒤로 넓게 퍼지고 아랫단 안쪽이 빨갛다. */
    void capeBehind(double x, double y, double w) {
        Path2D c = new Path2D.Double();
        c.moveTo(x - w, y - 6);
        c.quadTo(x, y + 6, x + w, y - 6);
        c.curveTo(x + w * 1.7, y + 40, x + w * 1.9, y + 90, x + w * 1.75, y + 112);
        c.quadTo(x, y + 124, x - w * 1.75, y + 112);
        c.curveTo(x - w * 1.9, y + 90, x - w * 1.7, y + 40, x - w, y - 6);
        c.closePath();
        filled(c, CAPE);
        shadeIn(c, ell(x, y + 120, w * 1.5, 18), CAPE_INNER);
        redraw(c);
    }

    /** 가운데가 (x, y)이고 k배인 리본: 양쪽 고리와 가운데 매듭을 c로 칠한다. */
    void bow(double x, double y, double k, Color c) {
        AffineTransform old = g.getTransform();
        push(x, y, 0);
        g.scale(k, k);
        Path2D l = new Path2D.Double();
        l.moveTo(0, 0);
        l.curveTo(-8, -16, -24, -14, -22, 0);
        l.curveTo(-24, 14, -8, 16, 0, 0);
        l.closePath();
        Path2D r = new Path2D.Double();
        r.moveTo(0, 0);
        r.curveTo(8, -16, 24, -14, 22, 0);
        r.curveTo(24, 14, 8, 16, 0, 0);
        r.closePath();
        filled(l, c);
        filled(r, c);
        filled(ell(0, 0, 6, 6), c);
        g.setTransform(old);
    }

    /** 가운데가 (cx, cy)이고 바깥 반지름 ro, 안 반지름 ri인 다섯 끝 별. 첫 끝이 위다. */
    static Shape star(double cx, double cy, double ro, double ri) {
        Path2D p = new Path2D.Double();
        for (int i = 0; i < 10; i++) {
            double a = i * Math.PI / 5 - Math.PI / 2;
            double r = i % 2 == 0 ? ro : ri;
            if (i == 0) {
                p.moveTo(cx + Math.cos(a) * r, cy + Math.sin(a) * r);
            } else {
                p.lineTo(cx + Math.cos(a) * r, cy + Math.sin(a) * r);
            }
        }
        p.closePath();
        return p;
    }

    /** 가운데가 (cx, cy)이고 폭 w, 높이 h인 뾰족한 끝 셋의 왕관. 아래 변은 cy + h/2다. */
    static Shape crown(double cx, double cy, double w, double h) {
        Path2D p = new Path2D.Double();
        double x0 = cx - w / 2;
        double y0 = cy - h / 2;
        p.moveTo(x0, cy + h / 2);
        p.lineTo(x0, y0 + h * 0.2);
        p.lineTo(x0 + w * 0.27, y0 + h * 0.55);
        p.lineTo(cx, y0);
        p.lineTo(x0 + w * 0.73, y0 + h * 0.55);
        p.lineTo(x0 + w, y0 + h * 0.2);
        p.lineTo(x0 + w, cy + h / 2);
        p.closePath();
        return p;
    }
}
