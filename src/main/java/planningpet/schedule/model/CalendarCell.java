package planningpet.schedule.model;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/** 06 26번. 메인 달력 칸에 보일 그날 일의 수와 마감 표시(D-86). */
public final class CalendarCell {
    private CalendarCell() {
    }

    /**
     * 그날 목록(6번 dayPlan)의 "오늘 할 일"(dueToday, timed, untimed) 가운데 할 일을, withFixed면 고정 일정도 센다(D-94). 마감 제목은
     * 그와 따로 그날이 마감인 끝내지 않은 기간 할 일에서 넣은 순으로 모은다. 지난 반복 사본과 지난 날의 고정 일정은 끝내지 않은 일로
     * 보지 않는다(D-69).
     */
    public static CellPreview preview(TaskBook book, LocalDate date, boolean withFixed) {
        DayPlan plan = book.dayPlan(date);
        List<Task> counted = Stream.of(plan.dueToday(), plan.timed(), plan.untimed()).flatMap(List::stream)
                .filter(t -> t.getKind() == Kind.TASK || withFixed)
                .toList();
        List<String> due = book.getTasks().stream()
                .filter(t -> t.period() && !t.isDone() && t.getDueDate().equals(date))
                .sorted(Comparator.comparingLong(Task::getSeq))
                .map(Task::getTitle)
                .toList();
        LocalDate today = book.today();
        boolean open = counted.stream().anyMatch(t -> !t.isDone() && !t.pastCopy(today)
                && !(t.getKind() == Kind.FIXED && t.getDate().isBefore(today)));
        return new CellPreview(counted.size(), open, due);
    }
}
