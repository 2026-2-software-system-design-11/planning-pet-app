package planningpet.schedule.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/** 06의 14번. 일정 입력 창과 시각 고치기 창의 기본값. */
public final class DefaultDates {
    private DefaultDates() {
    }

    /** 일정관리에서 추가할 때: 보이는 주에 오늘이 있으면 오늘, 없으면 그 주 월요일. */
    public static LocalDate forSchedule(LocalDate shownMonday, LocalDate today) {
        return TaskBook.mondayOf(today).equals(shownMonday) ? today : shownMonday;
    }

    /** 오늘이면 지금을 5분 단위로 올린 시각과 09:00 중 늦은 것(23:55까지), 다른 날이면 09:00. */
    public static LocalTime start(LocalDate date, LocalDateTime now) {
        if (!date.equals(now.toLocalDate())) {
            return LocalTime.of(9, 0);
        }
        int m = now.getHour() * 60 + now.getMinute() + (now.getSecond() > 0 ? 1 : 0);
        m = Math.min(Math.max((m + 4) / 5 * 5, 9 * 60), 23 * 60 + 55);
        return LocalTime.of(m / 60, m % 60);
    }

    /** 반복 끝 날짜(D-17, D-64): 고정 일정은 날짜에 15주, 할 일은 4주. */
    public static LocalDate repeatEnd(Task.Kind kind, LocalDate date) {
        return date.plusWeeks(kind == Task.Kind.FIXED ? 15 : 4);
    }

    /** 마감 기본값(D-65): 그 날짜가 든 주(월요일부터 일요일)의 일요일. */
    public static LocalDate due(LocalDate date) {
        return date.with(java.time.temporal.TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SUNDAY));
    }
}
