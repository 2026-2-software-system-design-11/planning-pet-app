package planningpet.stats.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import planningpet.common.model.Rules;

/** 통계의 기간(06 29번 입력). 두 끝을 모두 넣는다. 주간은 한 주(D-91), 월간은 그 달 1일부터 마지막 날이다. */
public record Period(LocalDate from, LocalDate to, boolean monthly) {
    /** any가 든 한 주. 한 주는 first 요일부터 7일이다. */
    public static Period week(LocalDate any, DayOfWeek first) {
        LocalDate start = Rules.weekStart(any, first);
        return new Period(start, start.plusDays(6), false);
    }

    /** any가 든 달. */
    public static Period month(LocalDate any) {
        LocalDate first = any.withDayOfMonth(1);
        return new Period(first, first.with(TemporalAdjusters.lastDayOfMonth()), true);
    }

    /** 같은 종류로 한 주나 한 달씩 앞(-1)이나 뒤(+1)로 옮긴 기간. 주간은 이 기간 첫날의 요일을 한 주 시작으로 쓴다. */
    public Period step(int delta) {
        return monthly ? month(from.plusMonths(delta)) : week(from.plusWeeks(delta), from.getDayOfWeek());
    }

    public boolean contains(LocalDate day) {
        return !day.isBefore(from) && !day.isAfter(to);
    }
}
