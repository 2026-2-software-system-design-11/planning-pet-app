package planningpet.pet.view;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.QuadCurve2D;

/** PetArt 펫 그림의 머리: 고양이 머리 모양, 귀, 웃는 얼굴(docs/porting/pet-art.md). 좌표는 머리 가운데가 (0, 0)이고 얼굴 폭이 230이다. */
class PetPaintHead extends PetPaintBase {
    PetPaintHead(Graphics2D g) {
        super(g);
    }

    static Shape headShape() {
        Path2D p = new Path2D.Double();
        p.moveTo(0, -74);
        p.curveTo(62, -74, 98, -46, 98, -4);
        p.curveTo(98, 42, 66, 70, 0, 70);
        p.curveTo(-66, 70, -98, 42, -98, -4);
        p.curveTo(-98, -46, -62, -74, 0, -74);
        p.closePath();
        return p;
    }

    /** 머리를 (0, 0)에 그린다: 귀 둘, 흰 머리와 위의 회색 얼룩(이마 가운데는 흰 무늬), 아래 그림자, 하이라이트, 웃는 얼굴. */
    void head() {
        Shape head = headShape();
        ear(-1);
        ear(1);
        filled(head, CAT_WHITE);
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
        shadeIn(head, grey, CAT_GREY);
        shadeIn(head, ell(0, 76, 92, 22), CAT_WHITE_SHADE);
        g.setColor(new Color(255, 255, 255, 150));
        g.fill(ell(-52, -46, 16, 8));
        redraw(head);
        faceParts();
    }

    /** 고양이 귀 하나. side는 왼쪽 -1, 오른쪽 1이다. 회색 귀에 분홍 안쪽이다. */
    void ear(int side) {
        AffineTransform old = g.getTransform();
        g.translate(side * 66, -52);
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

    /** 웃는 얼굴 부품: 감고 웃는 눈, 분홍 볼, 코, 닫은 입, 수염. 눈은 선 굵기의 0.85배, 입은 그 0.75배, 수염은 0.42배다. */
    void faceParts() {
        float w = line * 0.85f;
        stroke(new QuadCurve2D.Double(-54, 2, -40, -14, -26, 2), w, ink);
        stroke(new QuadCurve2D.Double(26, 2, 40, -14, 54, 2), w, ink);
        g.setColor(BLUSH);
        g.fill(ell(-62, 24, 15, 8));
        g.fill(ell(62, 24, 15, 8));
        Path2D n = new Path2D.Double();
        n.moveTo(-7, 12);
        n.quadTo(0, 9, 7, 12);
        n.quadTo(2, 20, 0, 20);
        n.quadTo(-2, 20, -7, 12);
        n.closePath();
        g.setColor(NOSE);
        g.fill(n);
        stroke(new Line2D.Double(0, 20, 0, 25), w * 0.75f, ink);
        stroke(new QuadCurve2D.Double(-14, 24, -7, 33, 0, 25), w * 0.75f, ink);
        stroke(new QuadCurve2D.Double(0, 25, 7, 33, 14, 24), w * 0.75f, ink);
        float ww = line * 0.42f;
        for (int s : new int[] {-1, 1}) {
            stroke(new Line2D.Double(s * 82, 10, s * 124, 2), ww, ink);
            stroke(new Line2D.Double(s * 82, 22, s * 120, 26), ww, ink);
        }
    }
}
