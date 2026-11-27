package planningpet.schedule.model;

import static java.time.DayOfWeek.MONDAY;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.Test;

/** 06 4번, 5번과 docs/porting/task-edit.md 2절의 경계. 따로 정하지 않으면 오늘은 11-18(수)이다. */
class TaskBookEditTest {
    private static final LocalTime T1030 = LocalTime.of(10, 30);
    private static final LocalTime T1300 = LocalTime.of(13, 0);
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

    /** 월 반복: 고정 일정 "대학수학" 11-16부터 12-28까지 월요일, 10:30, 75분(T-ADD-3). */
    private void weekly() {
        book().add(new TaskDraft(Kind.FIXED, "대학수학", Category.OTHER, day(11, 16), null, T1030, 75, "",
                Repeat.weekly(MONDAY), day(12, 28), false, null, null, false));
    }

    private Task at(LocalDate date) {
        return book.getTasks().stream().filter(t -> t.getDate().equals(date)).findFirst().orElseThrow();
    }

    /** 일정마다 f의 값. 넣은 순서다. */
    private List<Object> each(Function<Task, Object> f) {
        return book.getTasks().stream().map(f).toList();
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

    @Test
    void T_EDIT_2_onlyThisCopy() {
        weekly();
        Task t = at(day(11, 30));
        String series = t.getSeriesId();
        book.update(t, draft(t, "대학수학", day(11, 30), null, T1300, 75), EditScope.ONLY_THIS);
        assertEquals(List.of(T1030, T1030, T1300, T1030, T1030, T1030, T1030), each(Task::getStartTime));
        assertEquals(Collections.nCopies(7, series), each(Task::getSeriesId));
        assertEquals(Collections.nCopies(7, Repeat.weekly(MONDAY)), each(Task::getRepeat));
    }

    @Test
    void T_EDIT_7_thisAndAfterCopies() {
        weekly();
        Task t = at(day(11, 30));
        book.update(t, draft(t, "대학수학", day(11, 30), null, T1300, 75), EditScope.THIS_AND_AFTER);
        assertEquals(List.of(T1030, T1030, T1300, T1300, T1300, T1300, T1300), each(Task::getStartTime));
    }

    /** 넣은 순서로 앞 n개는 "대학수학", 나머지는 title이다. */
    private void titles(int n, String title) {
        List<Object> want = new ArrayList<>(Collections.nCopies(n, "대학수학"));
        want.addAll(Collections.nCopies(7 - n, title));
        assertEquals(want, each(Task::getTitle));
    }

    @Test
    void T_EDIT_8_dateOnlyThisCopy() {
        weekly();
        Task t = at(day(11, 30));
        book.update(t, draft(t, "대학수학 보강", day(12, 1), null, T1030, 75), EditScope.THIS_AND_AFTER);
        assertEquals(List.of(day(11, 16), day(11, 23), day(12, 1), day(12, 7), day(12, 14), day(12, 21), day(12, 28)),
                each(Task::getDate));
        titles(2, "대학수학 보강");
    }

    @Test
    void E_EDIT_8_dateBeforeEarlierCopy() {
        weekly();
        Task t = at(day(11, 30));
        book.update(t, draft(t, "보강", day(11, 20), null, T1030, 75), EditScope.THIS_AND_AFTER);
        assertEquals(List.of(day(11, 16), day(11, 23), day(11, 20), day(12, 7), day(12, 14), day(12, 21), day(12, 28)),
                each(Task::getDate));
        titles(2, "보강");
    }

    @Test
    void E_EDIT_10_lastCopy() {
        weekly();
        Task t = at(day(12, 28));
        book.update(t, draft(t, "끝", day(12, 28), null, T1030, 75), EditScope.THIS_AND_AFTER);
        titles(6, "끝");
    }
}
