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


/** PetArt 펫 그림의 몸과 자세(D-50): 앉기, 밥 먹기, 노래, 자기, 기지개와 소품(밥그릇, 음표, Zzz). */
final class PetPaintBody extends PetPaintHead {
    PetPaintBody(Graphics2D g, PetKind kind, PetPose pose, Set<String> wear, double t) {
        super(g, kind, pose, wear, t);
    }

    // ---------- 전체 ----------
    void paint() {
        switch (pose) {
            case SLEEP:
                sleep();
                break;
            case STRETCH:
                stretch();
                break;
            default:
                sitLike();
                break;
        }
    }

    // ---------- 앉은 자세들(앉기, 밥 먹기, 노래) ----------
    void sitLike() {
        AffineTransform base = g.getTransform();
        double breathe = wave(t, 2.6, 1.0, 1.022, 0);
        double headRot = wave(t, 3.4, -2.5, 2.5, 0.2);
        double headY = 0;
        Face face = Face.HAPPY;
        if (pose == PetPose.EAT) {
            headY = 14 + wave(t, 0.7, 0, 7, 0);
            headRot = 0;
            face = Face.EAT;
        } else if (pose == PetPose.SING) {
            headRot = wave(t, 1.6, -9, 5, 0);
            headY = -4;
            face = Face.SING;
        }
        Shape body = sitBody();
        if (has("망토")) {
            capeBehind(160, 200, 62);
        }
        tail(false);
        // 숨쉬기: 몸은 바닥 기준으로 위아래만 조금 부푼다.
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
        if (has("조끼")) {
            vest(body, 200, 258);
        }
        // 뒷다리 허벅지 선
        stroke(new QuadCurve2D.Double(92, 268, 108, 250, 126, 262), line * 0.7f, ink);
        stroke(new QuadCurve2D.Double(228, 268, 212, 250, 194, 262), line * 0.7f, ink);
        // 앞발
        boolean raise = pose == PetPose.SING;
        paw(138, 294, 0);
        if (raise) {
            double lift = wave(t, 0.8, 0, 8, 0);
            paw(206, 230 - lift, -30);
        } else {
            paw(182, 294, 0);
        }
        g.setTransform(base);
        double neckY = 202 + (breathe - 1) * -110;
        neckWear(160, neckY + headY * 0.3, 0, 58);
        if (pose == PetPose.EAT) {
            bowl();
        }
        head(160, 134 + headY + (breathe - 1) * -110, headRot, face, true);
        if (pose == PetPose.SING) {
            notes();
        }
    }

    Shape sitBody() {
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


    void paw(double x, double y, double rot) {
        AffineTransform old = g.getTransform();
        push(x, y, rot, 1, 1);
        Shape s = ell(0, -4, 21, 13);
        filled(s, fur);
        stroke(new QuadCurve2D.Double(-6, -9, -7, -3, -6, 2), line * 0.55f, ink);
        stroke(new QuadCurve2D.Double(6, -9, 7, -3, 6, 2), line * 0.55f, ink);
        g.setTransform(old);
    }

    void tail(boolean lying) {
        AffineTransform old = g.getTransform();
        if (cat) {
            double sw = wave(t, 2.0, -7, 9, 0);
            if (lying) {
                push(258, 270, sw * 0.5, 1, 1);
                Path2D p = new Path2D.Double();
                p.moveTo(0, 0);
                p.curveTo(24, 10, 20, 26, -20, 22);
                p.curveTo(-60, 20, -96, 22, -118, 14);
                tube(p, 22, CAT_GREY);
            } else {
                push(98, 286, sw, 1, 1);
                Path2D p = new Path2D.Double();
                p.moveTo(0, 0);
                p.curveTo(-44, 6, -66, -22, -58, -60);
                p.quadTo(-54, -78, -40, -84);
                tube(p, 26, CAT_GREY);
            }
        } else {
            double sw = wave(t, 0.5, -14, 18, 0);
            if (lying) {
                push(266, 282, sw * 0.4 + 62, 1, 1);
            } else {
                push(222, 272, sw, 1, 1);
            }
            Path2D p = new Path2D.Double();
            p.moveTo(0, 0);
            p.quadTo(26, -10, 30, -44);
            tube(p, 24, DOG_FUR);
            filled(ell(30, -48, 14, 13), DOG_FUR);
        }
        g.setTransform(old);
    }

    // ---------- 자기 ----------
    void sleep() {
        AffineTransform base = g.getTransform();
        double breathe = wave(t, 3.6, 1.0, 1.035, 0);
        if (has("망토")) {
            // 누운 자세의 망토는 몸 뒤가 아니라 등 위에 덮는다(아래 vestOrCapeLying).
        }
        Path2D body = new Path2D.Double();
        body.moveTo(80, 302);
        body.curveTo(56, 262, 100, 196, 186, 196);
        body.curveTo(256, 196, 292, 240, 286, 284);
        body.quadTo(284, 302, 264, 302);
        body.closePath();
        g.translate(170, 302);
        g.scale(1, breathe);
        g.translate(-170, -302);
        filled(body, fur);
        if (cat) {
            shadeIn(body, ell(226, 210, 62, 34), CAT_GREY);
        }
        shadeIn(body, ell(170, 304, 120, 14), furShade);
        redraw(body);
        if (has("조끼")) {
