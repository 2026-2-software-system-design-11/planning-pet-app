package planningpet.schedule.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** 반복 방식(D-64, 06 데이터 표의 repeat). 매일이거나 고른 요일들이다. 같은 반복의 사본은 같은 값을 가진다. */
public record Repeat(boolean daily, Set<DayOfWeek> days) {
    public static final Repeat DAILY = new Repeat(true, Set.of());
    /** 사본 수 한도(D-64). */
    public static final int MAX_COPIES = 120;
    private static final String NAMES = "월화수목금토일";

    public Repeat {
        days = Set.copyOf(days);
    }

    /** 고른 요일들. 하나도 없을 수 있다(06의 2번이 막는다). */
    public static Repeat weekly(java.util.Collection<DayOfWeek> days) {
        return new Repeat(false, Set.copyOf(days));
    }

    public static Repeat weekly(DayOfWeek... days) {
        return new Repeat(false, Set.of(days));
    }

    /** 그 날이 반복하는 날인가. */
    public boolean has(LocalDate date) {
        return daily || days.contains(date.getDayOfWeek());
    }

    /** 시작부터 끝까지 반복하는 날. limit개를 넘으면 limit + 1개에서 멈춘다. */
    public List<LocalDate> dates(LocalDate from, LocalDate to, int limit) {
        List<LocalDate> out = new ArrayList<>();
        for (LocalDate d = from; !d.isAfter(to) && out.size() <= limit; d = d.plusDays(1)) {
            if (has(d)) {
                out.add(d);
            }
        }
        return out;
    }

    /** 06 데이터 표의 값: "DAILY" 또는 "월, 목"처럼 월요일부터. */
    @Override
    public String toString() {
        if (daily) {
            return "DAILY";
        }
        List<String> names = new ArrayList<>();
        for (DayOfWeek d : DayOfWeek.values()) {
            if (days.contains(d)) {
                names.add(String.valueOf(NAMES.charAt(d.getValue() - 1)));
            }
        }
        return String.join(", ", names);
    }
}
