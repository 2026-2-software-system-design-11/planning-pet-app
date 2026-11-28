package planningpet.pet.view;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Set;
import planningpet.pet.model.PetKind;
import planningpet.pet.model.Pose;
import planningpet.pet.model.WearId;

/**
 * 펫 그림(D-48, D-50). 그림 파일이 아니라 도형 부품으로 그려 크게 그려도 흐려지지 않는다. 생김새는 나의 방 예시(2026-11-23)의
 * 고양이와 강아지다. 다섯 자세의 펫, 얼굴, 앱 로고를 그리고 장신구는 펫의 목걸이, 머리 장신구, 옷과 얼굴의 머리 장신구를 그린다
 * (docs/porting/pet-art.md, docs/porting/character.md S8b, S8c).
 */
public final class PetArt {
    private PetArt() {
    }

    /**
     * 앉은 펫을 (x, ground)에 발을 붙여 한 변 size 상자에 그린다(고르기 창 카드). t는 움직임 시각(초)으로 숨쉬기, 머리 기울기, 귀,
     * 꼬리가 따른다. since는 고른 뒤 지난 초로, 0.35초 동안 0.9배에서 살짝 넘치며 제 크기로 튄다. 고른 적이 없으면 큰 값(예: 99)을 준다.
     * wear는 입은 장신구(D-51)이고 null이면 없는 것으로 본다. 망토를 입으면 아랫단이 바닥 아래로 상자의 약 6%까지 내려온다
     * (docs/porting/pet-art.md E-CLO-6).
     */
    public static void drawAt(Graphics2D g0, PetKind kind, Set<WearId> wear, double t, double since, double x, double ground,
            double size) {
        drawAt(g0, kind, Pose.SIT, wear, t, since, x, ground, size);
    }

    /**
     * 자세 pose(D-50)로 그린다. 나머지는 앉은 펫과 같다. 밥 먹기는 밥그릇 아래 끝이 바닥보다 상자의 약 2% 아래까지 내려온다. 자기와
     * 기지개는 옆으로 길어 0.86배로 줄여 같은 상자에 넣는다(docs/porting/character.md S8b, S8c).
     */
    public static void drawAt(Graphics2D g0, PetKind kind, Pose pose, Set<WearId> wear, double t, double since, double x,
            double ground, double size) {
        Graphics2D g = (Graphics2D) g0.create();
        PetPaintBase.hints(g);
        g.translate(x, ground);
        double k = size / 320.0;
        double pop = since < 0.35 ? 0.9 + 0.1 * PetPaintBase.easeOutBack(since / 0.35) : 1.0;
        double fit = pose == Pose.SLEEP || pose == Pose.STRETCH ? 0.86 : 1.0;
        g.scale(k * pop * fit, k * pop * fit);
        g.translate(-160, -300);
        new PetPaintBody(g, kind, pose, wear, t).paint();
        g.dispose();
    }

    /** 모습 단계(D-20)의 크기 비율(D-50): 1단계 0.72배, 2단계 0.86배, 3단계 1배. 캐릭터 창과 나의 방이 쓴다. */
    public static double stageScale(int stage) {
        return stage <= 1 ? 0.72 : stage == 2 ? 0.86 : 1.0;
    }

    /** 홈 펫 카드의 모습 단계(D-20)별 크기 비율(D-93). 1단계도 카드 칸의 3분의 2 넘게 그리려고 단계 사이 차를 줄였다. */
    public static double cardScale(int stage) {
        return stage <= 1 ? 0.94 : stage == 2 ? 0.97 : 1.0;
    }

    /**
     * 앉은 펫의 그림 높이를 상자 size에 대한 비율로 준다(바닥에서 귀나 머리 끝까지). 고른 카드의 강조선 자리를 잰다.
     * 프로토타입이 2026-11-24 기본 목걸이를 입혀 그려 잰 값이다. 목걸이는 칠하는 칸을 바꾸지 않고 옷은 위 끝을 바꾸지 않는다(docs/porting/pet-art.md E-WEAR-8,
     * E-CLO-6).
     */
    public static double heightRatio(PetKind kind) {
        return heightRatio(kind, Pose.SIT);
    }

    /** 자세마다의 그림 높이 비율. 머리 위 하트와 강조선 자리에 쓴다. 값은 프로토타입이 같은 날 같은 방법으로 잰 것이다. */
    public static double heightRatio(PetKind kind, Pose pose) {
        boolean cat = kind == PetKind.CAT;
        return switch (pose) {
            case SIT -> cat ? 0.88 : 0.77;
            case SLEEP -> cat ? 0.51 : 0.50;
            case STRETCH -> cat ? 0.62 : 0.47;
            case EAT -> cat ? 0.83 : 0.73;
            case SING -> cat ? 0.92 : 0.86;
        };
    }

    /**
     * 펫 칩과 작은 자리에 쓰는 얼굴(머리만). (cx, cy)는 얼굴 가운데, size는 얼굴 폭이다. 작게 그릴수록 선을 굵게 둔다. 입은 머리
     * 장신구도 그리고 목걸이는 그리지 않는다.
     */
    public static void drawFace(Graphics2D g0, PetKind kind, Set<WearId> wear, double cx, double cy, double size) {
        Graphics2D g = (Graphics2D) g0.create();
        PetPaintBase.hints(g);
        g.translate(cx, cy);
        double k = size / 230.0;
        g.scale(k, k);
        PetPaintHead p = new PetPaintHead(g, kind, wear, 0);
        p.line = size < 48 ? 9f : size < 90 ? 7.5f : PetPaintBase.LINE;
        p.head(0, 0, 0);
        g.dispose();
    }

    /** 펫 탭과 펫 칩의 얼굴 그림: 96x96 투명 그림에 폭 0.82배의 얼굴을 가운데, 위에서 0.56 자리에 그린다. 머리 장신구는 그리고 목걸이는 그리지 않는다. */
    public static BufferedImage face(PetKind kind, Set<WearId> wear) {
        int n = 96;
        BufferedImage im = new BufferedImage(n, n, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = im.createGraphics();
        drawFace(g, kind, wear, n / 2.0, n * 0.56, n * 0.82);
        g.dispose();
        return im;
    }

    /**
     * 앱 로고(사이드바, 고르기 창 머리): 고양이 얼굴을 (x, y, w, h) 상자 가운데에 가장 크게 그린다(디자인 시스템 10절).
     * 얼굴은 폭이 size의 1.10배, 높이가 0.83배(가운데 위 0.51, 아래 0.31)라서 그 비율로 상자에 맞춘다.
     */
    public static void drawLogo(Graphics2D g, double x, double y, double w, double h) {
        double size = Math.min(w / 1.10, h / 0.84);
        double cx = x + w / 2;
        double cy = y + (h - 0.83 * size) / 2 + 0.51 * size;
        drawFace(g, PetKind.CAT, Set.of(), cx, cy, size);
    }
}
