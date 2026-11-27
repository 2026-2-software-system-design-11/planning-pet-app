package planningpet.common.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** 앱 날짜(06 1번, D-15). 하루는 새벽 4시에 바뀐다. */
public final class AppDay {
    private AppDay() {
    }

    /** 시각이 속한 앱 날짜. 지금 시각에서 4시간을 뺀 날이다. */
    public static LocalDate of(LocalDateTime time) {
        return time.minusHours(4).toLocalDate();
    }
}
