package planningpet.schedule.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

/** 06 3번과 docs/porting/task-add.md 2절의 경계. */
class TaskBookTest {
    private long next = 1;
    private final TaskBook book = new TaskBook(List.of(), () -> next++);

    private static LocalDate day(int month, int d) {
        return LocalDate.of(2026, month, d);
    }

    /** minute은 앱 하루의 시작 분(D-77)이고 null이면 시각 없음이다. 유형 기타, 메모 빈 글, 추가 창의 값이다. */
    private static TaskDraft draft(Kind kind, String title, LocalDate date, LocalDate due, Integer minute, int minutes) {
        LocalTime start = minute == null ? null : LocalTime.of(minute % Task.DAY / 60, minute % 60);
        return new TaskDraft(kind, title, Category.OTHER, date, due, start, minutes, "", null, null,
                minute != null && minute >= Task.DAY, null, null, false);
    }

    /** 할 일 "과제" 30분 하나를 넣는다. */
    private Task one(LocalDate date, LocalDate due, Integer minute) {
        List<Task> made = book.add(draft(Kind.TASK, "과제", date, due, minute, 30));
        assertEquals(1, made.size());
        return made.get(0);
    }

    @Test
    void T_ADD_1_firstTask() {
        Task t = one(day(11, 16), null, null);
        assertEquals(List.of(t), book.getTasks());
        assertEquals(List.of(1L, 2L), List.of(t.getSeq(), next));
        assertEquals(Arrays.asList(false, null, 0, 0, null, null, null, null), Arrays.asList(t.isDone(), t.getDoneDay(),
                t.getRewardPoints(), t.getRewardExp(), t.getRewardDay(), t.getRewardPet(), t.getSeriesId(), t.getRepeat()));
    }

    @Test
    void T_ADD_2_secondTask() {
        one(day(11, 16), null, null);
        assertEquals(2, one(day(11, 16), null, null).getSeq());
    }

    @Test
    void E_ADD_1_invalid() {
        assertThrows(IllegalArgumentException.class, () -> book.add(draft(Kind.TASK, "  ", day(11, 16), null, null, 30)));
        assertEquals(List.of(), book.getTasks());
        assertEquals(1, next);
    }

    @Test
    void T_ADD_7_period() {
        Task t = one(day(11, 18), day(11, 22), null);
        assertEquals(List.of(day(11, 18), day(11, 18), day(11, 22)), List.of(t.getDate(), t.getStartDay(), t.getDueDate()));
        assertNull(t.getSeriesId());
    }

    @Test
    void T_ADD_8_periodHasNoTime() {
        assertNull(one(day(11, 18), day(11, 22), 600).getStartTime());
    }

    @Test
    void E_ADD_2_titleMemoCategory() {
        Task t = book.add(new TaskDraft(Kind.TASK, "  과제  ", Category.EXERCISE, day(11, 16), null, null, 30, "준비물",
                null, null, false, null, null, false)).get(0);
        assertEquals(List.of("과제", "준비물", "운동"), List.of(t.getTitle(), t.getMemo(), t.getCategory().label()));
    }

    @Test
    void E_ADD_3_nextDay() {
        Task t = one(day(11, 16), null, Task.DAY + 90);
        assertEquals(List.of(LocalTime.of(1, 30), true), List.of(t.getStartTime(), t.isNextDay()));
    }

    @Test
    void E_ADD_4_latestStart() {
        Task t = book.add(draft(Kind.TASK, "과제", day(11, 16), null, 27 * 60 + 50, 10)).get(0);
        assertEquals(List.of(LocalTime.of(3, 50), true), List.of(t.getStartTime(), t.isNextDay()));
    }

    @Test
    void E_ADD_5_midnight() {
        Task t = one(day(11, 16), null, 0);
        assertEquals(List.of(LocalTime.MIDNIGHT, false), List.of(t.getStartTime(), t.isNextDay()));
    }

    @Test
    void E_ADD_6_periodNextDay() {
        Task t = one(day(11, 18), day(11, 22), Task.DAY + 90);
        assertEquals(Arrays.asList(null, false), Arrays.asList(t.getStartTime(), t.isNextDay()));
    }

    @Test
    void E_ADD_13_pastDate() {
        assertEquals(day(11, 10), one(day(11, 10), null, null).getDate());
    }
}
