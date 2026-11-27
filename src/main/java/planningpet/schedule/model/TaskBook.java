package planningpet.schedule.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

/** 일정 다루기(06 3번, 6번, 7번). 앱 데이터의 일정 목록, 넣은 순 번호, 앱의 오늘을 받아 다룬다. */
public final class TaskBook {
    private final List<Task> tasks;
    private final LongSupplier nextSeq;
    private final Supplier<LocalDate> today;

    /** nextSeq는 앱 데이터의 다음 넣은 순 번호를 하나 꺼내고 늘린다. today는 앱의 오늘이다(못 끝낸 계획, D-67). */
    public TaskBook(List<Task> tasks, LongSupplier nextSeq, Supplier<LocalDate> today) {
        this.tasks = new ArrayList<>(tasks);
        this.nextSeq = nextSeq;
        this.today = today;
    }

    /** 넣은 일정 모두. 바꿀 수 없는 목록이다. */
    public List<Task> getTasks() {
        return Collections.unmodifiableList(tasks);
    }

    /**
     * 06 3번. 검사(2번)를 통과한 입력으로 일정을 만든다. 반복이면 반복하는 날마다 같은 seriesId의 사본을 날짜 순으로 만든다(D-17,
     * D-64). 통과하지 않았거나 수정 창의 값(반복 칸을 검사하지 않은 값, D-42)이면 IllegalArgumentException.
     */
    public List<Task> add(TaskDraft draft) {
        if (draft.editing() || !draft.validate().isEmpty()) {
            throw new IllegalArgumentException("검사를 통과하지 않은 입력");
        }
        String series = draft.repeat() == null ? null : UUID.randomUUID().toString();
        List<LocalDate> days = draft.repeat() == null ? List.of(draft.date())
                : draft.repeat().dates(draft.date(), draft.repeatEnd(), Integer.MAX_VALUE - 1);
        List<Task> made = new ArrayList<>();
        for (LocalDate day : days) {
            made.add(make(draft, day, series));
        }
        tasks.addAll(made);
        return made;
    }

    /**
     * 06 3번 겹침 묻기(D-87). 입력이 들어갈 날(반복이면 사본마다)과 그 앞날, 다음 날의 시간표 일정(self는 빼고) 가운데 실제 시각으로
     * 겹치는 것(D-94). 날이 가장 이른, 그 안에서 실제 시작이 가장 이르고 넣은 순이 앞인 하나다. 시작 시각이 없거나 기간 할 일이면 null. 수정 창(self가 있음)은 그 날짜만 본다(D-42).
     */
    public Overlap overlap(TaskDraft draft, Task self) {
        Integer start = draft.startMinute();
        boolean period = draft.kind() == Kind.TASK && draft.repeat() == null && draft.dueDate() != null;
        if (start == null || period) {
            return null;
        }
        List<LocalDate> days = draft.repeat() == null || self != null ? List.of(draft.date())
                : draft.repeat().dates(draft.date(), draft.repeatEnd(), Integer.MAX_VALUE - 1);
        for (LocalDate day : days) {
            Overlap o = overlapOn(day, start, draft.durationMinutes(), self);
            if (o != null) {
                return o;
            }
        }
        return null;
    }

    /** 한 날의 겹침. 두 일정은 서로의 끝이 서로의 시작보다 늦을 때만 겹친다(18번과 같다). */
    private Overlap overlapOn(LocalDate day, int start, int minutes, Task self) {
        int end = start + minutes;
        // D-94: 실제 시각으로 본다. 앞날 일정은 24시간을 빼고, 다음 날 일정은 더해 잰다.
        Overlap best = null;
        int bestStart = Integer.MAX_VALUE;
        for (int shift = -1; shift <= 1; shift++) {
            int off = shift * Task.DAY;
            for (Task t : timedOn(day.plusDays(shift))) {
                int s = t.startMinute() + off;
                int e = t.endMinute() + off;
                boolean earlier = s < bestStart || s == bestStart && t.getSeq() < best.other().getSeq();
                if (t != self && s < end && start < e && earlier) {
                    best = new Overlap(t, Math.min(end, e) - Math.max(start, s), shift == 0);
                    bestStart = s;
                }
            }
        }
        return best;
    }

