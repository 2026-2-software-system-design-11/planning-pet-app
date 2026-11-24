package planningpet.schedule.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.LongSupplier;

/** 일정 다루기(06의 3~7번, 26번). 앱 데이터의 일정 목록과 넣은 순 번호를 받아 다룬다. */
public final class TaskBook {
    private final List<Task> tasks;
    private final LongSupplier nextSeq;
    private final java.util.function.Supplier<LocalDate> today;

    /** nextSeq는 앱 데이터의 다음 넣은 순 번호를 하나 꺼내고 늘린다. today는 앱의 오늘이다(못 끝낸 계획, D-67). */
    public TaskBook(List<Task> tasks, LongSupplier nextSeq, java.util.function.Supplier<LocalDate> today) {
        this.tasks = tasks;
        this.nextSeq = nextSeq;
        this.today = today;
    }

    public LocalDate today() {
        return today.get();
    }

    /** 06의 3번. 검사(2번)를 통과한 입력으로 일정을 만든다. 반복이면 반복하는 날마다 사본을 만든다(D-17, D-64). */
    public List<Task> add(TaskDraft d) {
        if (!TaskCheck.check(d, false).isEmpty()) {
            throw new IllegalArgumentException("검사를 통과하지 않은 입력");
        }
        return make(d);
    }

    /** 목 데이터와 검사가 쓰는 줄임. 반복 끝 날짜가 있으면 날짜의 요일 하나로 반복한다. */
    public List<Task> add(Task.Kind kind, String title, LocalDate date, LocalTime start, int minutes,
                          String memo, LocalDate repeatEnd) {
        TaskDraft d = new TaskDraft(kind, title, date, start, minutes, memo);
        return make(repeatEnd == null ? d : d.withRepeat(Repeat.weekly(date.getDayOfWeek()), repeatEnd));
    }

    private List<Task> make(TaskDraft d) {
        List<Task> made = new ArrayList<>();
        String series = d.repeat() == null ? null : UUID.randomUUID().toString();
        List<LocalDate> days = d.repeat() == null ? List.of(d.date())
                : d.repeat().dates(d.date(), d.repeatEnd(), Integer.MAX_VALUE - 1);
        for (LocalDate day : days) {
            boolean period = d.kind() == Task.Kind.TASK && d.repeat() == null && d.dueDate() != null;
            // D-68: 기간 할 일의 시각은 목록과 자동 배치로만 정하므로 시각 없이 만든다.
            Task t = new Task(UUID.randomUUID().toString(), nextSeq.getAsLong(), d.kind(), d.title().strip(), day,
                    period ? null : d.start(), d.minutes());
            t.memo = d.memo();
            t.category = d.category();
            t.seriesId = series;
            t.repeat = d.repeat();
            t.dueDate = period ? d.dueDate() : null;
            t.startDay = period ? day : null;
            tasks.add(t);
            made.add(t);
        }
        return made;
    }

    /**
     * 06의 4번. 제목, 유형, 날짜, 마감, 시작 시각, 걸리는 시간, 메모를 바꾼다. thisAndAfter면 같은 반복의 원래 날짜와 같거나 뒤인
     * 사본에도 넣는다(D-42). 종류가 다르면 IllegalArgumentException. 기간 할 일이면 입력의 날짜는 기간 첫날이고, 시각을 정해 둔
     * 자리(날짜와 시작 시각)는 그대로 둔다(D-68, D-93).
     */
    public void update(Task task, TaskDraft d, boolean thisAndAfter) {
        if (task.kind != d.kind()) {
            throw new IllegalArgumentException("종류는 바꿀 수 없다");
        }
        LocalDate originalDate = task.date;
        task.title = d.title().strip();
        task.category = d.category();
        boolean period = task.kind == Task.Kind.TASK && task.seriesId == null && d.dueDate() != null;
        if (period) {
            if (!(task.period() && task.timed())) {
                task.date = d.date();
                task.start = null;
            }
            task.startDay = d.date();
            task.dueDate = d.dueDate();
        } else {
            task.date = d.date();
            task.start = d.start();
            if (task.seriesId == null) {
                task.dueDate = null;
                task.startDay = null;
            }
        }
        task.minutes = d.minutes();
        task.memo = d.memo();
        if (thisAndAfter) {
            applyToSeries(task, originalDate);
        }
    }

