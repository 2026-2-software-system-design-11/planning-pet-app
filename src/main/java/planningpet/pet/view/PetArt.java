package planningpet.pet.view;

import java.awt.Graphics2D;

/**
 * 펫 그림(D-48, D-50). 그림 파일이 아니라 도형 부품으로 그려 크게 그려도 흐려지지 않는다. 생김새는 나의 방 예시(2026-11-23)의
 * 고양이다. 지금은 장신구 없는 고양이 얼굴과 앱 로고를 그린다. 강아지와 앉은 몸은 #408, 장신구는 #409가 더한다
 * (docs/porting/pet-art.md).
 */
public final class PetArt {
    private PetArt() {
    }

    /** 펫 칩과 작은 자리에 쓰는 얼굴(머리만). (cx, cy)는 얼굴 가운데, size는 얼굴 폭이다. 작게 그릴수록 선을 굵게 둔다. */
    public static void drawFace(Graphics2D g0, double cx, double cy, double size) {
        Graphics2D g = (Graphics2D) g0.create();
        PetPaintBase.hints(g);
        g.translate(cx, cy);
        double k = size / 230.0;
        g.scale(k, k);
        PetPaintHead p = new PetPaintHead(g);
        p.line = size < 48 ? 9f : size < 90 ? 7.5f : PetPaintBase.LINE;
        p.head();
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
        drawFace(g, cx, cy, size);
    }
}
