package planningpet.pet.view;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.QuadCurve2D;
import java.util.Set;
import planningpet.pet.model.PetKind;
import planningpet.pet.model.WearId;

/**
 * PetArt 펫 그림의 머리: 고양이와 강아지의 머리 모양, 귀, 웃는 얼굴(docs/porting/pet-art.md). 좌표는 머리 가운데가 (0, 0)이고
 * 얼굴 폭이 230이다.
 */
class PetPaintHead extends PetPaintWear {
    PetPaintHead(Graphics2D g, PetKind kind, Set<WearId> wear, double t) {
        super(g, kind, wear, t);
    }

    Shape headShape() {
        Path2D p = new Path2D.Double();
        if (!cat) {
            p.moveTo(0, -78);
            p.curveTo(64, -78, 94, -44, 94, -2);
            p.curveTo(94, 44, 62, 70, 0, 70);
            p.curveTo(-62, 70, -94, 44, -94, -2);
            p.curveTo(-94, -44, -64, -78, 0, -78);
            p.closePath();
            return p;
        }
        p.moveTo(0, -74);
        p.curveTo(62, -74, 98, -46, 98, -4);
        p.curveTo(98, 42, 66, 70, 0, 70);
        p.curveTo(-66, 70, -98, 42, -98, -4);
        p.curveTo(-98, -46, -62, -74, 0, -74);
        p.closePath();
        return p;
    }

    /**
     * 머리를 (x, y)에 rot도 기울여 그린다. 고양이는 귀 둘, 흰 머리와 위의 회색 얼룩(이마 가운데는 흰 무늬), 강아지는 크림색 머리와
     * 늘어진 귀다. 아래 그림자, 하이라이트, 웃는 얼굴, 입은 머리 장신구는 같이 그린다. 고양이 오른쪽 귀는 4.4초마다 한 번 까딱인다(t).
     */
    void head(double x, double y, double rot) {
        AffineTransform old = g.getTransform();
        push(x, y, rot);
        Shape head = headShape();
        if (cat) {
            ear(-1, 0);
            ear(1, Math.max(0, Math.sin(t * Math.PI / 2.2)) > 0.97 ? 8 : 0);
        }
        filled(head, fur);
        if (cat) {
            shadeIn(head, catCap(), CAT_GREY);
            shadeIn(head, ell(0, 76, 92, 22), CAT_WHITE_SHADE);
        } else {
            shadeIn(head, ell(0, 78, 92, 22), DOG_FUR_SHADE);
        }
        g.setColor(new Color(255, 255, 255, 150));
        g.fill(ell(-52, -46, 16, 8));
        redraw(head);
        if (!cat) {
            dogEar(-1);
            dogEar(1);
        }
        faceParts();
        headWear();
        g.setTransform(old);
    }

    /** 고양이 머리 위 회색 얼룩. 이마 가운데는 흰 무늬가 아래로 넓어지며 갈라진다. */
    private static Area catCap() {
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
        return grey;
    }

    /** 고양이 귀 하나. side는 왼쪽 -1, 오른쪽 1이다. 회색 귀에 분홍 안쪽이고 flick도 바깥으로 젖힌다. */
    void ear(int side, double flick) {
        AffineTransform old = g.getTransform();
        push(side * 66, -52, side * flick);
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

    /** 강아지의 늘어진 귀 하나. side는 왼쪽 -1, 오른쪽 1이다. 끝이 짙은 노란 귀가 바깥으로 조금 벌어져 2.4초 주기로 흔들린다(t). */
    void dogEar(int side) {
        AffineTransform old = g.getTransform();
        double sw = wave(t, 2.4, -3, 3, side > 0 ? 0.5 : 0);
        push(side * 74, -50, side * (6 + sw));
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

    /**
     * 웃는 얼굴 부품: 감고 웃는 눈, 분홍 볼, 코, 입. 고양이는 세모 코, 닫은 입, 수염이고 강아지는 둥근 코와 혀가 보이게 벌린 입이다.
     * 눈은 선 굵기의 0.85배, 닫은 입은 그 0.75배, 수염은 0.42배다.
     */
    void faceParts() {
        float w = line * 0.85f;
        stroke(new QuadCurve2D.Double(-54, 2, -40, -14, -26, 2), w, ink);
        stroke(new QuadCurve2D.Double(26, 2, 40, -14, 54, 2), w, ink);
        g.setColor(BLUSH);
        g.fill(ell(-62, 24, 15, 8));
        g.fill(ell(62, 24, 15, 8));
        if (!cat) {
            g.setColor(NOSE);
            g.fill(ell(0, 14, 11, 8));
            g.setColor(new Color(255, 255, 255, 170));
            g.fill(ell(-4, 11, 3.5, 2));
            openMouth(w);
            return;
        }
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

    /** 강아지의 벌린 입: 짙은 빨강 입 안 아래에 혀, 코에서 입으로 내려오는 선. */
    private void openMouth(float w) {
        double h = 12;
        Path2D m = new Path2D.Double();
        m.moveTo(-13, 26);
        m.quadTo(0, 30, 13, 26);
        m.quadTo(12, 26 + h * 1.3, 0, 26 + h * 1.3);
        m.quadTo(-12, 26 + h * 1.3, -13, 26);
        m.closePath();
        filled(m, MOUTH);
        shadeIn(m, ell(0, 26 + h * 1.2, 10, h * 0.5), TONGUE);
        redraw(m);
        stroke(new Line2D.Double(0, 20, 0, 27), w * 0.8f, ink);
    }
}
