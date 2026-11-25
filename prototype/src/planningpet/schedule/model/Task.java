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
    /** 시작 시각이 date 다음 날 새벽(00:00~03:55)인가(D-77). 그래도 date의 일이고 그날 24:00 아래에 놓인다. */
    public boolean nextDay;
    public int minutes;
    public String memo = "";
    public String seriesId;          // 반복 사본끼리 같다(D-17, D-64)
    /** 반복 방식(D-64). 반복 사본이 아니면 null. */
    public Repeat repeat;
    /** 기간 할 일의 마감(D-65). 기간 할 일이 아니면 null이다. */
    public LocalDate dueDate;
    /** 기간 할 일의 첫날(D-66). 기간 할 일이 아니면 null이다. date는 계획한 날(시각을 정한 날)이나 보이기 시작하는 날이다. */
    public LocalDate startDay;
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

    /**
     * 시각을 정한 날에 못 끝낸 기간 할 일인가(D-67): 끝내지 않았고, 시각을 정한 날이 오늘보다 앞이고 마감보다도 앞이다. 데이터는
     * 그대로 두고 오늘로 계산한다.
     */
    public boolean missed(LocalDate today) {
        return period() && !done && timed() && date.isBefore(today) && date.isBefore(dueDate);
    }

    /**
     * 지난 반복 사본인가(D-69): 끝내지 않은 할 일 반복 사본이고 날짜가 오늘보다 앞이다. 놓친 습관 날은 밀린 일이 아니라서
     * "지난 날", 달력 칸의 진한 색, 브리핑의 어제 못 끝낸 일에 넣지 않는다. 통계에는 센다.
     */
    public boolean pastCopy(LocalDate today) {
        return kind == Kind.TASK && seriesId != null && !done && date.isBefore(today);
    }

    /** 시간표와 목록의 시각 있는 칸에 놓이는가. 끝낸 기간 할 일(D-66)과 못 끝낸 계획(D-67)은 계획한 칸에서 빠진다. */
    public boolean onTimetable(LocalDate today) {
        return timed() && !(period() && done) && !missed(today);
    }

    /**
     * 06 데이터 절의 보이는 날. 끝낸 기간 할 일은 끝낸 날에만(D-66), 마감이 지난 기간 할 일은 마감 날에만, 못 끝낸 계획은 정한 다음 날부터 마감까지(D-67), 끝내지 않은
     * 시각 없는 기간 할 일은 날짜부터 마감까지(마감이 앞이면 날짜에만), 그 밖은 날짜.
     */
    public boolean shownOn(LocalDate day, LocalDate today) {
        if (period() && done) {
            return doneOn().equals(day);
        }
        if (period() && dueDate.isBefore(today)) {
            // D-67 5번: 마감이 지나도 못 끝낸 기간 할 일은 보통 할 일처럼 마감 날에만 보인다.
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

    /** 앱 하루의 시각(D-77): date 00:00부터 잰 분. nextDay면 24시간을 더한다. */
    public int startMinute() {
        return (nextDay ? DAY : 0) + start.getHour() * 60 + start.getMinute();
    }

    /** 하루 칸의 분(24:00). */
    public static final int DAY = 24 * 60;
    /** 하루 칸의 끝(D-77): 다음 날 04:00, 곧 28:00. */
    public static final int DAY_END = 28 * 60;

    /** 분을 "HH:mm"으로. 24시간을 넘으면 다음 날 시각이다. */
    public static String hhmm(int minute) {
        int m = minute % DAY;
        return String.format("%02d:%02d", m / 60, m % 60);
    }

    /** 끝 시각 글(D-77): 자정이면 "24:00", 넘으면 "01:00(다음 날)". */
    public static String endText(int minute) {
        if (minute == DAY) {
            return "24:00";
        }
        return minute > DAY ? hhmm(minute) + "(다음 날)" : hhmm(minute);
    }

    /** 앱 하루의 분에서 시각과 다음 날 여부를 넣는다. null이면 시각을 뺀다. */
    public void setStart(Integer minute) {
        start = minute == null ? null : LocalTime.of(minute % DAY / 60, minute % 60);
        nextDay = minute != null && minute >= DAY;
    }

    public int endMinute() {
        return startMinute() + minutes;
    }
}
