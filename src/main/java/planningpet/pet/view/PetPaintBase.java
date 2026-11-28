package planningpet.pet.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.util.Set;
import planningpet.pet.model.PetKind;
import planningpet.pet.model.WearId;

/**
 * PetArt 펫 그림의 바탕: 색, 시간 함수, 한 번 그리는 데 필요한 값, 그리기 도구. 머리는 PetPaintHead, 몸은 PetPaintBody가 더한다
 * (docs/porting/pet-art.md).
 */
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
    static final Color PINK = new Color(0xF48FA0);
    static final Color PINK_LIGHT = new Color(0xF9B8C6);
    static final Color BLUE = new Color(0x6C8EE0);
    static final Color MINT = new Color(0x7FCBB6);
    static final Color MINT_SHADE = new Color(0x5CAE98);
    static final Color GOLD_SHADE = new Color(0xD99A1E);
    static final Color RED = new Color(0xE8483C);
    static final Color RED_SHADE = new Color(0xC4332B);
    static final Color STRAW = new Color(0xF4D58A);
    static final Color STRAW_SHADE = new Color(0xD9B160);
    static final Color BERET = new Color(0xE8737A);
    static final Color CAPE = new Color(0x7A6FD0);
    static final Color CAPE_INNER = new Color(0xE8483C);
    static final Color BOWL = new Color(0x9ED9C0);
    static final Color KIBBLE = new Color(0xC97F3E);
    static final Color NOTE = new Color(0x2BA39A);
    /** 큰 그림의 선 굵기. 작게 그릴 때는 부르는 쪽이 line을 굵게 바꾼다. */
    static final float LINE = 6f;

    /** 표정. 자세마다 하나다(D-50): 앉기는 웃음, 자기는 잠, 밥 먹기는 먹기, 노래는 노래, 기지개는 기지개. */
    enum Face { HAPPY, SLEEP, SING, EAT, STRETCH }

    final Graphics2D g;
    /** 움직임 시각(초). 귀 까딱임과 흔들림, 숨쉬기, 머리 기울기, 꼬리가 쓴다. */
    final double t;
    /** 입은 장신구(D-51). */
    final Set<WearId> wear;
    final boolean cat;
    final Color ink;
    final Color fur;
    final Color furShade;
    float line = LINE;

    PetPaintBase(Graphics2D g, PetKind kind, Set<WearId> wear, double t) {
        this.g = g;
        this.wear = wear == null ? Set.of() : wear;
        this.t = t;
        this.cat = kind == PetKind.CAT;
        this.ink = cat ? CAT_INK : DOG_INK;
        this.fur = cat ? CAT_WHITE : DOG_FUR;
        this.furShade = cat ? CAT_WHITE_SHADE : DOG_FUR_SHADE;
    }

    boolean has(WearId w) {
        return wear.contains(w);
    }

    static void hints(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }

    /** 시각 t에서 period초 주기로 a와 b 사이를 부드럽게 오가는 값. phase는 주기의 몇 분의 일만큼 앞서는지다. */
    static double wave(double t, double period, double a, double b, double phase) {
        double x = (t / period + phase) * 2 * Math.PI;
        return a + (b - a) * (0.5 - 0.5 * Math.cos(x));
    }

    /** 0에서 1로 가며 끝에서 조금 넘쳤다 돌아오는 값(고를 때 튀기). x는 0과 1 사이로 자른다. */
    static double easeOutBack(double x) {
        x = Math.max(0, Math.min(1, x));
        double c1 = 1.70158;
        double c3 = c1 + 1;
        return 1 + c3 * Math.pow(x - 1, 3) + c1 * Math.pow(x - 1, 2);
    }

    /** c를 알파 a(0~255)로 바꾼 색. 음표가 나타나고 사라질 때 쓴다. */
    static Color alpha(Color c, int a) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), a);
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

    /** (x, y)로 옮기고 rot도 돌린다. */
    void push(double x, double y, double rot) {
        g.translate(x, y);
        g.rotate(Math.toRadians(rot));
    }

    /** 외곽선 있는 굵은 관(꼬리). 짙은 선을 넓게, 그 위에 c를 좁게 긋는다. */
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

    /** 외곽선만 다시 긋는다(안쪽을 칠한 뒤 덮인 선). */
    void redraw(Shape s) {
        g.setColor(ink);
        g.setStroke(stroke(line));
        g.draw(s);
    }
}
