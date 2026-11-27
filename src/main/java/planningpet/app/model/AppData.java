package planningpet.app.model;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import planningpet.common.model.AppDay;
import planningpet.schedule.model.TaskBook;

/** 앱 하나에 하나뿐인 값(06 데이터 절 AppData). 일정은 TaskBook, 펫은 PetState로 담는다(07). */
public final class AppData {
    private final Clock clock;
    private long nextSeq = 1;
    private final TaskBook book = new TaskBook(List.of(), () -> nextSeq++, this::today);

    /** 06 데이터 절의 처음 값인 앱 데이터. 시계는 Main이 컴퓨터 시계를, 검사가 고정 시계를 넘긴다(07 실행 모드). */
    public AppData(Clock clock) {
        this.clock = clock;
    }

    /** 앱의 오늘(06 1번, D-15). */
    public LocalDate today() {
        return AppDay.of(LocalDateTime.now(clock));
    }

    public TaskBook getBook() { return book; }
    /** 다음 일정에 줄 seq. */
    public long getNextSeq() { return nextSeq; }
}
