package planningpet.pet.view;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.QuadCurve2D;
import planningpet.pet.model.PetKind;

/**
 * PetArt 펫 그림의 몸: 앉은 자세(D-50의 처음 자세)의 앞발과 꼬리(docs/porting/pet-art.md 4절). 좌표는 320x320 상자이고 발이 닿는
 * 바닥은 y=300, 가운데는 x=160이다.
 */
final class PetPaintBody extends PetPaintHead {
    PetPaintBody(Graphics2D g, PetKind kind, double t) {
        super(g, kind, t);
    }

    /** 앞발 하나를 (x, y)에 그린다: 털색 타원에 발가락 선 둘. */
    void paw(double x, double y) {
        AffineTransform old = g.getTransform();
        g.translate(x, y);
        filled(ell(0, -4, 21, 13), fur);
        stroke(new QuadCurve2D.Double(-6, -9, -7, -3, -6, 2), line * 0.55f, ink);
        stroke(new QuadCurve2D.Double(6, -9, 7, -3, 6, 2), line * 0.55f, ink);
        g.setTransform(old);
    }

    /** 앉은 꼬리. 고양이는 왼쪽 뒤로 감아 올린 회색 꼬리가 2초, 강아지는 오른쪽 뒤의 짧은 꼬리가 0.5초 주기로 흔들린다(t). */
    void tail() {
        AffineTransform old = g.getTransform();
        Path2D p = new Path2D.Double();
        p.moveTo(0, 0);
        if (cat) {
            push(98, 286, wave(t, 2.0, -7, 9, 0));
            p.curveTo(-44, 6, -66, -22, -58, -60);
            p.quadTo(-54, -78, -40, -84);
            tube(p, 26, CAT_GREY);
        } else {
            push(222, 272, wave(t, 0.5, -14, 18, 0));
            p.quadTo(26, -10, 30, -44);
            tube(p, 24, DOG_FUR);
            filled(ell(30, -48, 14, 13), DOG_FUR);
        }
        g.setTransform(old);
    }
}
