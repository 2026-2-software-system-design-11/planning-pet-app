package planningpet.schedule.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static planningpet.schedule.model.Tasks.d;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

/** 공용 일정 만들기 Tasks(#518)의 처음 값과 고친 값. */
class TasksTest {
    private static List<Object> all(Task t) {
        return Arrays.asList(t.getId(), t.getSeq(), t.getKind(), t.getTitle(), t.getDate(), t.getStartTime(),
                t.getDurationMinutes(), t.getMemo(), t.isNextDay(), t.getCategory(), t.getSeriesId(), t.getRepeat(),
                t.getDueDate(), t.getStartDay(), t.isDone(), t.getDoneDay(), t.getRewardPoints(), t.getRewardExp(),
                t.getRewardDay(), t.getRewardPet());
    }

    @Test
    void days() {
        assertEquals(LocalDate.of(2026, 11, 18), d(18));
        assertEquals(LocalDate.of(2026, 12, 1), d(31));
        assertEquals(LocalDate.of(2026, 1, 2), d(1, 2));
    }

    @Test
    void defaults() {
        assertEquals(Arrays.asList("운동", 1L, Kind.TASK, "운동", d(18), null, 30, "", false, Category.OTHER, null, null,
                null, null, false, null, 0, 0, null, null), all(Tasks.task("운동", d(18)).task()));
    }

    @Test
    void timeKindAndDone() {
        Task t = Tasks.task("알바", d(18)).id("a").seq(7).kind(Kind.FIXED).at("23:30").minutes(90).memo("초안").nextDay()
                .category(Category.STUDY).done(d(19)).task();
        assertEquals(Arrays.asList("a", 7L, Kind.FIXED, "알바", d(18), LocalTime.of(23, 30), 90, "초안", true,
                Category.STUDY, null, null, null, null, true, d(19), 0, 0, null, null), all(t));
        assertEquals(LocalTime.of(9, 5), Tasks.task("x", d(18)).at(LocalTime.of(9, 5)).task().getStartTime());
    }
}
