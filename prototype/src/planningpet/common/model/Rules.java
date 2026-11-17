package planningpet.common.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** 06의 작은 규칙들: 앱 날짜(1번), 레벨(10번), 대사의 글자 규칙(16번). */
public final class Rules {
    public static final int[] LEVEL_BASE = {0, 50, 130, 240, 380, 550, 750, 990, 1270, 1600};
    public static final int DAILY_CAP = 20;
    public static final int TASK_POINTS = 10;
    public static final int TASK_EXP = 10;
    public static final int ATTEND_POINTS = 20;

    private Rules() {
    }

    public static LocalDate appDay(LocalDateTime time) {
        return time.minusHours(4).toLocalDate();
    }

    public static int level(int exp) {
        int level = 1;
        for (int i = 0; i < LEVEL_BASE.length; i++) {
            if (exp >= LEVEL_BASE[i]) {
                level = i + 1;
            }
        }
        return level;
    }

    public static int stage(int level) {
        return level <= 3 ? 1 : level <= 6 ? 2 : 3;
    }

    /** pet 화면의 "경험치 70 / 110". 레벨 10이면 "최고 레벨". */
    public static String expText(int exp) {
        int level = level(exp);
        if (level == 10) {
            return "최고 레벨";
        }
        int base = LEVEL_BASE[level - 1];
        return "경험치 " + (exp - base) + " / " + (LEVEL_BASE[level] - base);
    }

    public static double expRatio(int exp) {
        int level = level(exp);
        if (level == 10) {
            return 1.0;
        }
        int base = LEVEL_BASE[level - 1];
        return (exp - base) / (double) (LEVEL_BASE[level] - base);
    }

    /** 이름 마지막 글자가 받침 있는 한글이면 "이야", 그 밖은 "야". */
    public static String iya(String name) {
        return hasBatchim(name) ? "이야" : "야";
    }

    /** 마지막 글자를 읽을 때 받침이 있는지. 한글과 숫자(영, 일, 삼, 육, 칠, 팔)만 본다. */
    public static boolean hasBatchim(String word) {
        char last = word.charAt(word.length() - 1);
        if (last >= 0xAC00 && last <= 0xD7A3) {
            return (last - 0xAC00) % 28 != 0;
        }
        return "013678".indexOf(last) >= 0;
    }

    /** 15자를 넘으면 앞 14자 뒤에 "…". */
    public static String shortTitle(String title) {
        return title.length() > 15 ? title.substring(0, 14) + "…" : title;
    }
}
