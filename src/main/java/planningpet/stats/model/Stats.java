package planningpet.stats.model;

import java.time.LocalDate;
import java.util.List;
import planningpet.app.model.AppData;
import planningpet.schedule.model.Task;

/** 통계 화면의 값(06 29번, D-55). 펫 둘이 함께 쓰는 일정, 보상, 출석을 센다(D-48). */
public final class Stats {
    private final AppData data;

    public Stats(AppData data) {
        this.data = data;
    }

    /** 기간 동안 끝낸 수와 셀 일정 수. */
    public Rate rate(Period p) {
        return rate(counted(p.from(), p.to()));
    }

    /**
     * from부터 to까지가 세는 날(06 데이터 절)인 셀 일정. 앞날은 세지 않고(D-55), 오늘의 일정은 끝낸 것만 센다(D-63). 끝내지 않은 기간 할
     * 일은 마감 날의 일이다(D-65).
     */
    private List<Task> counted(LocalDate from, LocalDate to) {
        LocalDate today = data.today();
        LocalDate end = to.isAfter(today) ? today : to;
        return data.getBook().getTasks().stream()
                .filter(t -> !t.countDay().isBefore(from) && !t.countDay().isAfter(end))
                .filter(t -> t.isDone() || t.countDay().isBefore(today)).toList();
    }

    private static Rate rate(List<Task> list) {
        return new Rate((int) list.stream().filter(Task::isDone).count(), list.size());
    }
}
