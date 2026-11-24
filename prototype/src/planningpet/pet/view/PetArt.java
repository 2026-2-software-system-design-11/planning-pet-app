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

/**
 * 나의 방과 캐릭터 화면의 펫(D-48, D-50, D-51). 그림 파일이 아니라 도형 부품으로 그린다.
 * 생김새는 나의 방 예시(2026-11-23)의 고양이와 강아지다. 해부 기록은 ppa-design-work/art-room/README.md에 있다.
 *
 * 좌표: 320x320 상자 안에서 정한다. 발이 닿는 바닥은 y=300, 가운데는 x=160이다. drawAt이 상자를 size로 늘린다.
 * 도형이라 크게 그려도 흐려지지 않는다.
 *
 * 움직임: t는 초다. 화면은 javax.swing.Timer(30ms 안팎)로 t를 올리며 repaint하고, 동작 줄이기 설정이면 t를 0으로 고정한다.
 * since는 자세를 바꾼 뒤 지난 초다. 0.35초 동안 살짝 튀며 새 자세로 들어간다. 바꾼 적이 없으면 큰 값(예: 99)을 준다.
 *
 * 부품 나무: 몸(숨쉬기) 아래 목(목걸이, 스카프, 조끼, 망토)과 머리(귀, 얼굴, 모자, 핀, 안경)가 붙는다.
 * 자세는 머리와 목의 자리, 몸 모양, 앞발과 꼬리 자리를 바꾸는 묶음이다.
 */
public final class PetArt {
    /** 모습 단계(D-20)의 크기 비율(D-50). */
    public static double stageScale(int stage) {
        return stage <= 1 ? 0.72 : stage == 2 ? 0.86 : 1.0;
    }

    private PetArt() {
    }

    /**
     * (x, ground)에 발을 붙여 size 상자에 그린다. wear는 입은 장신구 이름(D-51, 예: "기본 목걸이", "밀짚모자").
     */
    public static void drawAt(Graphics2D g0, PetKind kind, PetPose pose, Set<String> wear, double t, double since,
            double x, double ground, double size) {
        Graphics2D g = (Graphics2D) g0.create();
        hints(g);
        g.translate(x, ground);
        double k = size / 320.0;
        double pop = since < 0.35 ? 0.9 + 0.1 * PetPaintBase.easeOutBack(since / 0.35) : 1.0;
        // 누운 자세는 옆으로 길어 상자 폭을 넘으므로 조금 줄여 같은 상자에 넣는다.
        double fit = pose == PetPose.SLEEP || pose == PetPose.STRETCH ? 0.86 : 1.0;
        g.scale(k * pop * fit, k * pop * fit);
        g.translate(-160, -300);
        new PetPaintBody(g, kind, pose, wear, t).paint();
        g.dispose();
    }

    /** 펫 칩과 작은 자리에 쓰는 얼굴(머리만). (cx, cy)는 얼굴 가운데, size는 얼굴 폭이다. 모자와 핀도 그린다. */
    public static void drawFace(Graphics2D g0, PetKind kind, Set<String> wear, double cx, double cy, double size) {
        Graphics2D g = (Graphics2D) g0.create();
        hints(g);
        g.translate(cx, cy);
        double k = size / 230.0;
        g.scale(k, k);
        PetPaintBody p = new PetPaintBody(g, kind, PetPose.SIT, wear, 0);
        p.line = size < 48 ? 9f : size < 90 ? 7.5f : PetPaintBase.LINE;
        p.head(0, 0, 0, PetPaintBase.Face.HAPPY, true);
        g.dispose();
    }

    /** 장신구 파일 이름(wear/*.png)과 D-51 이름. 아이콘을 그림 파일로 내보낼 때 쓴다. */
    public static final String[][] WEAR_FILES = {
        {"collar-basic", "기본 목걸이"}, {"collar-ribbon", "리본 목걸이"}, {"collar-star", "별 목걸이"},
        {"collar-crown", "왕관 목걸이"}, {"pin-flower", "꽃 핀"}, {"pin-ribbon", "리본 핀"}, {"glasses", "안경"},
        {"hat-straw", "밀짚모자"}, {"hat-beret", "베레모"}, {"hat-crown", "왕관"}, {"scarf", "스카프"},
        {"vest", "조끼"}, {"cape", "망토"}};

    /**
     * 장신구 하나만 아이콘으로 그린다(장신구 카드, 나의 방 장신구 칸). 펫이 입은 모습과 같은 도형이다.
     * (cx, cy)는 가운데, size는 아이콘 상자의 한 변이다.
     */
    public static void drawWear(Graphics2D g0, String name, double cx, double cy, double size) {
        Graphics2D g = (Graphics2D) g0.create();
        hints(g);
        PetPaintBody p = new PetPaintBody(g, PetKind.CAT, PetPose.SIT, Set.of(name), 0);
        // {가운데 x, 가운데 y, 폭} : 펫 머리나 목 좌표에서 그 장신구가 차지하는 자리
        double[] box;
        switch (name) {
            case "꽃 핀": box = new double[] {56, -56, 46}; break;
            case "리본 핀": box = new double[] {-56, -58, 64}; break;
            case "안경": box = new double[] {0, -4, 136}; break;
            case "밀짚모자": box = new double[] {0, -84, 200}; break;
