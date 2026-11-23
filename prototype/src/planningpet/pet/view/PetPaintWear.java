package planningpet.pet.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Set;


/** PetArt 펫 그림의 장신구(D-51): 목 둘레, 머리 위, 리본, 별, 왕관, 조끼, 망토, 장신구 아이콘. */
abstract class PetPaintWear extends PetPaintBase {
    PetPaintWear(Graphics2D g, PetKind kind, PetPose pose, Set<String> wear, double t) {
        super(g, kind, pose, wear, t);
    }

    // ---------- 장신구 (D-51) ----------
    /** 목 둘레. (x, y)는 목 가운데, w는 목 반폭. */
    void neckWear(double x, double y, double rot, double w) {
        AffineTransform old = g.getTransform();
        push(x, y, rot, 1, 1);
        if (has("스카프")) {
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
            Path2D tail1 = new Path2D.Double();
            tail1.moveTo(w * 0.9, 0);
            tail1.lineTo(w * 1.15, 18);
            tail1.lineTo(w * 0.7, 10);
            tail1.closePath();
            filled(tail1, RED);
        }
        String collar = has("왕관 목걸이") ? "왕관" : has("별 목걸이") ? "별" : has("리본 목걸이") ? "리본"
                : has("기본 목걸이") ? "기본" : null;
        if (collar != null) {
            Color band = "기본".equals(collar) ? TEAL : "리본".equals(collar) ? PINK : "별".equals(collar) ? BLUE : GOLD;
            Path2D b = new Path2D.Double();
            b.moveTo(-w, -8);
            b.quadTo(0, 10, w, -8);
            stroke(b, 13 + line * 2, ink);
            stroke(b, 13, band);
            double py = 6;
            switch (collar) {
                case "기본":
                    filled(ell(0, py + 9, 11, 11), GOLD);
                    stroke(new java.awt.geom.Line2D.Double(-7, py + 9, 7, py + 9), line * 0.5f, ink);
                    g.setColor(new Color(255, 255, 255, 200));
                    g.fill(ell(-4, py + 5, 3, 2.4));
                    break;
                case "리본":
                    bow(0, py + 4, 1.0, PINK_LIGHT);
                    break;
                case "별":
                    filled(star(0, py + 12, 15, 7), GOLD);
                    break;
                default:
                    filled(crown(0, py + 14, 30, 22), GOLD);
                    break;
            }
        }
        if (has("망토")) {
            filled(ell(0, -2, 9, 9), GOLD);
        }
        g.setTransform(old);
    }

    void headWear() {
        if (has("안경")) {
            for (int s : new int[] {-1, 1}) {
                g.setColor(new Color(255, 255, 255, 60));
                g.fill(ell(s * 40, -3, 23, 21));
                stroke(ell(s * 40, -3, 23, 21), line * 0.8f, ink);
            }
            stroke(new QuadCurve2D.Double(-17, -6, 0, -14, 17, -6), line * 0.8f, ink);
        }
        if (has("꽃 핀")) {
            AffineTransform old = g.getTransform();
            push(cat ? 56 : 54, cat ? -56 : -58, 0, 1, 1);
            for (int i = 0; i < 5; i++) {
                double a = i * Math.PI * 2 / 5 - Math.PI / 2;
                filled(ell(Math.cos(a) * 12, Math.sin(a) * 12, 9, 9), PINK_LIGHT);
            }
            filled(ell(0, 0, 7, 7), GOLD);
            g.setTransform(old);
        }
        if (has("리본 핀")) {
            bow(cat ? -56 : -54, cat ? -58 : -60, 1.3, PINK);
        }
        if (has("밀짚모자")) {
            AffineTransform old = g.getTransform();
            push(0, -62, -4, 1, 1);
            Shape brim = ell(0, 0, 96, 22);
            filled(brim, STRAW);
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
            stroke(band, 11 + line * 2, ink);
            stroke(band, 11, RED);
            redraw(dome);
            g.setTransform(old);
        }
        if (has("베레모")) {
            AffineTransform old = g.getTransform();
            push(14, -70, 12, 1, 1);
            Path2D b = new Path2D.Double();
            b.moveTo(-74, 8);
            b.curveTo(-80, -30, 70, -40, 76, 4);
            b.quadTo(0, 22, -74, 8);
            b.closePath();
            filled(b, BERET);
            shadeIn(b, ell(0, 14, 80, 10), alpha(Color.BLACK, 30));
            redraw(b);
            stroke(new java.awt.geom.Line2D.Double(0, -26, 2, -38), line * 1.2f, ink);
            g.setTransform(old);
        }
        if (has("왕관")) {
            AffineTransform old = g.getTransform();
            push(22, -78, 12, 1, 1);
            Shape c = crown(0, 0, 70, 46);
            filled(c, GOLD);
            shadeIn(c, new java.awt.geom.Rectangle2D.Double(-40, 8, 80, 20), GOLD_SHADE);
            redraw(c);
            filled(ell(0, 6, 7, 7), RED);
            g.setTransform(old);
        }
    }

    void bow(double x, double y, double k, Color c) {
        AffineTransform old = g.getTransform();
        push(x, y, 0, k, k);
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

    Shape star(double cx, double cy, double ro, double ri) {
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
