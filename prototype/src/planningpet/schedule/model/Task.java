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
    public String seriesId;          // 반복 사본끼리 같다(D-17, D-64)
    /** 반복 방식(D-64). 반복 사본이 아니면 null. */
    public Repeat repeat;
    /** 기간 할 일의 마감(D-65). 기간 할 일이 아니면 null이고 date는 기간의 첫날이다. */
    public LocalDate dueDate;
    public boolean done;
    /** 끝낸 앱 날짜(D-66). 끝내지 않았으면 null이다. 기간 할 일은 이 날에 보이고 센다. */
    public LocalDate doneDay;
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

    /** 기간 할 일인가(D-65). */
    public boolean period() {
        return dueDate != null;
    }

    /** 끝낸 날. doneDay가 없으면(끝낸 채로 만든 목 데이터) 날짜다. */
    public LocalDate doneOn() {
        return doneDay != null ? doneDay : date;
    }

    /** 06 데이터 절의 세는 날: 기간 할 일은 끝냈으면 끝낸 날, 아니면 마감(D-65, D-66). 그 밖은 날짜. */
    public LocalDate countDay() {
        if (!period()) {
            return date;
        }
        return done ? doneOn() : dueDate;
    }

    /** 시간표와 목록의 시각 있는 칸에 놓이는가. 끝낸 기간 할 일은 계획한 칸에서 빠진다(D-66). */
    public boolean onTimetable() {
        return timed() && !(period() && done);
    }

    /**
     * 06 데이터 절의 보이는 날. 끝낸 기간 할 일은 끝낸 날에만(D-66), 끝내지 않은 시각 없는 기간 할 일은 날짜부터 마감까지(마감이 앞이면
     * 날짜에만), 그 밖은 날짜.
     */
    public boolean shownOn(LocalDate day) {
        if (period() && done) {
            return doneOn().equals(day);
        }
        if (!period() || timed()) {
            return date.equals(day);
        }
        return !day.isBefore(date) && !day.isAfter(dueDate.isBefore(date) ? date : dueDate);
    }

    public int startMinute() {
        return start.getHour() * 60 + start.getMinute();
    }

    public int endMinute() {
        return startMinute() + minutes;
    }
}