    /**
     * 06의 4번을 시각 고치기 창(D-32)에서 부를 때. 시작 시각과 걸리는 시간만 넣는다. 기간 할 일에 시각을 정하면 날짜를 창을 연
     * 목록의 날(listDay)로 옮기고 기간은 둔다(D-65, D-68).
     */
    public void setTime(Task task, LocalTime start, int minutes, LocalDate listDay, boolean thisAndAfter) {
        if (task.period() && start != null) {
            task.date = listDay;
        }
        task.start = start;
        task.minutes = minutes;
        if (thisAndAfter) {
            applyToSeries(task, task.date, true);
        }
    }

    /** 지운 일정 가운데 보상을 받았던 것. 친밀도(D-49)와 통계 포인트(D-55)가 지워도 줄지 않게 센다(D-19). */
    public final List<Task> deletedRewarded = new ArrayList<>();

    /** 06의 5번. thisAndAfter면 같은 반복의 같거나 뒤 날짜 사본을 모두 지운다. 보상은 그대로 둔다. */
    public void delete(Task task, boolean thisAndAfter) {
        java.util.function.Predicate<Task> gone = thisAndAfter && task.seriesId != null
                ? t -> task.seriesId.equals(t.seriesId) && !t.date.isBefore(task.date)
                : t -> t == task;
        tasks.stream().filter(gone).filter(t -> t.rewardDay != null).forEach(deletedRewarded::add);
        tasks.removeIf(gone);
    }

