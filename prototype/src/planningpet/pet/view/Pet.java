package planningpet.pet.view;

import planningpet.pet.model.PetState;
import planningpet.common.view.Images;
import planningpet.reward.model.Level;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;

/** 펫 그림(디자인 시스템 6절). 자세(D-50)마다 그림이 있고 모습 단계는 크기로 보인다. 장신구(D-51)는 그림 위에 겹쳐 그린다. 그림을 못 읽으면 도형 임시 그림을 그린다. */
public final class Pet {
    private Pet() {
    }

    /** 고른 자세와 입은 장신구로 그린다. size는 그림 상자의 한 변, 그림은 상자 바닥(ground)에 발을 붙인다. */
    public static void drawAt(Graphics2D g, PetState p, int x, int ground, int size) {
        drawAt(g, p, p.pose, p.wear, x, ground, size);
    }

    /** 움직이는 자리(홈 펫 카드): t초의 숨쉬기와 꼬리로 그린다(PetArt 머리 주석). */
    public static void drawAt(Graphics2D g, PetState p, int x, int ground, int size, double t) {
        int stage = Level.stage(Level.level(p.exp));
        double box = size * PetArt.stageScale(stage);
        PetArt.drawAt(g, art(p.kind), pose(p.pose), new java.util.HashSet<>(p.wear.values()), t, 99, x, ground, box);
    }

    /** 캐릭터 화면의 미리보기처럼 자세와 장신구를 바꿔 그린다. 모습 단계(D-50)는 크기로 보인다. */
    public static void drawAt(Graphics2D g, PetState p, String pose, java.util.Map<String, String> wear, int x, int ground, int size) {
        int stage = Level.stage(Level.level(p.exp));
        drawStage(g, p.kind, pose, wear, stage, x, ground, size);
    }

    /** 성장 단계 그림처럼 단계를 정해 그린다. 펫은 도형으로 그려 크게 그려도 흐려지지 않는다(PetArt). */
    public static void drawStage(Graphics2D g, String kind, String pose, java.util.Map<String, String> wear, int stage,
                                 int x, int ground, int size) {
        double box = size * PetArt.stageScale(stage);
        java.util.Set<String> worn = wear == null ? java.util.Set.of() : new java.util.HashSet<>(wear.values());
        PetArt.drawAt(g, art(kind), pose(pose), worn, 0, 99, x, ground, box);
    }

    static PetKind art(String kind) {
        return "DOG".equals(kind) ? PetKind.DOG : PetKind.CAT;
    }

    /** D-50 자세 이름에서 그림 자세로. */
    static PetPose pose(String pose) {
        int i = planningpet.pet.model.Poses.ALL.indexOf(pose);
        return PetPose.values()[Math.max(0, i)];
    }

    /** 펫 칩의 얼굴 그림(입은 모자와 핀 포함). */
    public static java.awt.image.BufferedImage face(PetState p) {
        int n = 96;
        java.awt.image.BufferedImage im = new java.awt.image.BufferedImage(n, n, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = im.createGraphics();
        PetArt.drawFace(g, art(p.kind), new java.util.HashSet<>(p.wear.values()), n / 2.0, n * 0.56, n * 0.82);
        g.dispose();
        return im;
    }

    /** drawAt이 size 상자에 그리는 그림의 실제 높이. 머리 위 말풍선과 하트 자리에 쓴다. */
    public static int drawnHeight(PetState p, int size) {
        double box = size * PetArt.stageScale(Level.stage(Level.level(p.exp)));
        return (int) (box * PetArt.heightRatio(art(p.kind), pose(p.pose)));
    }

    /** (x, ground)는 발이 닿는 가운데 점이다. walking이면 다리를 벌린다. */
    public static void draw(Graphics2D g, String kind, int x, int ground, int stage, boolean walking) {
        int s = 14 + stage * 6;
        boolean cat = "CAT".equals(kind);
        Color body = cat ? new Color(0xF2A65A) : new Color(0xC8935A);
        Color dark = body.darker();
        g.setColor(dark);
        int legGap = walking ? s / 3 : s / 6;
        g.fillRect(x - legGap - 2, ground - s / 3, 4, s / 3);
        g.fillRect(x + legGap - 2, ground - s / 3, 4, s / 3);
        g.setColor(body);
        g.fillOval(x - s * 2 / 3, ground - s, s * 4 / 3, s * 2 / 3);
        int headX = x + s / 2;
        int headY = ground - s * 3 / 2;
        int head = s * 3 / 4;
        g.fillOval(headX - head / 2, headY, head, head);
        g.setColor(dark);
        if (cat) {
            g.fillPolygon(new Polygon(new int[] {headX - head / 2, headX - head / 4, headX - head / 2},
                    new int[] {headY + head / 3, headY, headY - head / 4}, 3));
            g.fillPolygon(new Polygon(new int[] {headX + head / 2, headX + head / 4, headX + head / 2},
                    new int[] {headY + head / 3, headY, headY - head / 4}, 3));
            g.drawLine(x - s * 2 / 3, ground - s * 2 / 3, x - s, ground - s * 4 / 3);
        } else {
            g.fillOval(headX - head / 2 - head / 6, headY + head / 6, head / 3, head / 2);
            g.fillOval(headX + head / 2 - head / 6, headY + head / 6, head / 3, head / 2);
        }
        g.setColor(Color.BLACK);
        g.fillOval(headX - head / 5, headY + head / 3, 3, 3);
        g.fillOval(headX + head / 6, headY + head / 3, 3, 3);
    }
}
