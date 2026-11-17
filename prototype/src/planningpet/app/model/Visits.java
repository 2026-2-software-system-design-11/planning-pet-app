package planningpet.app.model;

import java.time.LocalDate;

/** 06의 12번. 연속 접속. 15번(창이 보일 때)만 부른다. */
public final class Visits {
    private Visits() {
    }

    /** 브리핑 직전에 부른다. 갱신한 연속 접속 일수를 돌려준다. */
    public static int recordOpen(Store s) {
        LocalDate today = s.today();
        if (today.minusDays(1).equals(s.lastOpenDay)) {
            s.streak++;
        } else if (!today.equals(s.lastOpenDay)) {
            s.streak = 1;
        }
        s.lastOpenDay = today;
        return s.streak;
    }
}
