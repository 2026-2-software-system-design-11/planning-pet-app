package planningpet.pet.view;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Set;
import planningpet.pet.model.PetKind;
import planningpet.pet.model.Pose;
import planningpet.pet.model.WearId;

/**
 * PetArt 펫 그림의 몸: 다섯 자세(D-50)의 몸, 앞발, 꼬리와 그 위의 머리, 밥그릇과 음표와 Zzz(docs/porting/pet-art.md 4절,
 * docs/porting/character.md S8b, S8c). 좌표는 320x320 상자이고 발이 닿는 바닥은 y=300, 가운데는 x=160이다.
 */
final class PetPaintBody extends PetPaintHead {
    final Pose pose;

    /** 앉기로 그린다. */
    PetPaintBody(Graphics2D g, PetKind kind, Set<WearId> wear, double t) {
        this(g, kind, Pose.SIT, wear, t);
    }

    /** 자세 pose로 그린다. 표정은 자세를 따른다. */
    PetPaintBody(Graphics2D g, PetKind kind, Pose pose, Set<WearId> wear, double t) {
        super(g, kind, wear, t);
        this.pose = pose;
        face = switch (pose) {
            case SIT -> Face.HAPPY;
            case SLEEP -> Face.SLEEP;
            case STRETCH -> Face.STRETCH;
            case EAT -> Face.EAT;
            case SING -> Face.SING;
        };
    }

    /** 자세대로 그린다. 자기는 누운 몸, 나머지는 앉은 몸이다. */
    void paint() {
        switch (pose) {
            case SLEEP -> sleep();
            default -> sit();
        }
    }

