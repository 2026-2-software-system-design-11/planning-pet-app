package planningpet.home.view;

import java.awt.Color;

/** 홈 펫 카드(D-59)의 그림 도구. 값은 디자인 시스템 10절 "홈"에 있다(docs/porting/home.md S3j). */
final class PetCardArt {
    private PetCardArt() {
    }

    static Color mix(Color a, Color b, double k) {
        return new Color((int) (a.getRed() * (1 - k) + b.getRed() * k), (int) (a.getGreen() * (1 - k) + b.getGreen() * k),
                (int) (a.getBlue() * (1 - k) + b.getBlue() * k));
    }

    static Color withAlpha(Color c, int a) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), a);
    }
}
