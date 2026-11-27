package planningpet.app.model;

import java.time.LocalDate;

/** 연속 접속(06 12번, D-21). 그날 처음 창을 열어 브리핑할 때 15번이 부른다. */
public final class Visits {
    private final AppData data;

    public Visits(AppData data) {
        this.data = data;
    }

    /** 마지막으로 브리핑한 날이 없으면 1, 어제면 하나 늘리고, 오늘이면 그대로, 그 밖이면 1. 마지막 날을 today로 하고 일수를 돌려준다. */
    public int recordOpen(LocalDate today) {
        LocalDate last = data.getLastOpenDay();
        if (today.minusDays(1).equals(last)) {
            data.setStreak(data.getStreak() + 1);
        } else if (!today.equals(last)) {
            data.setStreak(1);
        }
        data.setLastOpenDay(today);
        return data.getStreak();
    }
}
