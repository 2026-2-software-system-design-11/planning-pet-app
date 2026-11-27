package planningpet.schedule.model;

import java.time.LocalDate;
import java.time.LocalTime;
import planningpet.pet.model.PetKind;

/**
 * 일정 하나(06 데이터 절 Task). 값은 06 데이터 표 순서대로 생성자에서 모두 받는다. 처음 값은 만드는 쪽이 정한다.
 */
public final class Task {
    /** 하루 칸의 분(24:00). */
    public static final int DAY = 24 * 60;
    /** 하루 칸의 끝(D-77): 다음 날 04:00, 곧 28:00. */
    public static final int DAY_END = 28 * 60;

    /** 분을 "HH:mm"으로. 24시간을 넘으면 다음 날 시각이다. */
    public static String hhmm(int minute) {
        int m = minute % DAY;
        return String.format("%02d:%02d", m / 60, m % 60);
    }

    private final String id;
    private final long seq;
    private Kind kind;
    private String title;
    private LocalDate date;
    private LocalTime startTime;
    private int durationMinutes;
    private String memo;
    private boolean nextDay;
    private Category category;
    private String seriesId;
    private Repeat repeat;
    private LocalDate dueDate;
    private LocalDate startDay;
    private boolean done;
    private LocalDate doneDay;
    private int rewardPoints;
    private int rewardExp;
    private LocalDate rewardDay;
    private PetKind rewardPet;

    public Task(String id, long seq, Kind kind, String title, LocalDate date, LocalTime startTime,
            int durationMinutes, String memo, boolean nextDay, Category category, String seriesId,
            Repeat repeat, LocalDate dueDate, LocalDate startDay, boolean done, LocalDate doneDay, int rewardPoints,
            int rewardExp, LocalDate rewardDay, PetKind rewardPet) {
        this.id = id;
        this.seq = seq;
        this.kind = kind;
        this.title = title;
        this.date = date;
        this.startTime = startTime;
        this.durationMinutes = durationMinutes;
        this.memo = memo;
        this.nextDay = nextDay;
        this.category = category;
        this.seriesId = seriesId;
        this.repeat = repeat;
        this.dueDate = dueDate;
        this.startDay = startDay;
        this.done = done;
        this.doneDay = doneDay;
        this.rewardPoints = rewardPoints;
        this.rewardExp = rewardExp;
        this.rewardDay = rewardDay;
        this.rewardPet = rewardPet;
    }

    public String getId() { return id; }
    public long getSeq() { return seq; }
    public Kind getKind() { return kind; }
    public String getTitle() { return title; }
    public LocalDate getDate() { return date; }
    /** 시작 시각. 없으면 null. */
    public LocalTime getStartTime() { return startTime; }
    public int getDurationMinutes() { return durationMinutes; }
    public String getMemo() { return memo; }
    /** 시작 시각이 date 다음 날 새벽(00:00~03:55)인가(D-77). */
    public boolean isNextDay() { return nextDay; }
    public Category getCategory() { return category; }
    /** 반복 사본끼리 같은 번호. 반복이 아니면 null(D-17, D-64). */
    public String getSeriesId() { return seriesId; }
    /** 반복 방식. 반복 사본이 아니면 null(D-64). */
    public Repeat getRepeat() { return repeat; }
    /** 기간 할 일의 마감. 기간 할 일이 아니면 null(D-65). */
    public LocalDate getDueDate() { return dueDate; }
    /** 기간 할 일의 첫날. 기간 할 일이 아니면 null(D-66). */
    public LocalDate getStartDay() { return startDay; }
    public boolean isDone() { return done; }
    /** 끝낸 앱 날짜. 끝내지 않았으면 null(D-66). */
    public LocalDate getDoneDay() { return doneDay; }
    public int getRewardPoints() { return rewardPoints; }
    public int getRewardExp() { return rewardExp; }
    public LocalDate getRewardDay() { return rewardDay; }
    /** 경험치를 받은 펫의 종류(D-48). 받지 않았으면 null. */
    public PetKind getRewardPet() { return rewardPet; }

