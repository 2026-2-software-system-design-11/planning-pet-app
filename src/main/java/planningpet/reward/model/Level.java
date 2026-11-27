package planningpet.reward.model;

/** 한 펫의 누적 경험치에서 구한 레벨과 다음 레벨까지의 진행(06 10번). 펫마다 따로 구한다(D-48). */
public final class Level {
    /** 각 레벨이 되는 누적 경험치와 모습 단계(stage)는 D-20이다. 레벨 10이면 다음 레벨까지의 폭(span)이 0이다. */
    private static final int[] BASE = {0, 50, 130, 240, 380, 550, 750, 990, 1270, 1600};

    private final int level;
    private final int stage;
    private final int inLevel;
    private final int span;

    private Level(int level, int stage, int inLevel, int span) {
        this.level = level;
        this.stage = stage;
        this.inLevel = inLevel;
        this.span = span;
    }

    /** 경험치보다 크지 않은 가장 큰 기준의 레벨. 0보다 작으면 레벨 1이다(docs/porting/rewards.md E-LVL-5). */
    public static Level of(int exp) {
        int level = 1;
        for (int i = 0; i < BASE.length; i++) {
            if (exp >= BASE[i]) {
                level = i + 1;
            }
        }
        int span = level == BASE.length ? 0 : BASE[level] - BASE[level - 1];
        return new Level(level, level <= 3 ? 1 : level <= 6 ? 2 : 3, exp - BASE[level - 1], span);
    }

    public int getLevel() { return level; }
    public int getStage() { return stage; }
    public int getInLevel() { return inLevel; }
    public int getSpan() { return span; }

    /** 나의 방과 캐릭터 창의 "경험치 {모은 값} / {폭}". 레벨 10이면 "최고 레벨". */
    public String text() {
        return span == 0 ? "최고 레벨" : "경험치 " + inLevel + " / " + span;
    }

    /** 경험치 막대가 찬 비율. 레벨 10이면 가득 찬다. */
    public double ratio() {
        return span == 0 ? 1.0 : inLevel / (double) span;
    }
}
