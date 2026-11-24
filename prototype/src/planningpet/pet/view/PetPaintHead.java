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


/** PetArt 펫 그림의 머리: 머리 모양, 귀, 얼굴, 머리에 붙는 장신구. */
abstract class PetPaintHead extends PetPaintWear {
    PetPaintHead(Graphics2D g, PetKind kind, PetPose pose, Set<String> wear, double t) {
        super(g, kind, pose, wear, t);
    }

    // ---------- 머리 ----------
    Shape headShape() {
        Path2D p = new Path2D.Double();
        if (cat) {
            p.moveTo(0, -74);
            p.curveTo(62, -74, 98, -46, 98, -4);
            p.curveTo(98, 42, 66, 70, 0, 70);
            p.curveTo(-66, 70, -98, 42, -98, -4);
            p.curveTo(-98, -46, -62, -74, 0, -74);
        } else {
            p.moveTo(0, -78);
            p.curveTo(64, -78, 94, -44, 94, -2);
            p.curveTo(94, 44, 62, 70, 0, 70);
            p.curveTo(-62, 70, -94, 44, -94, -2);
            p.curveTo(-94, -44, -64, -78, 0, -78);
        }
        p.closePath();
        return p;
    }

    /** (x, y)는 머리 가운데. withAccessories가 참이면 모자, 핀, 안경도 그린다. */
    void head(double x, double y, double rot, Face face, boolean withAccessories) {
        AffineTransform old = g.getTransform();
        push(x, y, rot, 1, 1);
        Shape head = headShape();
        if (cat) {
            double flick = Math.max(0, Math.sin(t * Math.PI / 2.2)) > 0.97 ? 8 : 0;
            ear(-1, 0);
            ear(1, flick);
        }
        filled(head, fur);
        if (cat) {
            // 머리 위 회색 얼룩. 이마 가운데는 흰 무늬가 아래로 넓어지며 갈라진다.
            Path2D cap = new Path2D.Double();
            cap.moveTo(-110, -100);
            cap.lineTo(110, -100);
            cap.lineTo(110, 14);
            cap.curveTo(80, 0, 50, -22, 0, -26);
            cap.curveTo(-50, -22, -80, 0, -110, 14);
            cap.closePath();
            Area grey = new Area(cap);
            Path2D blaze = new Path2D.Double();
            blaze.moveTo(4, -80);
            blaze.curveTo(10, -50, 30, -30, 60, -14);
            blaze.lineTo(-48, -14);
            blaze.curveTo(-20, -30, -4, -50, 4, -80);
            blaze.closePath();
            grey.subtract(new Area(blaze));
            Area greyLow = new Area(grey);
            greyLow.intersect(new Area(new java.awt.geom.Rectangle2D.Double(-110, -22, 220, 20)));
            Path2D edge = new Path2D.Double();
            edge.append(grey, false);
            shadeIn(head, grey, CAT_GREY);
            shadeIn(head, ell(0, 76, 92, 22), CAT_WHITE_SHADE);
        } else {
            shadeIn(head, ell(0, 78, 92, 22), DOG_FUR_SHADE);
        }
        // 하이라이트
        g.setColor(new Color(255, 255, 255, 150));
        g.fill(ell(-52, -46, 16, 8));
        redraw(head);
        if (!cat) {
            dogEar(-1);
            dogEar(1);
        }
        faceParts(face);
        if (withAccessories) {
            headWear();
        }
        g.setTransform(old);
    }

    void ear(int side, double flick) {
        AffineTransform old = g.getTransform();
        push(side * 66, -52, side * flick, 1, 1);
        Path2D p = new Path2D.Double();
        p.moveTo(side * -40, 4);
        p.curveTo(side * -30, -34, side * -6, -58, side * 4, -60);
        p.curveTo(side * 16, -56, side * 26, -24, side * 30, 14);
        p.closePath();
        filled(p, CAT_GREY);
        Path2D in = new Path2D.Double();
        in.moveTo(side * -22, 0);
        in.curveTo(side * -14, -26, side * -2, -42, side * 4, -42);
        in.curveTo(side * 12, -36, side * 16, -14, side * 16, 8);
        in.closePath();
        g.setColor(CAT_EAR);
        g.fill(in);
        g.setTransform(old);
    }