    // 06 4번의 고치기. TaskBook만 부르도록 package-private이다(D-109). 시각이 없으면 nextDay는 false다(D-77).
    void setTitle(String title) { this.title = title; }
    void setCategory(Category category) { this.category = category; }
    void setDate(LocalDate date) { this.date = date; }
    void setStart(LocalTime startTime, boolean nextDay) {
        this.startTime = startTime;
        this.nextDay = startTime != null && nextDay;
    }
    void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }
    void setMemo(String memo) { this.memo = memo; }
    void setStartDay(LocalDate startDay) { this.startDay = startDay; }
    void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    /**
     * 끝냄으로 바꾸고 끝낸 앱 날짜와 받은 보상을 적는다(06 8번). Rewards가 부른다(07). 받은 포인트와 경험치가 0이면(하루 상한, D-18)
     * 받은 날은 null이다. 날짜, 시작 시각, 기간은 그대로 둔다(D-66).
     */
    public void finish(LocalDate day, int points, int exp, PetKind pet) {
        done = true;
        doneDay = day;
        rewardPoints = points;
        rewardExp = exp;
        rewardDay = points == 0 && exp == 0 ? null : day;
        rewardPet = pet;
    }

    /** 값이 같은 새 일정. TaskBook.restore가 쓴다. */
    Task copy() {
        return new Task(id, seq, kind, title, date, startTime, durationMinutes, memo, nextDay, category, seriesId,
                repeat, dueDate, startDay, done, doneDay, rewardPoints, rewardExp, rewardDay, rewardPet);
    }

    /** id가 같은 from의 값을 넣는다(06 22번의 되돌리기). id와 seq는 같은 일정이라 그대로다. */
    void setFrom(Task from) {
        kind = from.kind;
        title = from.title;
        date = from.date;
        startTime = from.startTime;
        durationMinutes = from.durationMinutes;
        memo = from.memo;
        nextDay = from.nextDay;
        category = from.category;
        seriesId = from.seriesId;
        repeat = from.repeat;
        dueDate = from.dueDate;
        startDay = from.startDay;
        done = from.done;
        doneDay = from.doneDay;
        rewardPoints = from.rewardPoints;
        rewardExp = from.rewardExp;
        rewardDay = from.rewardDay;
        rewardPet = from.rewardPet;
    }

    public boolean timed() {
        return startTime != null;
    }

    /** 앱 하루의 시각(D-77): date 00:00부터 잰 분. nextDay면 24시간을 더한다. */
    public int startMinute() {
        return (nextDay ? DAY : 0) + startTime.getHour() * 60 + startTime.getMinute();
    }

    public int endMinute() {
        return startMinute() + durationMinutes;
    }

    /** 기간 할 일인가(D-65). */
    public boolean period() {
        return dueDate != null;
    }

    /** 06 데이터 절의 세는 날: 기간 할 일은 끝냈으면 끝낸 날, 아니면 마감(D-65, D-66). 그 밖은 날짜. */
    public LocalDate countDay() {
        if (!period()) {
            return date;
        }
        return done ? doneOn() : dueDate;
    }

    /**
     * 못 끝낸 계획인가(D-67): 끝내지 않은 기간 할 일의 시각을 정한 날이 오늘보다 앞이고 마감보다도 앞이며, 마감이 지나지 않았다.
     * 데이터는 그대로 두고 오늘로 계산한다.
     */
    public boolean missed(LocalDate today) {
        return leftPlan(today) && !dueDate.isBefore(today);
    }

    /** 시각을 정한 날이 지났고 마감보다도 앞인 끝내지 않은 기간 할 일. 마감이 지나도 정한 날로 돌아가지 않는다. */
    private boolean leftPlan(LocalDate today) {
        return period() && !done && timed() && date.isBefore(today) && date.isBefore(dueDate);
    }

    /**
     * 시간표와 목록의 시각 있는 칸에 놓이는가. 끝낸 기간 할 일(D-66), 못 끝낸 계획(D-67), 마감이 지난 옛 계획은 정한 칸에서 빠진다.
     * 07은 인자를 day로 적었지만 앱의 오늘이다.
     */
    public boolean onTimetable(LocalDate today) {
        return timed() && !(period() && done) && !leftPlan(today);
    }

    /**
     * 06 데이터 절의 보이는 날. 끝낸 기간 할 일은 끝낸 날에만(D-66), 마감이 지난 기간 할 일은 마감 날에만, 못 끝낸 계획은 정한 다음
     * 날부터 마감까지(D-67), 끝내지 않은 시각 없는 기간 할 일은 날짜부터 마감까지(마감이 앞이면 날짜에만), 그 밖은 날짜.
     */
    public boolean shownOn(LocalDate day, LocalDate today) {
        if (period() && done) {
            return doneOn().equals(day);
        }
        if (period() && dueDate.isBefore(today)) {
            return dueDate.equals(day);
        }
        if (missed(today)) {
            return day.isAfter(date) && !day.isAfter(dueDate);
        }
        if (!period() || timed()) {
            return date.equals(day);
        }
        return !day.isBefore(date) && !day.isAfter(dueDate.isBefore(date) ? date : dueDate);
    }

    /** 마감이 지나도 끝내지 못한 기간 할 일인가(D-67, D-99). */
    public boolean pastDue(LocalDate today) {
        return period() && !done && dueDate.isBefore(today);
    }

    /**
     * 지난 반복 사본인가(D-69): 끝내지 않은 할 일 반복 사본이고 날짜가 오늘보다 앞이다. 놓친 습관 날은 밀린 일이 아니다.
     */
    public boolean pastCopy(LocalDate today) {
        return kind == Kind.TASK && seriesId != null && !done && date.isBefore(today);
    }

    /** 끝낸 날. doneDay가 없으면(끝낸 채로 만든 값) 날짜다. */
    private LocalDate doneOn() {
        return doneDay != null ? doneDay : date;
    }
}