    /**
     * 앉은 펫을 그린다. 몸은 바닥을 기준으로 2.6초 주기로 숨쉬며 위아래로 조금 부풀고(조끼도 함께), 목 둘레와 머리는 몸을 따라 오르내린다. 머리는
     * 3.4초 주기로 좌우로 기운다(t). 망토 뒤판은 꼬리보다 먼저 몸 뒤에 그려 숨쉬지 않는다. 밥 먹기는 머리를 숙여 0.7초 주기로 까딱이며
     * 밥그릇에 대고, 노래는 머리를 조금 들어 1.6초 주기로 기울이고 오른 앞발을 들어 0.8초 주기로 흔들며 음표 둘을 띄운다.
     */
    void sit() {
        AffineTransform base = g.getTransform();
        double breathe = wave(t, 2.6, 1.0, 1.022, 0);
        double headRot = wave(t, 3.4, -2.5, 2.5, 0.2);
        double headY = 0;
        if (pose == Pose.EAT) {
            headY = 14 + wave(t, 0.7, 0, 7, 0);
            headRot = 0;
        } else if (pose == Pose.SING) {
            headRot = wave(t, 1.6, -9, 5, 0);
            headY = -4;
        }
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
        paw(138, 294, 0);
        if (pose == Pose.SING) {
            paw(206, 230 - wave(t, 0.8, 0, 8, 0), -30);
        } else {
            paw(182, 294, 0);
        }
        g.setTransform(base);
        neckWear(160, 202 + (breathe - 1) * -110 + headY * 0.3, 58);
        if (pose == Pose.EAT) {
            bowl();
        }
        head(160, 134 + headY + (breathe - 1) * -110, headRot);
        if (pose == Pose.SING) {
            notes();
        }
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

    /** 앞발 하나를 (x, y)에 rot도 돌려 그린다: 털색 타원에 발가락 선 둘. */
    void paw(double x, double y, double rot) {
        AffineTransform old = g.getTransform();
        push(x, y, rot);
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

    /** 누운 꼬리. 고양이는 엉덩이에서 바닥을 따라 왼쪽으로 길게 눕힌 회색 꼬리, 강아지는 엉덩이에서 오른쪽으로 뻗은 짧은 꼬리다. 앉은 꼬리보다 덜 흔들린다(t). */
    void lyingTail() {
        AffineTransform old = g.getTransform();
        Path2D p = new Path2D.Double();
        p.moveTo(0, 0);
        if (cat) {
            push(258, 270, wave(t, 2.0, -7, 9, 0) * 0.5);
            p.curveTo(24, 10, 20, 26, -20, 22);
            p.curveTo(-60, 20, -96, 22, -118, 14);
            tube(p, 22, CAT_GREY);
        } else {
            push(266, 282, wave(t, 0.5, -14, 18, 0) * 0.4 + 62);
            p.quadTo(26, -10, 30, -44);
            tube(p, 24, DOG_FUR);
            filled(ell(30, -48, 14, 13), DOG_FUR);
        }
        g.setTransform(old);
    }

    /**
     * 자는 펫: 옆으로 엎드린 몸이 3.6초 주기로 숨쉬고, 누운 꼬리, 앞발 둘, 왼쪽에 기울인 잠든 머리와 오른쪽 위 Zzz.
     * 목 장신구는 바닥(y=300) 아래를 잘라 그린다.
     */
    void sleep() {
        AffineTransform base = g.getTransform();
        double breathe = wave(t, 3.6, 1.0, 1.035, 0);
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
        g.setTransform(base);
        lyingTail();
        paw(150, 290, 0);
        paw(104, 292, 0);
        Shape clip = g.getClip();
        g.clip(new Rectangle2D.Double(0, 0, 320, 300));
        neckWear(116, 280, -6, 46);
        g.setClip(clip);
        head(112, 232, -6);
        zzz();
    }

    /** 밥 먹는 펫 앞의 민트 밥그릇: 사료가 담긴 윗면, 사료 알 일곱, 그릇, 흰 빛, 테두리. 아래 끝(y=306)은 바닥보다 아래다. */
    void bowl() {
        Path2D b = new Path2D.Double();
        b.moveTo(104, 270);
        b.lineTo(216, 270);
        b.quadTo(212, 304, 186, 306);
        b.lineTo(134, 306);
        b.quadTo(108, 304, 104, 270);
        b.closePath();
        filled(ell(160, 268, 58, 12), KIBBLE);
        for (int i = 0; i < 7; i++) {
            g.setColor(i % 2 == 0 ? KIBBLE.darker() : new Color(0xE9A35A));
            g.fill(ell(122 + i * 12, 264 + (i % 3) * 2, 6, 4));
        }
        filled(b, BOWL);
        g.setColor(new Color(255, 255, 255, 150));
        g.fill(new RoundRectangle2D.Double(116, 278, 26, 6, 6, 6));
        stroke(new Line2D.Double(104, 270, 216, 270), line, ink);
    }

    /**
     * 노래하는 펫 오른쪽 위의 음표 둘. 2초 주기로 흔들리며 오르고, 반 주기 어긋나 하나가 나타날 때 하나가 사라진다(t). 음수 시각도 주기로
     * 본다(프로토타입과 일부러 다름, character.md E-POSE-13).
     */
    void notes() {
        for (int i = 0; i < 2; i++) {
            double p = (t / 2.0 + i * 0.5) % 1.0;
            if (p < 0) {
                p += 1;
            }
            double x = 252 + i * 24 + Math.sin(p * Math.PI * 2) * 6;
            double y = 86 - p * 56;
            g.setColor(alpha(NOTE, (int) (255 * Math.min(1, Math.min(p * 4, (1 - p) * 3)))));
            g.fill(ell(x, y, 9, 7));
            g.setStroke(stroke(4f));
            g.draw(new Line2D.Double(x + 8, y, x + 8, y - 30));
            g.draw(new QuadCurve2D.Double(x + 8, y - 30, x + 22, y - 24, x + 18, y - 12));
        }
    }

    /** 자는 펫 오른쪽 위의 Z 셋. 2.6초 주기로 커지며 오른쪽 위로 오르고, 3분의 1 주기씩 어긋나 나타났다 사라진다(t). */
    void zzz() {
        for (int i = 0; i < 3; i++) {
            double p = (t / 2.6 + i / 3.0) % 1.0;
            double s = 9 + p * 8;
            double x = 214 + p * 46;
            double y = 176 - p * 70;
            Path2D z = new Path2D.Double();
            z.moveTo(x - s, y - s);
            z.lineTo(x + s, y - s);
            z.lineTo(x - s, y + s);
            z.lineTo(x + s, y + s);
            stroke(z, 4.5f, alpha(BLUE, (int) (255 * Math.min(1, Math.min(p * 4, (1 - p) * 3)))));
        }
    }
}
