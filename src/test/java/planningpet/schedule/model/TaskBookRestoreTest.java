package planningpet.schedule.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;

/** docs/porting/store.md 2절의 E-BOOK 경계(지운 보상 기록을 넣어 만들기). 오늘은 11-18(수)이다. */
class TaskBookRestoreTest {
    private static final LocalDate TODAY = LocalDate.of(2026, 11, 18);
    private long next = 4;

    /** seq번 할 일. done이면 그날 끝내 10P, 경험치 10을 받았다. */
    private static Task task(long seq, LocalTime start, boolean done) {
        return new Task("id" + seq, seq, Kind.TASK, "할 일" + seq, TODAY, start, 30, "", false, Category.OTHER, null,
                null, null, null, done, done ? TODAY : null, done ? 10 : 0, done ? 10 : 0, done ? TODAY : null, null);
    }

    private final Task a = task(1, LocalTime.of(9, 0), true);
    private final Task b = task(2, null, false);
    private final TaskBook book = new TaskBook(List.of(a, b), List.of(task(3, null, true)), () -> next++, () -> TODAY);

    private static List<Long> seqs(List<Task> tasks) {
        return tasks.stream().map(Task::getSeq).toList();
    }

    @Test
    void E_BOOK_1_madeWithDeletedRewards() {
        assertEquals(List.of(3L), seqs(book.getDeletedRewarded()));
        assertEquals(List.of(3L, 1L), seqs(book.rewardHistory()));
    }
}
