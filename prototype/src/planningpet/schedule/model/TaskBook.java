package planningpet.schedule.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/** 일정 다루기(06의 3~7번, 26번). 앱 데이터의 일정 목록을 받아 다룬다. */
public final class TaskBook {
    private final List<Task> tasks;
    private long nextSeq = 1;

    public TaskBook(List<Task> tasks) {
        this.tasks = tasks;
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
}
