package planningpet.schedule.model;

import java.time.LocalDate;
import java.time.LocalTime;

/** 일정 하나. 06 "데이터" 표의 Task 값을 그대로 둔다. */
public final class Task {
    public enum Kind { TASK, FIXED }

    public final String id;
    public final long seq;
    public Kind kind;
    public String title;
    public LocalDate date;
    public LocalTime start;          // 없으면 null
    public int minutes;
    public String memo = "";
    public String seriesId;          // 매주 반복 사본끼리 같다
    public boolean done;
    public int rewardPoints;
    public int rewardExp;
    public LocalDate rewardDay;
    /** 경험치를 받은 펫의 종류(D-48). 받지 않았으면 null. */
    public String rewardPet;
    /** D-54의 유형. */
    public String category = Categories.OTHER;

    public Task(String id, long seq, Kind kind, String title, LocalDate date, LocalTime start, int minutes) {
        this.id = id;
        this.seq = seq;
        this.kind = kind;
        this.title = title;
        this.date = date;
        this.start = start;
        this.minutes = minutes;
    }

    public boolean timed() {
        return start != null;
    }

    public int startMinute() {
        return start.getHour() * 60 + start.getMinute();
    }

    public int endMinute() {
        return startMinute() + minutes;
    }
}
