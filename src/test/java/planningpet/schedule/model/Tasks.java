package planningpet.schedule.model;

import java.time.LocalDate;

/** 테스트가 함께 쓰는 날짜와 일정 만들기(#518). 고칠 값만 적고 나머지는 처음 값으로 둔다. */
public final class Tasks {
    private Tasks() {
    }

    /** 2026-11-01에서 day-1일 뒤. 31부터는 12월로 넘어간다. */
    public static LocalDate d(int day) { return LocalDate.of(2026, 11, 1).plusDays(day - 1); }

    public static LocalDate d(int month, int day) { return LocalDate.of(2026, month, day); }
}
