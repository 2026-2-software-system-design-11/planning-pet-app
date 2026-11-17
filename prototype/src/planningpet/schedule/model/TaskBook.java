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

    /** D-42, 06의 4번 THIS_AND_AFTER. 고친 사본의 제목, 시작 시각, 걸리는 시간, 메모를 원래 날짜와 같거나 뒤인 같은 반복 사본에 넣는다. */
    public void applyToSeries(Task edited, LocalDate originalDate) {
        applyToSeries(edited, originalDate, false);
    }

    /** timeOnly면 시작 시각과 걸리는 시간만 넣는다(시각 고치기 창, D-42). */
    public void applyToSeries(Task edited, LocalDate originalDate, boolean timeOnly) {
        for (Task t : tasks) {
            if (t != edited && edited.seriesId != null && edited.seriesId.equals(t.seriesId) && !t.date.isBefore(originalDate)) {
                t.start = edited.start;
                t.minutes = edited.minutes;
                if (!timeOnly) {
                    t.title = edited.title;
                    t.memo = edited.memo;
                }
            }
        }
    }

    /** 반복의 마지막 날짜. 반복 사본이 아니면 그 날짜. */
    public LocalDate seriesEnd(Task task) {
        return tasks.stream().filter(t -> task.seriesId != null && task.seriesId.equals(t.seriesId)).map(t -> t.date)
                .max(LocalDate::compareTo).orElse(task.date);
    }
}
