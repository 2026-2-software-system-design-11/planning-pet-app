package planningpet.proto;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** 메모리에만 있는 앱 데이터(06 "데이터" 표의 AppData)와 일정 다루기(06의 3~7번). 저장하지 않는다. */
public final class Store {
    public final List<Task> tasks = new ArrayList<>();
    public LocalDateTime now;
    public int points;
    public int exp;
    public LocalDate rewardCountDay;
    public int rewardCount;
    public LocalDate attendanceDay;
    public LocalDate lastOpenDay;
    public int streak;
    public String petKind = "CAT";
    public String petName = "나비";
    public final Set<String> owned = new LinkedHashSet<>();
    public final Set<String> placed = new LinkedHashSet<>();
    public String talk = "";
    private long nextSeq = 1;
    private final List<Runnable> listeners = new ArrayList<>();

    public LocalDate today() {
        return Rules.appDay(now);
    }

    public void onChange(Runnable listener) {
        listeners.add(listener);
    }

    public void changed() {
        listeners.forEach(Runnable::run);
    }

    /** 06의 3번. 반복 끝 날짜가 있으면 같은 요일마다 사본을 만든다. */
    public List<Task> add(Task.Kind kind, String title, LocalDate date, LocalTime start, int minutes,
                          String memo, LocalDate repeatEnd) {
        List<Task> made = new ArrayList<>();
        String series = repeatEnd == null ? null : UUID.randomUUID().toString();
        LocalDate last = repeatEnd == null ? date : repeatEnd;
        for (LocalDate d = date; !d.isAfter(last); d = d.plusWeeks(1)) {
            Task t = new Task(UUID.randomUUID().toString(), nextSeq++, kind, title.strip(), d, start, minutes);
            t.memo = memo;
            t.seriesId = series;
            tasks.add(t);
            made.add(t);
        }
        return made;
    }

    /** 06의 5번. thisAndAfter면 같은 반복의 같거나 뒤 날짜 사본을 모두 지운다. 보상은 그대로 둔다. */
    public void delete(Task task, boolean thisAndAfter) {
        if (thisAndAfter && task.seriesId != null) {
            tasks.removeIf(t -> task.seriesId.equals(t.seriesId) && !t.date.isBefore(task.date));
        } else {
            tasks.remove(task);
        }
    }

    /** 06의 6번. 메인 목록: 시각 있는 할 일을 시각 순으로 먼저, 없는 할 일을 넣은 순으로. */
    public List<Task> forDate(LocalDate date) {
        return tasks.stream()
                .filter(t -> t.kind == Task.Kind.TASK && t.date.equals(date))
                .sorted(Comparator.comparing((Task t) -> !t.timed())
                        .thenComparing(t -> t.timed() ? t.start : LocalTime.MIN)
                        .thenComparingLong(t -> t.seq))
                .toList();
    }

    /** 06의 7번. 일정관리 오른쪽 목록. */
    public List<Task> untimedOfWeek(LocalDate monday) {
        return tasks.stream()
                .filter(t -> t.kind == Task.Kind.TASK && !t.timed())
                .filter(t -> !t.date.isBefore(monday) && !t.date.isAfter(monday.plusDays(6)))
                .sorted(Comparator.comparing((Task t) -> t.date).thenComparingLong(t -> t.seq))
                .toList();
    }

    public List<Task> timedOn(LocalDate date) {
        return tasks.stream().filter(t -> t.timed() && t.date.equals(date)).toList();
    }

    public static LocalDate mondayOf(LocalDate date) {
        return date.with(DayOfWeek.MONDAY);
    }
}
