package planningpet.schedule.model;

import planningpet.app.model.Store;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 자동 배치(06의 18번)와 시간표 열 나누기(06의 19번). */
public final class Planner {
    /** 놓은 할 일과 놓은 날, 시작 시각. 기간 할 일은 적용하면 날짜가 day로 바뀐다(D-65). */
    public record Placed(Task task, LocalDate day, LocalTime start) {
    }

    public record Placement(List<Placed> placed, List<Task> unplaced) {
    }

    private Planner() {
    }

    /**
     * 놓을 수 있는 날(06의 18번): 보이는 주의 날 가운데 오늘과 할 일의 날짜 중 늦은 날부터 끝 날까지. 끝 날은 기간 할 일이면
     * 마감(마감이 날짜보다 앞이면 날짜), 아니면 날짜다(D-25, D-65).
     */
    public static List<LocalDate> days(Task t, LocalDate monday, LocalDate today) {
        LocalDate from = t.date.isAfter(today) ? t.date : today;
        LocalDate end = t.dueDate != null && t.dueDate.isAfter(t.date) ? t.dueDate : t.date;
        List<LocalDate> out = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate d = monday.plusDays(i);
            if (!d.isBefore(from) && !d.isAfter(end)) {
                out.add(d);
            }
        }
        return out;
    }

    /** 대상: 시작 시각 없는, 끝내지 않은 할 일 가운데 놓을 수 있는 날이 있는 것. 날짜, 긴 순, 넣은 순. */
    public static List<Task> targets(Store s, LocalDate monday) {
        return s.tasks.stream()
                .filter(t -> t.kind == Task.Kind.TASK && !t.onTimetable(s.today()) && !t.done
                        && !days(t, monday, s.today()).isEmpty())
                .sorted(Comparator.comparing((Task t) -> t.date)
                        .thenComparing(t -> -t.minutes)
                        .thenComparingLong(t -> t.seq))
                .toList();
    }

    /** 지금을 5분 단위로 올린 시각(D-38). 이보다 앞에는 놓지 않는다. */
    public static LocalDateTime earliest(Store s) {
        LocalDateTime now = s.now.truncatedTo(ChronoUnit.MINUTES).plusMinutes(s.now.getSecond() > 0 ? 1 : 0);
        return now.plusMinutes((5 - now.getMinute() % 5) % 5);
    }

    public static Placement place(Store s, LocalDate monday) {
        LocalDateTime earliest = earliest(s);
        List<Placed> placed = new ArrayList<>();
        List<Task> unplaced = new ArrayList<>();
        for (Task t : targets(s, monday)) {
            Placed found = null;
            for (LocalDate day : days(t, monday, s.today())) {
                found = slot(s, t, day, placed, earliest);
                if (found != null) {
                    break;
                }
            }
            if (found == null) {
                unplaced.add(t);
            } else {
                placed.add(found);
            }
        }
        return new Placement(placed, unplaced);
    }

    /** 그날 09:00부터 5분씩 늦추며 처음으로 비는 자리. 없으면 null. */
    private static Placed slot(Store s, Task t, LocalDate day, List<Placed> placed, LocalDateTime earliest) {
        List<int[]> busy = new ArrayList<>();
        s.book.timedOn(day).forEach(x -> busy.add(new int[] {x.startMinute(), x.endMinute()}));
        placed.stream().filter(p -> p.day().equals(day)).forEach(p -> {
            int from = p.start().getHour() * 60 + p.start().getMinute();
            busy.add(new int[] {from, from + p.task().minutes});
        });
        for (int m = 9 * 60; m + t.minutes <= 24 * 60; m += 5) {
            int start = m;
            boolean free = busy.stream().noneMatch(b -> start < b[1] && b[0] < start + t.minutes);
            if (free && !day.atTime(m / 60, m % 60).isBefore(earliest)) {
                return new Placed(t, day, LocalTime.of(m / 60, m % 60));
            }
        }
        return null;
    }

    /** 일정마다 {열 번호, 무리의 열 수}. 겹침으로 이어진 일정끼리 한 무리다. */
    public static Map<Task, int[]> columns(List<Task> dayTasks) {
        List<Task> sorted = dayTasks.stream()
                .sorted(Comparator.comparingInt(Task::startMinute)
                        .thenComparing(t -> -t.minutes)
                        .thenComparingLong(t -> t.seq))
                .toList();
        Map<Task, int[]> result = new HashMap<>();
        List<Task> cluster = new ArrayList<>();
        List<Integer> columnEnds = new ArrayList<>();
        int clusterEnd = -1;
        for (Task t : sorted) {
            if (t.startMinute() >= clusterEnd) {
                cluster.forEach(c -> result.get(c)[1] = columnEnds.size());
                cluster.clear();
                columnEnds.clear();
            }
            int col = 0;
            while (col < columnEnds.size() && columnEnds.get(col) > t.startMinute()) {
                col++;
            }
            if (col == columnEnds.size()) {
                columnEnds.add(t.endMinute());
            } else {
                columnEnds.set(col, t.endMinute());
            }
            result.put(t, new int[] {col, 0});
            cluster.add(t);
            clusterEnd = Math.max(clusterEnd, t.endMinute());
        }
        cluster.forEach(c -> result.get(c)[1] = columnEnds.size());
        return result;
    }
}
