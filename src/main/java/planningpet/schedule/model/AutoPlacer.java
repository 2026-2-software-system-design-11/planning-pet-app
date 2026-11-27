package planningpet.schedule.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import planningpet.common.model.AppDay;

/** 06 18번. 시각 없는 할 일을 보이는 주의 빈 시간에 놓는 안을 만든다. 저장하지 않는다. */
public final class AutoPlacer {
    private AutoPlacer() {
    }

    /** 대상을 하나씩 놓을 수 있는 날의 이른 날부터 보며 처음으로 비는 자리에 둔다. 오늘은 지금의 앱 날짜다(D-15). */
    public static Placement place(TaskBook book, LocalDate weekStart, LocalDateTime now) {
        LocalDate today = AppDay.of(now);
        // D-38: 지금을 5분 단위로 올린 때보다 앞에는 놓지 않는다. 초가 있으면 그 분을 지난 것으로 본다.
        LocalDateTime m = now.truncatedTo(ChronoUnit.MINUTES).plusMinutes(now.getSecond() > 0 ? 1 : 0);
        LocalDateTime earliest = m.plusMinutes((5 - m.getMinute() % 5) % 5);
        List<Placement.Placed> placed = new ArrayList<>();
        List<Task> unplaced = new ArrayList<>();
        for (Task t : targets(book, weekStart, today)) {
            days(t, weekStart, today).stream()
                    .map(day -> slot(book, t, day, placed, earliest, today))
                    .filter(Objects::nonNull)
                    .findFirst()
                    .ifPresentOrElse(placed::add, () -> unplaced.add(t));
        }
        return new Placement(placed, unplaced);
    }

    /**
     * 놓을 수 있는 날: 보이는 주의 날 가운데 오늘과 할 일의 날짜 중 늦은 날부터 끝 날까지. 끝 날은 기간 할 일이면 마감(마감이 날짜보다
     * 앞이면 날짜), 아니면 날짜다(D-25, D-65).
     */
    private static List<LocalDate> days(Task t, LocalDate weekStart, LocalDate today) {
        LocalDate from = t.getDate().isAfter(today) ? t.getDate() : today;
        LocalDate end = t.period() && t.getDueDate().isAfter(t.getDate()) ? t.getDueDate() : t.getDate();
        return weekStart.datesUntil(weekStart.plusDays(7)).filter(d -> !d.isBefore(from) && !d.isAfter(end)).toList();
    }

    /** 대상: 시간표에 놓이지 않은 끝내지 않은 할 일(못 끝낸 계획 포함, D-67) 가운데 놓을 수 있는 날이 있는 것. */
    private static List<Task> targets(TaskBook book, LocalDate weekStart, LocalDate today) {
        return book.getTasks().stream()
                .filter(t -> t.getKind() == Kind.TASK && !t.onTimetable(today) && !t.isDone()
                        && !days(t, weekStart, today).isEmpty())
                .sorted(Comparator.comparing(Task::getDate)
                        .thenComparing(Comparator.comparingInt(Task::getDurationMinutes).reversed())
                        .thenComparingLong(Task::getSeq))
                .toList();
    }

    /** 그날 09:00부터 5분씩 늦추며, 끝이 24:00 안이고(D-77) 놓인 일정과 겹치지 않는(D-25) earliest 뒤의 첫 자리. 없으면 null. */
    private static Placement.Placed slot(TaskBook book, Task t, LocalDate day, List<Placement.Placed> placed,
            LocalDateTime earliest, LocalDate today) {
        List<int[]> busy = new ArrayList<>();
        book.getTasks().stream().filter(x -> x.onTimetable(today) && x.getDate().equals(day))
                .forEach(x -> busy.add(new int[] {x.startMinute(), x.endMinute()}));
        placed.stream().filter(p -> p.day().equals(day)).forEach(p -> {
            int from = p.start().getHour() * 60 + p.start().getMinute();
            busy.add(new int[] {from, from + p.task().getDurationMinutes()});
        });
        int minutes = t.getDurationMinutes();
        for (int m = 9 * 60; m + minutes <= Task.DAY; m += 5) {
            int start = m;
            boolean free = busy.stream().noneMatch(b -> start < b[1] && b[0] < start + minutes);
            if (free && !day.atTime(m / 60, m % 60).isBefore(earliest)) {
                return new Placement.Placed(t, day, LocalTime.of(m / 60, m % 60));
            }
        }
        return null;
    }
}
