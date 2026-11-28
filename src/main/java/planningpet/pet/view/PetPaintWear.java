package planningpet.pet.view;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import planningpet.pet.model.PetKind;

/** PetArt 펫 그림의 장신구(D-51): 목걸이와 그 장식(docs/porting/pet-art.md 5절). 머리와 몸이 이것을 물려받아 입힌다. */
abstract class PetPaintWear extends PetPaintBase {
    PetPaintWear(Graphics2D g, PetKind kind, double t) {
        super(g, kind, t);
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
