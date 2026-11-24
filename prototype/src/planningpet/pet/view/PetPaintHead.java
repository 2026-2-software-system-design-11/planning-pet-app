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
