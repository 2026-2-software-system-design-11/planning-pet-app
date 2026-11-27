package planningpet.pet.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;

/** PetArt 펫 그림의 바탕: 색, 한 번 그리는 데 필요한 값, 그리기 도구. 머리는 PetPaintHead가 더한다(docs/porting/pet-art.md). */
abstract class PetPaintBase {
    // ---------- 색 (예시 픽셀에서 뽑은 값) ----------
    static final Color CAT_INK = new Color(0x3B3535);
    static final Color CAT_GREY = new Color(0xA9A8AE);
    static final Color CAT_WHITE = new Color(0xFFFEFC);
    static final Color CAT_WHITE_SHADE = new Color(0xE8E3E3);
    static final Color CAT_EAR = new Color(0xF6B8B4);
    static final Color BLUSH = new Color(247, 160, 165, 150);
    static final Color NOSE = new Color(0x4A2E28);
    /** 큰 그림의 선 굵기. 작게 그릴 때는 부르는 쪽이 line을 굵게 바꾼다. */
    static final float LINE = 6f;

    final Graphics2D g;
    final Color ink = CAT_INK;
    float line = LINE;

    PetPaintBase(Graphics2D g) {
        this.g = g;
    }

    static void hints(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }

    static Ellipse2D ell(double cx, double cy, double rx, double ry) {
        return new Ellipse2D.Double(cx - rx, cy - ry, rx * 2, ry * 2);
    }

    // ---------- 그리기 도구 ----------
    BasicStroke stroke(float w) {
        return new BasicStroke(w, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    }

    /** c로 칠하고 선 굵기의 외곽선을 긋는다. */
    void filled(Shape s, Color c) {
        g.setColor(c);
        g.fill(s);
        redraw(s);
    }

    void stroke(Shape s, float w, Color c) {
        g.setColor(c);
        g.setStroke(stroke(w));
        g.draw(s);
    }

    /** s 안쪽에만 shade를 칠한다(그림자 한 단). */
    void shadeIn(Shape s, Shape shade, Color c) {
        Shape old = g.getClip();
        g.clip(s);
        g.setColor(c);
        g.fill(shade);
        g.setClip(old);
    }

    /** 외곽선만 다시 긋는다(안쪽을 칠한 뒤 덮인 선). */
    void redraw(Shape s) {
        g.setColor(ink);
        g.setStroke(stroke(line));
        g.draw(s);
    }
}
