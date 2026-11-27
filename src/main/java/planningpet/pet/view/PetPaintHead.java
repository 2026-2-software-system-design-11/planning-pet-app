package planningpet.pet.view;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;

/** PetArt 펫 그림의 머리: 고양이 귀(docs/porting/pet-art.md). 좌표는 머리 가운데가 (0, 0)이고 얼굴 폭이 230이다. */
class PetPaintHead extends PetPaintBase {
    PetPaintHead(Graphics2D g) {
        super(g);
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
}
