package planningpet.reward.model;


/** 06의 8번이 돌려주는 값. 펫 말풍선은 이 값으로 고른다. */
public record RewardResult(Outcome outcome, int levelBefore, int levelAfter) {
    public enum Outcome { REWARDED, CAPPED }

    public boolean levelUp() {
        return levelAfter > levelBefore;
    }

    public boolean stageUp() {
        return Level.stage(levelAfter) > Level.stage(levelBefore);
    }
}
