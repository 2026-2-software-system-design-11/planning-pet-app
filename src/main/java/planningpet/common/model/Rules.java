package planningpet.common.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

/** 여러 도메인이 쓰는 날짜 규칙. 한 주(06 데이터 절, D-91). */
public final class Rules {
    private Rules() {
    }

    /** 그 날짜가 든 주의 첫날. 한 주는 weekStart 요일부터 7일이다. */
    public static LocalDate weekStart(LocalDate date, DayOfWeek weekStart) {
        return date.with(TemporalAdjusters.previousOrSame(weekStart));
    }
}
