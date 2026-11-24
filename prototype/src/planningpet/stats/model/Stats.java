package planningpet.stats.model;

import planningpet.app.model.Store;
import planningpet.reward.model.Game;
import planningpet.schedule.model.Categories;
import planningpet.schedule.model.Task;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** D-55 통계. 셀 일정은 기간 안에서 오늘까지인 날의 할 일과 고정 일정이다. */
public final class Stats {
    /** 기간. 두 끝을 모두 넣는다. */
    public record Period(LocalDate from, LocalDate to, boolean monthly) {
        public static Period week(LocalDate any) {
            LocalDate monday = any.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            return new Period(monday, monday.plusDays(6), false);
        }

        public static Period month(LocalDate any) {
            LocalDate first = any.withDayOfMonth(1);
            return new Period(first, first.with(TemporalAdjusters.lastDayOfMonth()), true);
        }

        /** 앞(-1)이나 뒤(+1) 기간. */
        public Period step(int delta) {
            return monthly ? month(from.plusMonths(delta)) : week(from.plusWeeks(delta));
        }

        public boolean contains(LocalDate d) {
            return !d.isBefore(from) && !d.isAfter(to);
        }
    }

    /** 끝낸 수와 셀 일정 수. */
    public record Rate(int done, int total) {
        /** 반올림한 백분율. 셀 일정이 없으면 0. */
        public int percent() {
            return total == 0 ? 0 : Math.round(done * 100f / total);
        }
    }

    /** 추이의 한 칸(주간은 하루, 월간은 한 주). */
    public record Bucket(LocalDate from, LocalDate to, Rate rate) {
    }

    private Stats() {
    }

    private static List<Task> counted(Store s, LocalDate from, LocalDate to) {
        LocalDate end = to.isAfter(s.today()) ? s.today() : to;
        return s.tasks.stream().filter(t -> !t.date.isBefore(from) && !t.date.isAfter(end)).toList();
    }

    private static Rate rate(List<Task> list) {
        return new Rate((int) list.stream().filter(t -> t.done).count(), list.size());
    }

    public static Rate rate(Store s, Period p) {
        return rate(counted(s, p.from(), p.to()));
    }

    /** 주간은 날마다, 월간은 월요일로 끊은 주마다. */
    public static List<Bucket> trend(Store s, Period p) {
        List<Bucket> out = new ArrayList<>();
        LocalDate d = p.from();
        while (!d.isAfter(p.to())) {
            LocalDate end = p.monthly() ? d.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)) : d;
            if (end.isAfter(p.to())) {
                end = p.to();
            }
            out.add(new Bucket(d, end, rate(counted(s, d, end))));
            d = end.plusDays(1);
        }
        return out;
    }

    /** 셀 일정이 있는 유형만, D-54의 순서로. */
    public static Map<String, Rate> byCategory(Store s, Period p) {
        List<Task> list = counted(s, p.from(), p.to());
        Map<String, Rate> out = new LinkedHashMap<>();
        for (String c : Categories.ALL) {
            List<Task> in = list.stream().filter(t -> c.equals(t.category)).toList();
            if (!in.isEmpty()) {
                out.put(c, rate(in));
            }
        }
        return out;
    }

    /** 그 기간에 얻은 포인트: 그 기간에 받은 완료 보상(지운 일정 것도, D-19)과 출석. */
    public static int points(Store s, Period p) {
        int tasks = s.book.rewardHistory().stream().filter(t -> p.contains(t.rewardDay))
                .mapToInt(t -> t.rewardPoints).sum();
        long attend = s.attendDays.stream().filter(p::contains).count();
        return tasks + (int) attend * Game.ATTEND_POINTS;
    }
}
