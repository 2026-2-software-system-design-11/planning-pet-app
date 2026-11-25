package planningpet.app.model;

import planningpet.common.model.Rules;
import planningpet.schedule.model.DefaultDates;
import planningpet.schedule.model.Task;
import planningpet.schedule.model.TaskDraft;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

/** 06의 31번(D-74). 첫 실행의 "예시로 둘러보기"가 넣는 예시 일정. 목 데이터를 오늘과 이번 주 날짜에 맞춘 것이다. */
public final class Examples {
    private Examples() {
    }

    /** 오늘과 오늘이 든 주에 맞춰 예시 일정을 넣고 그 id를 sampleIds에 넣는다. 포인트, 경험치, 아이템은 건드리지 않는다. */
    public static void fill(Store s) {
        LocalDate today = s.today();
        LocalDate week = Rules.weekStart(today, s.weekStart);
        fixed(s, "자료구조", day(week, DayOfWeek.MONDAY), 9);
        fixed(s, "자료구조", day(week, DayOfWeek.WEDNESDAY), 9);
        fixed(s, "운영체제", day(week, DayOfWeek.TUESDAY), 10);
        fixed(s, "운영체제", day(week, DayOfWeek.THURSDAY), 10);
        Task algo = task(s, "알고리즘 과제 2번", today, LocalTime.of(10, 0), 60, "학업");
        algo.done = true;
        algo.doneDay = today;
        task(s, "자소서 초안", today, LocalTime.of(14, 0), 90, "프로젝트").memo = "1번 문항 먼저 쓰기";
        task(s, "토익 단어", today, null, 30, "학업");
        task(s, "은행 서류", today.plusDays(1), null, 30, "기타");
        TaskDraft e = new TaskDraft(Task.Kind.TASK, "E사 지원서", today, null, 120, "")
                .withDue(DefaultDates.due(today, s.weekStart));
        mark(s, s.book.add(e).get(0)).category = "프로젝트";
    }

    /** 그 주에서 그 요일인 날. */
    private static LocalDate day(LocalDate weekStart, DayOfWeek dow) {
        return weekStart.plusDays(Math.floorMod(dow.getValue() - weekStart.getDayOfWeek().getValue(), 7));
    }

    private static void fixed(Store s, String title, LocalDate date, int hour) {
        mark(s, s.book.add(Task.Kind.FIXED, title, date, LocalTime.of(hour, 0), 75, "", null).get(0)).category = "학업";
    }

    private static Task task(Store s, String title, LocalDate date, LocalTime start, int minutes, String category) {
        Task t = mark(s, s.book.add(Task.Kind.TASK, title, date, start, minutes, "", null).get(0));
        t.category = category;
        return t;
    }

    private static Task mark(Store s, Task t) {
        s.sampleIds.add(t.id);
        return t;
    }
}
