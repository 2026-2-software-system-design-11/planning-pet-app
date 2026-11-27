package planningpet.schedule.model;

import java.time.LocalDate;
import java.time.LocalTime;
import planningpet.pet.model.PetKind;

/**
 * 일정 하나(06 데이터 절 Task). 값은 06 데이터 표 순서대로 생성자에서 모두 받는다. 처음 값은 만드는 쪽이 정한다.
 * 반복 방식(repeat)은 Repeat과 함께 M3에서 더한다.
 */
public final class Task {
    /** 하루 칸의 분(24:00). */
    public static final int DAY = 24 * 60;

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
            LocalDate dueDate, LocalDate startDay, boolean done, LocalDate doneDay, int rewardPoints,
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
}
