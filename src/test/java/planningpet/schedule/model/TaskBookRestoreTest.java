package planningpet.schedule.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;

/** docs/porting/store.md 2절의 E-BOOK 경계(지운 보상 기록을 넣어 만들기, 되돌리기). 오늘은 11-18(수)이다. */
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

    private TaskBook copy() {
        TaskBook c = new TaskBook(List.of(), () -> 0, () -> TODAY);
        c.restore(book);
        return c;
    }

    private static List<Long> seqs(List<Task> tasks) {
        return tasks.stream().map(Task::getSeq).toList();
    }

    @Test
    void E_BOOK_1_madeWithDeletedRewards() {
        assertEquals(List.of(3L), seqs(book.getDeletedRewarded()));
        assertEquals(List.of(3L, 1L), seqs(book.rewardHistory()));
    }

    @Test
    void E_BOOK_2_copyIsSeparate() {
        TaskBook c = copy();
        book.clearTime(a, EditScope.ONLY_THIS);
        assertNotSame(a, c.getTasks().get(0));
        assertEquals(LocalTime.of(9, 0), c.getTasks().get(0).getStartTime());
        assertEquals(List.of(3L), seqs(c.getDeletedRewarded()));
    }

    @Test
    void E_BOOK_3_restoreKeepsObjects() {
        TaskBook c = copy();
        book.clearTime(a, EditScope.ONLY_THIS);
        book.restore(c);
        assertSame(a, book.getTasks().get(0));
        assertEquals(LocalTime.of(9, 0), a.getStartTime());
    }

    @Test
    void E_BOOK_4_deletedTaskComesBack() {
        TaskBook c = copy();
        book.delete(a, DeleteScope.ONLY_THIS);
        book.restore(c);
        assertEquals(List.of(a, b), book.getTasks());
        assertEquals(List.of(3L), seqs(book.getDeletedRewarded()));
    }

    @Test
    void E_BOOK_5_addedTaskLeaves() {
        TaskBook c = copy();
        book.add(new TaskDraft(Kind.TASK, "새 일", Category.OTHER, TODAY, null, null, 30, "", null, null, false, null,
                TODAY, false));
        book.restore(c);
        assertEquals(List.of(1L, 2L), seqs(book.getTasks()));
    }
}
