package planningpet.pet.view;

import java.awt.Graphics2D;
import java.util.Set;
import planningpet.pet.model.PetKind;
import planningpet.pet.model.WearId;

/**
 * 펫 그림(D-48, D-50). 그림 파일이 아니라 도형 부품으로 그려 크게 그려도 흐려지지 않는다. 생김새는 나의 방 예시(2026-11-23)의
 * 고양이와 강아지다. 지금은 앉은 펫, 얼굴, 앱 로고를 그리고 장신구는 앉은 펫의 목걸이만 그린다. 머리 장신구와 옷은 #414가
 * 더한다(docs/porting/pet-art.md).
 */
public final class PetArt {
    private PetArt() {
    }

    /**
     * 앉은 펫을 (x, ground)에 발을 붙여 한 변 size 상자에 그린다(고르기 창 카드). t는 움직임 시각(초)으로 숨쉬기, 머리 기울기, 귀,
     * 꼬리가 따른다. since는 고른 뒤 지난 초로, 0.35초 동안 0.9배에서 살짝 넘치며 제 크기로 튄다. 고른 적이 없으면 큰 값(예: 99)을 준다.
     * wear는 입은 장신구(D-51)이고 null이면 없는 것으로 본다.
     */
    public static void drawAt(Graphics2D g0, PetKind kind, Set<WearId> wear, double t, double since, double x, double ground,
            double size) {
        Graphics2D g = (Graphics2D) g0.create();
        PetPaintBase.hints(g);
        g.translate(x, ground);
        double k = size / 320.0;
        double pop = since < 0.35 ? 0.9 + 0.1 * PetPaintBase.easeOutBack(since / 0.35) : 1.0;
        g.scale(k * pop, k * pop);
        g.translate(-160, -300);
        new PetPaintBody(g, kind, wear, t).sit();
        g.dispose();
    }

    /**
     * 앉은 펫의 그림 높이를 상자 size에 대한 비율로 준다(바닥에서 귀나 머리 끝까지). 고른 카드의 강조선 자리를 잰다.
     * 프로토타입이 2026-11-24 기본 목걸이를 입혀 그려 잰 값이다. 목걸이는 칠하는 칸을 바꾸지 않는다(docs/porting/pet-art.md E-WEAR-8).
     */
    public static double heightRatio(PetKind kind) {
        return kind == PetKind.CAT ? 0.88 : 0.77;
    }

    /** 펫 칩과 작은 자리에 쓰는 얼굴(머리만). (cx, cy)는 얼굴 가운데, size는 얼굴 폭이다. 작게 그릴수록 선을 굵게 둔다. */
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
