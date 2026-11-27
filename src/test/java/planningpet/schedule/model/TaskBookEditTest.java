package planningpet.schedule.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/** 06 4번, 5번과 docs/porting/task-edit.md 2절의 경계. 따로 정하지 않으면 오늘은 11-18(수)이다. */
class TaskBookEditTest {
    private LocalDate today = day(11, 18);
    private long next = 1;
    private final List<Task> tasks = new ArrayList<>();
    private TaskBook book;

    private static LocalDate day(int month, int d) {
        return LocalDate.of(2026, month, d);
    }

    /** 할 일 하나를 둔다. due가 있으면 첫날 from인 기간 할 일이다. */
    private Task put(String title, LocalDate date, LocalTime start, LocalDate from, LocalDate due, String series) {
        Task t = new Task("id" + next, next++, Kind.TASK, title, date, start, 30, "", false, Category.OTHER, series,
                series == null ? null : Repeat.DAILY, due, from, false, null, 0, 0, null, null);
        tasks.add(t);
        return t;
    }

    private TaskBook book() {
        book = new TaskBook(tasks, () -> next++, () -> today);
        return book;
    }

    /** 수정 창의 값. 유형과 메모는 t의 값이다. 기간 할 일의 날짜 칸은 첫날이다. */
    private static TaskDraft draft(Task t, String title, LocalDate date, LocalDate due, LocalTime start, int minutes) {
        return new TaskDraft(t.getKind(), title, t.getCategory(), date, due, start, minutes, t.getMemo(), t.getRepeat(),
                null, false, null, null, true);
    }

    @Test
    void T_EDIT_3_seqStays() {
        put("a", day(11, 16), null, null, null, null);
        put("b", day(11, 16), null, null, null, null);
        Task t = put("c", day(11, 16), null, null, null, null);
        book().update(t, draft(t, "새 제목", day(11, 16), null, null, 30), EditScope.ONLY_THIS);
        assertEquals(List.of("새 제목", 3L), List.of(t.getTitle(), t.getSeq()));
    }
}
