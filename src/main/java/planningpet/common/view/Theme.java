package planningpet.common.view;

import java.awt.Color;

/** 디자인 시스템(docs/design-system.md)의 값. 이름은 문서의 이름과 같다. */
public final class Theme {
    /** 글자. 예시(2026-11-23 나의 방)의 진남색. 순흑을 쓰지 않는다. */
    public static final Color INK = new Color(0x1B2238);
    /** 보조 글자(부제, 안내, 라벨). */
    public static final Color INK_SUB = new Color(0x4A5A72);
    public static final Color INK_DIM = new Color(0x1B, 0x22, 0x38, 102);
    /** 선 아이콘. 메뉴, 제목, 카드 제목의 아이콘이 모두 이 색이다. */
    public static final Color ICON = new Color(0x0E6A64);
    public static final Color MINT = new Color(0x3FCFAC);
    /** 주 행동 버튼과 고른 탭의 채움(위에서 아래로 MINT_TOP에서 MINT_DEEP). */
    public static final Color MINT_TOP = new Color(0x52D9B8);
    public static final Color MINT_DEEP = new Color(0x1FB99C);
    public static final Color MINT_SELECTED = new Color(0x7BDECB);
    public static final Color MINT_SOFT = new Color(0xC7F0E4);
    public static final Color MINT_PALE = new Color(0xE3F7F1);
    /** 고른 메뉴의 알약 바탕. */
    public static final Color NAV_PILL = new Color(0xBDF1E2);
    public static final Color MINT_LINE = new Color(0xAAD9DD);
    /** 사이드바 카드. 바탕이 비치는 흰색. */
    public static final Color SIDE = new Color(255, 255, 255, 175);
    /** 본문 판. 사이드바 옆의 큰 밝은 판이고 화면의 카드가 이 위에 놓인다. */
    public static final Color PANEL = new Color(255, 255, 255, 165);
    public static final Color BG = new Color(0xF0F9FA);
    /** 창 바탕 그라데이션의 위와 아래. */
    public static final Color BG_TOP = new Color(0xCDEEF5);
    public static final Color BG_BOTTOM = new Color(0xB9E8EC);
    public static final Color CARD_TOP = Color.WHITE;
    public static final Color CARD_BOTTOM = new Color(0xFAFDFE);
    public static final Color CARD_LINE = new Color(0xC2E5F0);
    /** 카드 가장자리. 거의 보이지 않는 선이다. */
    public static final Color CARD_EDGE = new Color(0xE2F0F4);
    public static final Color CARD_SHADOW = new Color(0x6F, 0xA9, 0xB8, 26);
    public static final Color TRACK = new Color(0xD7E9EF);
    public static final Color DAY = new Color(0xF2F9FD);
    public static final Color DAY_SELECTED = new Color(0x8FE0CA);
    public static final Color SUNDAY = new Color(0xE55C64);
    public static final Color SATURDAY = new Color(0x4C82DA);
    public static final Color TASK_TEXT = new Color(0x28977F);
    public static final Color COIN = new Color(0xFCD828);
    public static final Color COIN_RIM = new Color(0xEDA11F);
    public static final Color HEART = new Color(0xF7646E);
    public static final Color SPARK = new Color(0xF8C630);
    /** 놓인 아이템의 테두리와 체크 배지. */
    public static final Color CHECK = new Color(0x1DB898);
    /** 홈 달력의 오늘(2026-11-24 홈 예시의 연민트 둥근 네모). */
    public static final Color DAY_TODAY = new Color(0xC2F0E0);
    /** 끝낸 할 일 줄의 바탕(홈 예시의 민트빛 줄). */
    public static final Color ROW_DONE = new Color(0xE3, 0xF7, 0xF1, 150);
    /** 홈 펫 카드 장면의 하늘(위에서 아래로)과 풀밭(가까운 곳, 먼 곳, 풀잎). */
    public static final Color SKY_TOP = new Color(0xC9E9FC);
    public static final Color SKY_BOTTOM = new Color(0xEAF6FD);
    public static final Color GRASS = new Color(0xDCEDA2);
    public static final Color GRASS_FAR = new Color(0xCCEBC8);
    public static final Color GRASS_DEEP = new Color(0xA9D38A);
    /** 흰 바탕이 아닌 곳(하늘 위)의 진행 막대 바탕과 진행 칸의 빈 칸. */
    public static final Color TRACK_DEEP = new Color(0xB4D0E2);
    /** 반려동물 고르기 창(2026-11-23 고르기 예시). 리본 띠(위에서 아래로)와 뒤로 접힌 꼬리. 흰 글과 띠 가운데가 3:1을 넘게 예시(#55B8AA)보다 진하다. */
    public static final Color RIBBON_TOP = new Color(0x3DA898);
    public static final Color RIBBON_DEEP = new Color(0x2C9282);
    public static final Color RIBBON_TAIL = new Color(0x7FCDBF);
    /** 고르기 카드 바탕(위에서 아래로)과 안쪽 테두리, 카드 안 언덕. 고양이는 차가운 하늘빛, 강아지는 따뜻한 크림이다. */
    public static final Color COOL_TOP = new Color(0xEAF8FD);
    public static final Color COOL_BOTTOM = new Color(0xDDF3FB);
    public static final Color COOL_LINE = new Color(0xBFE6EA);
    public static final Color COOL_HILL = new Color(0xC9EDCE);
    public static final Color WARM_TOP = new Color(0xFEF9F0);
    public static final Color WARM_BOTTOM = new Color(0xFCF2E2);
    public static final Color WARM_LINE = new Color(0xF4DDBC);
    public static final Color WARM_HILL = new Color(0xE5EDBA);
    /** 강아지 카드의 발자국(예시 #885832, 크림 바탕과 5.5:1). */
    public static final Color WARM_ICON = new Color(0x885832);
    /** 하늘의 발자국 장식과 먼 산. */
    public static final Color SKY_PAW = new Color(0x9ECEED);
    public static final Color MOUNTAIN = new Color(0xCDEAF6);
    /** 데이지 꽃잎과 가운데. */
    public static final Color DAISY = new Color(0xFEFCF4);
    public static final Color DAISY_HEART = new Color(0xFDD65E);
    public static final int CARD_RADIUS = 18;
    public static final int BUTTON_RADIUS = 22;
    public static final int IMAGE_RADIUS = 16;
    public static final int BUBBLE_RADIUS = 16;
    /** 카드 사이 간격(디자인 시스템 4절). */
    public static final int GAP = 12;

    private Theme() {
    }
}