    /**
     * 06 4번의 수정 창 저장. 제목, 유형, 날짜, 마감, 시작 시각, 걸리는 시간, 메모만 바꾸고 id, seq, 반복, 끝냄, 보상은 그대로 둔다(D-18).
     * 기간 할 일이면 입력의 날짜는 기간 첫날이고, 시각을 정해 둔 자리(날짜와 시작 시각)는 그대로 둔다(D-68, D-93). 반복 사본은 기간
     * 할 일이 되지 않는다(D-65). THIS_AND_AFTER면 같은 반복에서 원래 날짜와 같거나 뒤인 끝내지 않은 사본에 제목, 유형, 시작 시각,
     * 걸리는 시간, 메모를 넣는다(D-42, D-54). 종류가 다르면 IllegalArgumentException.
     */
    public void update(Task task, TaskDraft draft, EditScope scope) {
        if (task.getKind() != draft.kind()) {
            throw new IllegalArgumentException("종류는 바꿀 수 없다");
        }
        if (draft.timeOnly()) {
            updateTime(task, draft, scope);
            return;
        }
        LocalDate originalDate = task.getDate();
        task.setTitle(draft.title().strip());
        task.setCategory(draft.category());
        boolean period = task.getKind() == Kind.TASK && task.getSeriesId() == null && draft.dueDate() != null;
        if (period) {
            if (!(task.period() && task.timed())) {
                task.setDate(draft.date());
                task.setStart(null, false);
            }
            task.setStartDay(draft.date());
            task.setDueDate(draft.dueDate());
        } else {
            task.setDate(draft.date());
            task.setStart(draft.startTime(), draft.nextDay());
            if (task.getSeriesId() == null) {
                task.setStartDay(null);
                task.setDueDate(null);
            }
        }
        task.setDurationMinutes(draft.durationMinutes());
        task.setMemo(draft.memo());
        if (scope == EditScope.THIS_AND_AFTER) {
            for (Task t : laterCopies(task, originalDate)) {
                t.setStart(task.getStartTime(), task.isNextDay());
                t.setDurationMinutes(task.getDurationMinutes());
                t.setTitle(task.getTitle());
                t.setMemo(task.getMemo());
                t.setCategory(task.getCategory());
            }
        }
    }

    /**
     * 06 4번을 시각 고치기 창(D-32)에서 부를 때. 시작 시각과 걸리는 시간만 넣고 날짜를 값의 날짜로 옮긴다. 기간 할 일은 창을 연 목록의
     * 날로 옮기고 기간은 둔다(D-65, D-68). THIS_AND_AFTER면 끝내지 않은 뒤 사본에 시작 시각과 걸리는 시간을 넣고, 고친 사본이
     * 옮긴 날 수("밤 4시", D-98)만큼 옮긴다(D-42).
     */
    private void updateTime(Task task, TaskDraft draft, EditScope scope) {
        LocalDate originalDate = task.getDate();
        if (!task.period() || draft.startTime() != null) {
            task.setDate(draft.date());
        }
        task.setStart(draft.startTime(), draft.nextDay());
        task.setDurationMinutes(draft.durationMinutes());
        if (scope == EditScope.THIS_AND_AFTER) {
            long days = ChronoUnit.DAYS.between(originalDate, task.getDate());
            for (Task t : laterCopies(task, originalDate)) {
                t.setStart(task.getStartTime(), task.isNextDay());
                t.setDurationMinutes(task.getDurationMinutes());
                t.setDate(t.getDate().plusDays(days));
            }
        }
    }

    /** task와 같은 반복에서 날짜가 from과 같거나 뒤인 끝내지 않은 다른 사본(D-42). 반복 사본이 아니면 없다. */
    private List<Task> laterCopies(Task task, LocalDate from) {
        String series = task.getSeriesId();
        return tasks.stream().filter(t -> t != task && !t.isDone() && series != null
                && series.equals(t.getSeriesId()) && !t.getDate().isBefore(from)).toList();
    }

    /**
     * 06 6번. date의 시간표 일정은 앱 하루의 시각 순(같으면 넣은 순)으로 timed에 둔다. 그날 보이는(Task.shownOn) 그 밖의 할 일은 넣은
     * 순으로, 끝내지 않은 기간 할 일 가운데 마감이 그날이면 dueToday, 뒤면 period, 나머지는 untimed다(D-70).
     */
    public DayPlan dayPlan(LocalDate date) {
        LocalDate now = today.get();
        List<Task> timed = tasks.stream()
                .filter(t -> t.onTimetable(now) && t.getDate().equals(date))
                .sorted(Comparator.comparingInt(Task::startMinute).thenComparingLong(Task::getSeq))
                .toList();
        List<Task> rest = tasks.stream()
                .filter(t -> t.getKind() == Kind.TASK && !t.onTimetable(now) && t.shownOn(date, now))
                .sorted(Comparator.comparingLong(Task::getSeq))
                .toList();
        List<Task> due = rest.stream().filter(t -> t.period() && !t.isDone() && t.getDueDate().equals(date)).toList();
        List<Task> period = rest.stream().filter(t -> t.period() && !t.isDone() && t.getDueDate().isAfter(date)).toList();
        List<Task> untimed = rest.stream().filter(t -> !due.contains(t) && !period.contains(t)).toList();
        return new DayPlan(date, due, timed, untimed, period);
    }

