package planningpet.reward.model;

/** 06 8번의 결과. 펫 말풍선은 이 값으로 고른다. outcome은 보상을 받았는지, 하루 상한(D-18)에 걸렸는지다. */
public record RewardResult(int points, int exp, Outcome outcome, Level before, Level after) {
    public enum Outcome { REWARDED, CAPPED }

    public boolean levelUp() {
        return after.getLevel() > before.getLevel();
    }

    public boolean stageUp() {
        return after.getStage() > before.getStage();
    }
}
