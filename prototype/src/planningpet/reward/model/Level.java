package planningpet.reward.model;

/** 06의 10번 레벨. */
public final class Level {
    private static final int[] LEVEL_BASE = {0, 50, 130, 240, 380, 550, 750, 990, 1270, 1600};

    private Level() {
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
}
