package planningpet.schedule.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/** 06 17번. 지금 보낼 일정 시작 알림을 고른다. 시작 10분 전부터 알린다(D-24). */
public final class Reminders {
    private static final Duration BEFORE = Duration.ofMinutes(10);

    private Reminders() {
    }

    /**
     * 시작 시각이 있고 끝내지 않았고 시작 10분 전(포함)부터 시작(빼고)까지 사이이고 sent에 없는 일정을 고르고 sent에 넣는다.
     * 시작 일시 순, 같으면 넣은 순이다(06 6번 timed와 같은 순서).
     */
    public static List<Reminder> due(TaskBook book, LocalDateTime now, Set<ReminderKey> sent) {
        List<Reminder> out = new ArrayList<>();
        for (Task t : book.getTasks().stream().filter(t -> t.timed() && !t.isDone())
                .sorted(Comparator.comparing(Reminders::start).thenComparingLong(Task::getSeq)).toList()) {
            Duration left = Duration.between(now, start(t));
            if (left.isPositive() && left.compareTo(BEFORE) <= 0 && sent.add(new ReminderKey(t.getId(), start(t)))) {
                out.add(new Reminder(t, start(t), (int) Math.ceilDiv(left.toNanos(), Duration.ofMinutes(1).toNanos())));
            }
        }
        return out;
    }

    /** 시작 일시: date의 startTime, nextDay면 다음 날의 startTime(D-77). */
    private static LocalDateTime start(Task t) {
        return t.getDate().plusDays(t.isNextDay() ? 1 : 0).atTime(t.getStartTime());
    }
}