    /** 보상 기록: 남은 일정과 지운 일정 가운데 보상을 받은 것. */
    public List<Task> rewardHistory() {
        List<Task> all = new ArrayList<>(deletedRewarded);
        tasks.stream().filter(t -> t.rewardDay != null).forEach(all::add);
        return all;
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
                    t.category = edited.category;
                }
            }
        }
    }

    /** 반복의 마지막 날짜. 반복 사본이 아니면 그 날짜. */
    public LocalDate seriesEnd(Task task) {
        return tasks.stream().filter(t -> task.seriesId != null && task.seriesId.equals(t.seriesId)).map(t -> t.date)
                .max(LocalDate::compareTo).orElse(task.date);
    }

    /** 시각 있는 일을 시각 순으로 먼저, 없는 일을 넣은 순으로(06의 6번과 26번의 순서). */
    private Comparator<Task> order() {
        LocalDate day = today();
        return Comparator.comparing((Task t) -> !t.onTimetable(day))
                .thenComparing(t -> t.onTimetable(day) ? t.start : LocalTime.MIN)
                .thenComparingLong(t -> t.seq);
    }

    /** 그날의 "오늘 할 일"(할 일과 고정 일정, D-35). 기간 안에 할 일 묶음은 세지 않는다(D-70). 성취도 막대와 남은 수가 센다. */
    public List<Task> checkables(LocalDate date) {
        DayPlan plan = dayPlan(date);
        List<Task> all = new ArrayList<>(plan.dueToday());
        all.addAll(plan.timed());
        all.addAll(plan.untimed());
        return all;
    }

    /**
     * 06의 6번(D-31, D-70). 메인 목록의 하루 시간표: 맨 위 "오늘 마감"인 시각 없는 기간 할 일, 시각 있는 할 일과 고정 일정, 그 아래
     * 시각 없는 할 일, 맨 아래 "기간 안에 할 일"(그날이 기간 안이고 시각이 없는 끝내지 않은 기간 할 일).
     */
    public record DayPlan(LocalDate date, List<Task> dueToday, List<Task> timed, List<Task> untimed, List<Task> period) {
        public boolean isEmpty() {
            return dueToday.isEmpty() && timed.isEmpty() && untimed.isEmpty() && period.isEmpty();
        }
    }

    public DayPlan dayPlan(LocalDate date) {
        LocalDate now = today();
        List<Task> timed = tasks.stream()
                .filter(t -> t.onTimetable(now) && t.date.equals(date))
                .sorted(Comparator.comparing((Task t) -> t.start).thenComparingLong(t -> t.seq))
                .toList();
        List<Task> rest = tasks.stream()
                .filter(t -> t.kind == Task.Kind.TASK && !t.onTimetable(now) && t.shownOn(date, now))
                .sorted(Comparator.comparingLong(t -> t.seq))
                .toList();
        List<Task> due = rest.stream().filter(t -> t.period() && !t.done && t.dueDate.equals(date)).toList();
        List<Task> period = rest.stream().filter(t -> t.period() && !t.done && t.dueDate.isAfter(date)).toList();
        List<Task> untimed = rest.stream().filter(t -> !due.contains(t) && !period.contains(t)).toList();
        return new DayPlan(date, due, timed, untimed, period);
    }

    /** 펫 카드 막대 아래 글(D-70): "기간 할 일 {수}개 (~{월}/{일})", 가장 가까운 마감. 오늘 기간 안에 할 일이 없으면 null. */
    public String periodNote() {
        List<Task> list = dayPlan(today()).period();
        return list.stream().map(t -> t.dueDate).min(LocalDate::compareTo)
                .map(d -> "기간 할 일 " + list.size() + "개 (~" + d.getMonthValue() + "/" + d.getDayOfMonth() + ")")
                .orElse(null);
    }

    /** 06의 26번(D-33). 달력 칸의 첫 할 일 제목과 끝내지 않은 일이 있는지. 세는 날이 그날인 할 일이 없으면 null. */
    public record CellPreview(String title, boolean open) {
    }

    public CellPreview cellPreview(LocalDate date) {
        List<Task> list = tasks.stream()
                .filter(t -> t.kind == Task.Kind.TASK && t.countDay().equals(date))
                .sorted(order())
                .toList();
        if (list.isEmpty()) {
            return null;
        }
        String title = list.get(0).title;
        if (title.length() > 5) {
            title = title.substring(0, 5).stripTrailing() + "…";
        }
        return new CellPreview(title, list.stream().anyMatch(t -> !t.done));
    }

    /** 마감이 있는 할 일의 "~11/22까지"(06의 6번). 마감이 없으면 null. */
    public static String dueText(Task t) {
        return t.dueDate == null ? null : "~" + t.dueDate.getMonthValue() + "/" + t.dueDate.getDayOfMonth() + "까지";
    }

    /** "10:00~11:00". 자정에 끝나면 "24:00". */
    public static String timeText(Task t) {
        int end = t.endMinute();
        return t.start + "~" + String.format("%02d:%02d", end / 60, end % 60);
    }

    /** 06의 7번. 일정관리 오른쪽 목록: 그 주에 보이는 시각 없는 할 일을 한 번씩, 줄의 날짜 순, 같은 날은 넣은 순. */
    public List<Task> untimedOfWeek(LocalDate monday) {
        LocalDate now = today();
        return tasks.stream()
                .filter(t -> t.kind == Task.Kind.TASK && !t.onTimetable(now) && rowDay(t, monday, now) != null)
                .sorted(Comparator.comparing((Task t) -> rowDay(t, monday, now)).thenComparingLong(t -> t.seq))
                .toList();
    }

    /** 일정관리 목록 줄의 날짜: 그 주에서 보이는 첫날. 그 주에 보이지 않으면 null(D-65). */
    public static LocalDate rowDay(Task t, LocalDate monday, LocalDate today) {
        for (int i = 0; i < 7; i++) {
            if (t.shownOn(monday.plusDays(i), today)) {
                return monday.plusDays(i);
            }
        }
        return null;
    }

    public List<Task> timedOn(LocalDate date) {
        LocalDate now = today();
        return tasks.stream().filter(t -> t.onTimetable(now) && t.date.equals(date)).toList();
    }

    public static LocalDate mondayOf(LocalDate date) {
        return date.with(DayOfWeek.MONDAY);
    }
}
