package planningpet.pet.view;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.QuadCurve2D;
import java.util.Set;
import planningpet.pet.model.PetKind;
import planningpet.pet.model.WearId;

/**
 * PetArt 펫 그림의 몸: 앉은 자세(D-50의 처음 자세)의 몸, 앞발, 꼬리와 그 위의 머리(docs/porting/pet-art.md 4절). 좌표는 320x320
 * 상자이고 발이 닿는 바닥은 y=300, 가운데는 x=160이다.
 */
final class PetPaintBody extends PetPaintHead {
    PetPaintBody(Graphics2D g, PetKind kind, Set<WearId> wear, double t) {
        super(g, kind, wear, t);
    }

    /**
     * 앉은 펫을 그린다. 몸은 바닥을 기준으로 2.6초 주기로 숨쉬며 위아래로 조금 부풀고(조끼도 함께), 목 둘레와 머리는 몸을 따라 오르내린다. 머리는
     * 3.4초 주기로 좌우로 기운다(t). 망토 뒤판은 꼬리보다 먼저 몸 뒤에 그려 숨쉬지 않는다.
     */
    void sit() {
        AffineTransform base = g.getTransform();
        double breathe = wave(t, 2.6, 1.0, 1.022, 0);
        double headRot = wave(t, 3.4, -2.5, 2.5, 0.2);
        Shape body = sitBody();
        if (has(WearId.CAPE)) {
            capeBehind(160, 200, 62);
        }
        tail();
        g.translate(160, 300);
        g.scale(1, breathe);
        g.translate(-160, -300);
        filled(body, fur);
        shadeIn(body, ell(160, 200, 70, 26), furShade);
        shadeIn(body, ell(256, 262, 34, 90), furShade);
        if (cat) {
            shadeIn(body, ell(90, 262, 42, 38), CAT_GREY);
            shadeIn(body, ell(90, 294, 42, 12), CAT_GREY_SHADE);
        } else {
            shadeIn(body, ell(160, 300, 90, 12), DOG_FUR_SHADE);
        }
        redraw(body);
        if (has(WearId.VEST)) {
            vest(body, 200, 258);
        }
        // 뒷다리 허벅지 선
        stroke(new QuadCurve2D.Double(92, 268, 108, 250, 126, 262), line * 0.7f, ink);
        stroke(new QuadCurve2D.Double(228, 268, 212, 250, 194, 262), line * 0.7f, ink);
        paw(138, 294);
        paw(182, 294);
        g.setTransform(base);
        neckWear(160, 202 + (breathe - 1) * -110, 58);
        head(160, 134 + (breathe - 1) * -110, headRot);
    }

    static Shape sitBody() {
        Path2D p = new Path2D.Double();
        p.moveTo(94, 190);
        p.curveTo(70, 216, 62, 264, 74, 292);
        p.quadTo(80, 302, 98, 302);
        p.lineTo(222, 302);
        p.quadTo(240, 302, 246, 292);
        p.curveTo(258, 264, 250, 216, 226, 190);
        p.closePath();
        return p;
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

    /** 노래하는 펫 오른쪽 위의 음표 둘. 2초 주기로 흔들리며 오르고, 반 주기 어긋나 하나가 나타날 때 하나가 사라진다(t). */
    void notes() {
        for (int i = 0; i < 2; i++) {
            double p = (t / 2.0 + i * 0.5) % 1.0;
            double x = 252 + i * 24 + Math.sin(p * Math.PI * 2) * 6;
            double y = 86 - p * 56;
            g.setColor(alpha(NOTE, (int) (255 * Math.min(1, Math.min(p * 4, (1 - p) * 3)))));
            g.fill(ell(x, y, 9, 7));
            g.setStroke(stroke(4f));
            g.draw(new Line2D.Double(x + 8, y, x + 8, y - 30));
            g.draw(new QuadCurve2D.Double(x + 8, y - 30, x + 22, y - 24, x + 18, y - 12));
        }
    }
}