    void dogEar(int side) {
        AffineTransform old = g.getTransform();
        double sw = wave(t, 2.4, -3, 3, side > 0 ? 0.5 : 0);
        push(side * 74, -50, side * (6 + sw), 1, 1);
        Path2D p = new Path2D.Double();
        p.moveTo(side * -18, 2);
        p.curveTo(side * 20, -14, side * 46, 26, side * 42, 76);
        p.curveTo(side * 40, 104, side * 12, 112, side * -2, 96);
        p.curveTo(side * -10, 86, side * -6, 70, side * -12, 56);
        p.curveTo(side * -20, 38, side * -26, 18, side * -18, 2);
        p.closePath();
        filled(p, DOG_EAR);
        shadeIn(p, ell(side * 4, 92, 26, 18), DOG_EAR_SHADE);
        redraw(p);
        g.setTransform(old);
    }

    void faceParts(Face face) {
        float w = line * 0.85f;
        // 눈
        if (face == Face.SLEEP) {
            stroke(new QuadCurve2D.Double(-54, -2, -40, 8, -26, -2), w, ink);
            stroke(new QuadCurve2D.Double(26, -2, 40, 8, 54, -2), w, ink);
        } else if (face == Face.STRETCH) {
            // 꼭 감은 눈(> <)
            Path2D l = new Path2D.Double();
            l.moveTo(-54, -12);
            l.lineTo(-30, -3);
            l.lineTo(-54, 6);
            Path2D r = new Path2D.Double();
            r.moveTo(54, -12);
            r.lineTo(30, -3);
            r.lineTo(54, 6);
            stroke(l, w, ink);
            stroke(r, w, ink);
        } else {
            stroke(new QuadCurve2D.Double(-54, 2, -40, -14, -26, 2), w, ink);
            stroke(new QuadCurve2D.Double(26, 2, 40, -14, 54, 2), w, ink);
        }
        // 볼
        g.setColor(BLUSH);
        g.fill(ell(-62, 24, 15, 8));
        g.fill(ell(62, 24, 15, 8));
        // 코
        if (cat) {
            Path2D n = new Path2D.Double();
            n.moveTo(-7, 12);
            n.quadTo(0, 9, 7, 12);
            n.quadTo(2, 20, 0, 20);
            n.quadTo(-2, 20, -7, 12);
            n.closePath();
            g.setColor(NOSE);
            g.fill(n);
        } else {
            g.setColor(NOSE);
            g.fill(ell(0, 14, 11, 8));
            g.setColor(new Color(255, 255, 255, 170));
            g.fill(ell(-4, 11, 3.5, 2));
        }
        // 입
        boolean open = face == Face.SING || face == Face.STRETCH || (!cat && face != Face.SLEEP)
                || (face == Face.EAT && Math.sin(t * Math.PI / 0.35) > 0);
        if (open) {
            double h = face == Face.SING ? wave(t, 0.5, 12, 18, 0) : face == Face.STRETCH ? 17 : 12;
            Path2D m = new Path2D.Double();
            m.moveTo(-13, 26);
            m.quadTo(0, 30, 13, 26);
            m.quadTo(12, 26 + h * 1.3, 0, 26 + h * 1.3);
            m.quadTo(-12, 26 + h * 1.3, -13, 26);
            m.closePath();
            filled(m, MOUTH);
            shadeIn(m, ell(0, 26 + h * 1.2, 10, h * 0.5), TONGUE);
            redraw(m);
            stroke(new java.awt.geom.Line2D.Double(0, 20, 0, 27), w * 0.8f, ink);
        } else {
            stroke(new java.awt.geom.Line2D.Double(0, 20, 0, 25), w * 0.75f, ink);
            stroke(new QuadCurve2D.Double(-14, 24, -7, 33, 0, 25), w * 0.75f, ink);
            stroke(new QuadCurve2D.Double(0, 25, 7, 33, 14, 24), w * 0.75f, ink);
        }
        // 고양이 수염
        if (cat) {
            float ww = line * 0.42f;
            for (int s : new int[] {-1, 1}) {
                stroke(new java.awt.geom.Line2D.Double(s * 82, 10, s * 124, 2), ww, ink);
                stroke(new java.awt.geom.Line2D.Double(s * 82, 22, s * 120, 26), ww, ink);
            }
        }
    }
}
