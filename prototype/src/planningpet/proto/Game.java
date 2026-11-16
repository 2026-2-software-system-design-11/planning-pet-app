package planningpet.proto;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

/** 보상, 출석, 연속 접속, 상점(06의 8, 9, 11, 12, 13, 20번). */
public final class Game {
    /** D-28의 아이템과 가격. 가격 순이다. */
    public static final Map<String, Integer> ITEMS = new LinkedHashMap<>();

    static {
        String[] names = {"쿠션", "화분", "러그", "액자", "조명", "책장", "책상", "소파"};
        int[] prices = {50, 80, 100, 120, 150, 200, 250, 300};
        for (int i = 0; i < names.length; i++) {
            ITEMS.put(names[i], prices[i]);
        }
    }

    private final Store s;

    public Game(Store store) {
        this.s = store;
    }

    public RewardResult complete(Task t) {
        LocalDate today = s.today();
        int before = Rules.level(s.exp);
        if (!today.equals(s.rewardCountDay)) {
            s.rewardCountDay = today;
            s.rewardCount = 0;
        }
        t.done = true;
        if (s.rewardCount >= Rules.DAILY_CAP) {
            return new RewardResult(RewardResult.Outcome.CAPPED, before, before);
        }
        t.rewardPoints = Rules.TASK_POINTS;
        t.rewardExp = Rules.TASK_EXP;
        t.rewardDay = today;
        s.points += t.rewardPoints;
        s.exp += t.rewardExp;
        s.rewardCount++;
        return new RewardResult(RewardResult.Outcome.REWARDED, before, Rules.level(s.exp));
    }

    public void uncomplete(Task t) {
        s.points -= t.rewardPoints;
        s.exp -= t.rewardExp;
        if (s.today().equals(t.rewardDay) && s.today().equals(s.rewardCountDay)) {
            s.rewardCount--;
        }
        t.done = false;
        t.rewardPoints = 0;
        t.rewardExp = 0;
        t.rewardDay = null;
    }

    public boolean attend() {
        if (s.today().equals(s.attendanceDay)) {
            return false;
        }
        s.points += Rules.ATTEND_POINTS;
        s.attendanceDay = s.today();
        return true;
    }

    /** 06의 12번. 브리핑 직전에 부른다. */
    public void recordOpen() {
        LocalDate today = s.today();
        if (today.minusDays(1).equals(s.lastOpenDay)) {
            s.streak++;
        } else if (!today.equals(s.lastOpenDay)) {
            s.streak = 1;
        }
        s.lastOpenDay = today;
    }

    /** 06의 20번. 모자라면 모자란 포인트(양수), 사면 0, 이미 있으면 -1. */
    public int buy(String item) {
        if (s.owned.contains(item)) {
            return -1;
        }
        int price = ITEMS.get(item);
        if (s.points < price) {
            return price - s.points;
        }
        s.points -= price;
        s.owned.add(item);
        s.placed.add(item);
        return 0;
    }
}
