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


/** PetArt 펫 그림의 바탕: 색, 시간 함수, 한 번 그리는 데 필요한 값, 그리기 도구. 자세와 부품은 아래 클래스가 더한다. */
abstract class PetPaintBase {
    // ---------- 색 (예시 픽셀에서 뽑은 값) ----------
    static final Color CAT_INK = new Color(0x3B3535);
    static final Color CAT_GREY = new Color(0xA9A8AE);
    static final Color CAT_GREY_SHADE = new Color(0x8F8D96);
    static final Color CAT_WHITE = new Color(0xFFFEFC);
    static final Color CAT_WHITE_SHADE = new Color(0xE8E3E3);
    static final Color CAT_EAR = new Color(0xF6B8B4);
    static final Color DOG_INK = new Color(0x5A3424);
    static final Color DOG_FUR = new Color(0xFEF1CF);
    static final Color DOG_FUR_SHADE = new Color(0xF6D7A2);
    static final Color DOG_EAR = new Color(0xF8C67E);
    static final Color DOG_EAR_SHADE = new Color(0xEBA66A);
    static final Color BLUSH = new Color(247, 160, 165, 150);
    static final Color NOSE = new Color(0x4A2E28);
    static final Color MOUTH = new Color(0x8A3B3B);
    static final Color TONGUE = new Color(0xF27E86);
    static final Color TEAL = new Color(0x2BA39A);
    static final Color GOLD = new Color(0xF5C542);
    static final Color GOLD_SHADE = new Color(0xD99A1E);
    static final Color PINK = new Color(0xF48FA0);
    static final Color PINK_LIGHT = new Color(0xF9B8C6);
    static final Color RED = new Color(0xE8483C);
    static final Color RED_SHADE = new Color(0xC4332B);
    static final Color BLUE = new Color(0x6C8EE0);
    static final Color MINT = new Color(0x7FCBB6);
    static final Color MINT_SHADE = new Color(0x5CAE98);
    static final Color STRAW = new Color(0xF4D58A);
    static final Color STRAW_SHADE = new Color(0xD9B160);
    static final Color BERET = new Color(0xE8737A);
    static final Color CAPE = new Color(0x7A6FD0);
    static final Color CAPE_INNER = new Color(0xE8483C);
    static final Color BOWL = new Color(0x9ED9C0);
    static final Color KIBBLE = new Color(0xC97F3E);
    static final Color NOTE = new Color(0x2BA39A);
    static final float LINE = 6f;

    enum Face { HAPPY, SLEEP, SING, EAT, STRETCH }

static void hints(Graphics2D g) {
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
}

// ---------- 시간 함수 ----------
static double wave(double t, double period, double a, double b, double phase) {
    double x = (t / period + phase) * 2 * Math.PI;
    return a + (b - a) * (0.5 - 0.5 * Math.cos(x));
}

static double easeOutBack(double x) {
    x = Math.max(0, Math.min(1, x));
    double c1 = 1.70158;
    double c3 = c1 + 1;
    return 1 + c3 * Math.pow(x - 1, 3) + c1 * Math.pow(x - 1, 2);
}

static Ellipse2D ell(double cx, double cy, double rx, double ry) {
    return new Ellipse2D.Double(cx - rx, cy - ry, rx * 2, ry * 2);
}

static Color alpha(Color c, int a) {
    return new Color(c.getRed(), c.getGreen(), c.getBlue(), a);
}


    final Graphics2D g;
    final PetKind kind;
    final PetPose pose;
    final Set<String> wear;
    final double t;
    final boolean cat;
    final Color ink;
    final Color fur;
    final Color furShade;
    float line = LINE;

    PetPaintBase(Graphics2D g, PetKind kind, PetPose pose, Set<String> wear, double t) {
        this.g = g;
        this.kind = kind;
        this.pose = pose;
        this.wear = wear == null ? Set.of() : wear;
        this.t = t;
        this.cat = kind == PetKind.CAT;
        this.ink = cat ? CAT_INK : DOG_INK;
        this.fur = cat ? CAT_WHITE : DOG_FUR;
        this.furShade = cat ? CAT_WHITE_SHADE : DOG_FUR_SHADE;
    }

    boolean has(String name) {
        return wear.contains(name);
    }

    // ---------- 그리기 도구 ----------
    BasicStroke stroke(float w) {
        return new BasicStroke(w, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    }

    void filled(Shape s, Color c) {
        g.setColor(c);
        g.fill(s);
        g.setColor(ink);
        g.setStroke(stroke(line));
        g.draw(s);
    }

    void stroke(Shape s, float w, Color c) {
        g.setColor(c);
        g.setStroke(stroke(w));
        g.draw(s);
    }

    /** 외곽선 있는 굵은 관(꼬리, 다리). 어두운 선을 넓게, 그 위에 털색을 좁게 긋는다. */
    void tube(Shape path, float w, Color c) {
        stroke(path, w + line * 2, ink);
        stroke(path, w, c);
    }

    /** s 안쪽에만 shade를 칠한다(그림자 한 단). */
    void shadeIn(Shape s, Shape shade, Color c) {
        Shape old = g.getClip();
        g.clip(s);
        g.setColor(c);
        g.fill(shade);
        g.setClip(old);
    }

    void push(double x, double y, double rot, double sx, double sy) {
        g.translate(x, y);
        g.rotate(Math.toRadians(rot));
        g.scale(sx, sy);
    }

    void redraw(Shape s) {
        g.setColor(ink);
        g.setStroke(stroke(line));
        g.draw(s);
    }
}
