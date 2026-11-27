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
        LocalDateTime earliest = earliest(now);
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
     * 놓지 못한 까닭(D-76). 마감이 보이는 주의 마지막 날보다 뒤인 기간 할 일은 다음 주에 놓을 자리가 있는지 알린다(D-94). 그 밖은
     * 기간 할 일이면 마감까지, 아니면 그날의 빈 시간이 모자란 것이다.
     */
    public static String reason(Task task, TaskBook book, LocalDate weekStart, LocalDateTime now) {
        if (task.period() && task.getDueDate().isAfter(weekStart.plusDays(6))) {
            return nextWeekFits(task, book, weekStart.plusDays(7), now)
                    ? "이번 주엔 자리가 없어요. 마감 " + monthDay(task.getDueDate()) + "까지 시간이 있으니 다음 주에서 자동 배치하면 놓을 수 있어요"
                    : "이번 주엔 자리가 없어요. 다음 주에도 자리가 없어요. 걸리는 시간을 나누거나 다른 일을 옮겨 보세요";
        }
        return task.period() ? "마감까지 빈 시간이 모자라요" : "그날 빈 시간이 모자라요";
    }

    /** 놓지 못한 일의 날짜 글: 기간 할 일은 "마감 11/22(일)", 그 밖은 "날짜 11/22(일)"(06 18번, D-71). */
    public static String dateText(Task task) {
        return task.period() ? "마감 " + monthDay(task.getDueDate()) : "날짜 " + monthDay(task.getDate());
    }

    /** 다음 주(next부터 7일)의 마감까지의 날에, 지금 시간표에 있는 일정만 보고 놓을 자리가 있는가(D-94). */
    private static boolean nextWeekFits(Task task, TaskBook book, LocalDate next, LocalDateTime now) {
        LocalDate today = AppDay.of(now);
        LocalDateTime earliest = earliest(now);
        return days(task, next, today).stream().anyMatch(d -> slot(book, task, d, List.of(), earliest, today) != null);
    }

    /** 지금을 5분 단위로 올린 때. 초가 있으면 그 분을 지난 것으로 본다(D-38). */
    private static LocalDateTime earliest(LocalDateTime now) {
        LocalDateTime m = now.truncatedTo(ChronoUnit.MINUTES).plusMinutes(now.getSecond() > 0 ? 1 : 0);
        return m.plusMinutes((5 - m.getMinute() % 5) % 5);
    }

    private static String monthDay(LocalDate d) {
        return d.getMonthValue() + "/" + d.getDayOfMonth() + "(" + "월화수목금토일".charAt(d.getDayOfWeek().getValue() - 1) + ")";
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