    /**
     * 06 7번. 일정관리 오른쪽 목록: weekStart부터 7일 동안 보이는 시각 없는 할 일을 한 줄씩(줄의 날짜는 rowDay, D-105). 끝내지 않은
     * 기간 할 일은 마감 날이 그 주에 있고 줄의 날짜와 다르면 마감 날에 한 줄을 더한다(D-93). 줄의 날짜 순, 같은 날은 넣은 순.
     */
    public List<WeekRow> untimedOfWeek(LocalDate weekStart) {
        LocalDate now = today.get();
        List<WeekRow> rows = new ArrayList<>();
        tasks.stream().filter(t -> t.getKind() == Kind.TASK && !t.onTimetable(now)).forEach(t -> {
            LocalDate day = rowDay(t, weekStart, now);
            if (day == null) {
                return;
            }
            rows.add(new WeekRow(t, day));
            LocalDate due = t.getDueDate();
            boolean dueInWeek = t.period() && !t.isDone() && !due.isBefore(weekStart) && due.isBefore(weekStart.plusDays(7));
            if (dueInWeek && !due.equals(day) && t.shownOn(due, now)) {
                rows.add(new WeekRow(t, due));
            }
        });
        rows.sort(Comparator.comparing(WeekRow::day).thenComparingLong(r -> r.task().getSeq()));
        return rows;
    }

    /**
     * 목록 줄의 날짜(D-65, D-105): 그 주에서 보이는 날 가운데 오늘이거나 오늘 뒤인 첫날. 그런 날이 없으면(오늘이 없는 지난 주)
     * 그 주에서 보이는 마지막 날. 그 주에 보이지 않으면 null.
     */
    private static LocalDate rowDay(Task t, LocalDate weekStart, LocalDate today) {
        LocalDate last = null;
        for (int i = 0; i < 7; i++) {
            LocalDate day = weekStart.plusDays(i);
            if (t.shownOn(day, today)) {
                if (!day.isBefore(today)) {
                    return day;
                }
                last = day;
            }
        }
        return last;
    }

    /**
     * 펫 카드 막대 아래 글 "기간 할 일 {수}개 (~{월}/{일})"(06 13번, D-70). today의 기간 안에 할 일 수와 그 가운데 가장 가까운
     * 마감이다. 없으면 null.
     */
    public String periodNote(LocalDate today) {
        List<Task> list = dayPlan(today).period();
        return list.stream().map(Task::getDueDate).min(LocalDate::compareTo)
                .map(d -> "기간 할 일 " + list.size() + "개 (~" + d.getMonthValue() + "/" + d.getDayOfMonth() + ")")
                .orElse(null);
    }

    /**
     * 06 6번의 시각 글 "10:00~11:00". 자정에 끝나면 "24:00", 넘으면 "23:30~01:00(다음 날)", 자정 넘어 시작하면
     * "01:30~02:00(다음 날)"이다(D-77).
     */
    public String timeText(Task task) {
        return span(task.startMinute(), task.endMinute());
    }

    /** 06 6번의 마감 글 "~11/22까지". 마감이 없으면 null. */
    public String dueText(Task task) {
        LocalDate due = task.getDueDate();
        return due == null ? null : "~" + due.getMonthValue() + "/" + due.getDayOfMonth() + "까지";
    }

    /** 앱 하루의 분 start부터 end까지의 시각 글(timeText의 꼴). 겹침 물음 글(Overlap)도 쓴다. */
    static String span(int start, int end) {
        if (start >= Task.DAY) {
            return Task.hhmm(start) + "~" + Task.hhmm(end) + "(다음 날)";
        }
        String last = end == Task.DAY ? "24:00" : Task.hhmm(end) + (end > Task.DAY ? "(다음 날)" : "");
        return Task.hhmm(start) + "~" + last;
    }

    /** 그날 시간표에 놓인 일정. 끝낸 기간 할 일(D-66)과 못 끝낸 계획(D-67)은 빠진다. */
    private List<Task> timedOn(LocalDate date) {
        LocalDate now = today.get();
        return tasks.stream().filter(t -> t.onTimetable(now) && t.getDate().equals(date)).toList();
    }

    /** 06 데이터 절의 처음 값으로 day의 일정 하나를 만든다. 보상과 끝냄은 없다. */
    private Task make(TaskDraft d, LocalDate day, String series) {
        boolean period = d.kind() == Kind.TASK && d.repeat() == null && d.dueDate() != null;
        // D-68: 기간 할 일의 시각은 목록과 자동 배치로만 정하므로 시각 없이 만든다.
        return new Task(UUID.randomUUID().toString(), nextSeq.getAsLong(), d.kind(), d.title().strip(), day,
                period ? null : d.startTime(), d.durationMinutes(), d.memo(),
                !period && d.startTime() != null && d.nextDay(), d.category(), series, d.repeat(),
                period ? d.dueDate() : null, period ? day : null, false, null, 0, 0, null, null);
    }
}
