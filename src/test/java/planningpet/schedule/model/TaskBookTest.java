package planningpet.schedule.model;

import static java.time.DayOfWeek.FRIDAY;
import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.THURSDAY;
import static java.time.DayOfWeek.TUESDAY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import org.junit.jupiter.api.Test;

/** 06 3번과 docs/porting/task-add.md 2절의 경계. 오늘은 11-18(수)이다. */
class TaskBookTest {
    private static final LocalDate TODAY = LocalDate.of(2026, 11, 18);
    private long next = 1;
    private final TaskBook book = new TaskBook(List.of(), () -> next++, () -> TODAY);

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

    /** "과제"의 반복 값. minute은 그날 시작 분이고 null이면 시각 없음이다. */
    private static TaskDraft repeat(Kind kind, LocalDate date, Integer minute, int minutes, Repeat how, LocalDate end) {
        LocalTime start = minute == null ? null : LocalTime.of(minute / 60, minute % 60);
        return new TaskDraft(kind, "과제", Category.OTHER, date, null, start, minutes, "", how, end, false, null, null,
                false);
    }

    /** 넣은 사본이 날짜 순으로 dates이고 seq가 firstSeq부터 하나씩이며, 같은 seriesId와 how, 서로 다른 id를 가졌는지 본다. */
    private void copies(List<Task> made, Repeat how, long firstSeq, LocalDate... dates) {
        assertEquals(List.of(dates), made.stream().map(Task::getDate).toList());
        assertEquals(LongStream.range(firstSeq, firstSeq + dates.length).boxed().toList(),
                made.stream().map(Task::getSeq).toList());
        assertNotNull(made.get(0).getSeriesId());
        assertEquals(1, made.stream().map(Task::getSeriesId).distinct().count());
        assertEquals(Set.of(how), made.stream().map(Task::getRepeat).collect(Collectors.toSet()));
        assertEquals(dates.length, made.stream().map(Task::getId).distinct().count());
        assertEquals(made, book.getTasks());
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

    @Test
    void T_ADD_3_weekly() {
        copies(book.add(repeat(Kind.FIXED, day(11, 16), 630, 75, Repeat.weekly(MONDAY), day(12, 28))),
                Repeat.weekly(MONDAY), 1, day(11, 16), day(11, 23), day(11, 30), day(12, 7), day(12, 14), day(12, 21),
                day(12, 28));
    }

    @Test
    void T_ADD_4_endNotRepeatDay() {
        copies(book.add(repeat(Kind.FIXED, day(11, 16), 630, 75, Repeat.weekly(MONDAY), day(12, 30))),
                Repeat.weekly(MONDAY), 1, day(11, 16), day(11, 23), day(11, 30), day(12, 7), day(12, 14), day(12, 21),
                day(12, 28));
    }

    @Test
    void T_ADD_5_dailyTask() {
        List<Task> made = book.add(repeat(Kind.TASK, day(11, 16), null, 30, Repeat.DAILY, day(11, 22)));
        copies(made, Repeat.DAILY, 1, day(11, 16), day(11, 17), day(11, 18), day(11, 19), day(11, 20), day(11, 21),
                day(11, 22));
        assertTrue(made.stream().noneMatch(Task::timed));
    }

    @Test
    void T_ADD_6_severalDays() {
        Repeat how = Repeat.weekly(TUESDAY, THURSDAY);
        copies(book.add(repeat(Kind.FIXED, day(11, 16), 1080, 240, how, day(11, 26))), how, 1, day(11, 17),
                day(11, 19), day(11, 24), day(11, 26));
    }

    @Test
    void E_ADD_9_newYear() {
        Repeat how = Repeat.weekly(FRIDAY);
        copies(book.add(repeat(Kind.TASK, day(12, 25), null, 30, how, LocalDate.of(2027, 1, 8))), how, 1,
                day(12, 25), LocalDate.of(2027, 1, 1), LocalDate.of(2027, 1, 8));
    }

    @Test
    void E_ADD_10_oneCopy() {
        Repeat how = Repeat.weekly(MONDAY);
        copies(book.add(repeat(Kind.TASK, day(11, 16), null, 30, how, day(11, 16))), how, 1, day(11, 16));
    }

    @Test
    void E_ADD_7_maxCopies() {
        List<Task> made = book.add(repeat(Kind.TASK, day(11, 16), null, 30, Repeat.DAILY, LocalDate.of(2027, 3, 15)));
        assertEquals(List.of(120, LocalDate.of(2027, 3, 15), 120L, 121L),
                List.of(made.size(), made.get(119).getDate(), made.get(119).getSeq(), next));
    }

    @Test
    void E_ADD_8_tooManyCopies() {
        TaskDraft d = repeat(Kind.TASK, day(11, 16), null, 30, Repeat.DAILY, LocalDate.of(2027, 3, 16));
        assertThrows(IllegalArgumentException.class, () -> book.add(d));
        assertEquals(List.of(List.of(), 1L), List.of(book.getTasks(), next));
    }

    @Test
    void E_ADD_11_seqContinues() {
        next = 41;
        copies(book.add(repeat(Kind.TASK, day(11, 16), null, 30, Repeat.DAILY, day(11, 18))), Repeat.DAILY, 41,
                day(11, 16), day(11, 17), day(11, 18));
        assertEquals(44, next);
    }

    @Test
    void E_ADD_12_editingDraft() {
        TaskDraft d = new TaskDraft(Kind.TASK, "과제", Category.OTHER, day(11, 16), null, null, 30, "", Repeat.DAILY,
                LocalDate.of(2027, 3, 16), false, null, null, true);
        assertThrows(IllegalArgumentException.class, () -> book.add(d));
        assertEquals(List.of(List.of(), 1L), List.of(book.getTasks(), next));
    }

    /** title인 할 일이나 고정 일정 하나를 넣는다. minute은 앱 하루의 시작 분이다. */
    private Task put(Kind kind, String title, LocalDate date, int minute, int minutes) {
        return book.add(draft(kind, title, date, null, minute, minutes)).get(0);
    }

    /** 11-16에 새 할 일 "새 일"을 minute부터 minutes분 넣을 때의 겹침. */
    private Overlap at(int minute, int minutes) {
        return book.overlap(draft(Kind.TASK, "새 일", day(11, 16), null, minute, minutes), null);
    }

    @Test
    void T_OVL_1_overlaps() {
        Task essay = put(Kind.TASK, "자소서 초안", day(11, 16), 14 * 60, 60);
        assertEquals(new Overlap(essay, 30, true), at(14 * 60 + 30, 60));
    }

    @Test
    void T_OVL_2_noOverlap() {
        put(Kind.TASK, "자소서 초안", day(11, 16), 14 * 60, 60);
        assertEquals(Arrays.asList(null, null), Arrays.asList(at(15 * 60, 30), at(13 * 60, 60)));
        assertNull(book.overlap(draft(Kind.TASK, "새 일", day(11, 16), null, null, 60), null));
    }

    @Test
    void T_OVL_3_earliestStart() {
        Task lab = put(Kind.FIXED, "실습", day(11, 16), 8 * 60, 480);
        put(Kind.TASK, "과제", day(11, 16), 8 * 60 + 30, 30);
        assertEquals(new Overlap(lab, 60, true), at(8 * 60, 60));
    }

    @Test
    void T_OVL_4_repeatAndEdit() {
        Task homework = put(Kind.TASK, "과제", day(11, 17), 18 * 60 + 30, 60);
        Task report = put(Kind.TASK, "보고서", day(11, 16), 10 * 60, 30);
        TaskDraft job = repeat(Kind.FIXED, day(11, 16), 18 * 60, 240, Repeat.weekly(TUESDAY), day(11, 30));
        assertEquals(new Overlap(homework, 60, true), book.overlap(job, null));
        Task jobOn17 = book.add(job).get(0);
        assertNull(book.overlap(draft(Kind.TASK, "보고서", day(11, 16), null, 10 * 60 + 15, 30), report));
        assertEquals(new Overlap(jobOn17, 60, true),
                book.overlap(draft(Kind.TASK, "과제", day(11, 17), null, 19 * 60, 60), homework));
    }

    @Test
    void E_OVL_6_sameStart() {
        Task first = put(Kind.TASK, "먼저", day(11, 16), 10 * 60, 60);
        put(Kind.TASK, "나중", day(11, 16), 10 * 60, 60);
        assertEquals(new Overlap(first, 60, true), at(10 * 60, 60));
    }

    /** from부터 due까지인 기간 할 일 "계획"을 date 10:00, 60분에 정해 둔 것. done이면 date에 끝냈다. */
    private static Task planned(LocalDate from, LocalDate due, LocalDate date, boolean done) {
        return new Task("p", 1, Kind.TASK, "계획", date, LocalTime.of(10, 0), 60, "", false, Category.OTHER, null, null,
                due, from, done, done ? date : null, 0, 0, null, null);
    }

    /** 일정 other 하나만 있을 때 date의 minute부터 60분인 새 할 일의 겹침. */
    private static Overlap with(Task other, LocalDate date, int minute) {
        TaskBook only = new TaskBook(List.of(other), () -> 2, () -> TODAY);
        return only.overlap(draft(Kind.TASK, "새 일", date, null, minute, 60), null);
    }

    @Test
    void E_OVL_1_periodDraft() {
        put(Kind.TASK, "과제", TODAY, 10 * 60, 60);
        assertNull(book.overlap(draft(Kind.TASK, "기간", TODAY, day(11, 22), 10 * 60, 60), null));
    }

    @Test
    void E_OVL_2_doneTask() {
        Task done = new Task("d", 1, Kind.TASK, "끝낸 일", TODAY, LocalTime.of(10, 0), 60, "", false, Category.OTHER, null,
                null, null, null, true, TODAY, 0, 0, null, null);
        assertEquals(new Overlap(done, 30, true), with(done, TODAY, 10 * 60 + 30));
    }

    @Test
    void E_OVL_3_donePeriod() {
        assertNull(with(planned(TODAY, day(11, 22), TODAY, true), TODAY, 10 * 60));
    }

    @Test
    void E_OVL_4_missedPlan() {
        assertNull(with(planned(day(11, 16), day(11, 22), day(11, 17), false), day(11, 17), 10 * 60));
    }

    @Test
    void E_OVL_5_laterPlan() {
        Task plan = planned(day(11, 16), day(11, 22), day(11, 19), false);
        assertEquals(new Overlap(plan, 60, true), with(plan, day(11, 19), 10 * 60));
    }

    @Test
    void E_OVL_10_editingRepeat() {
        Task self = put(Kind.TASK, "자기", day(11, 16), 10 * 60, 60);
        put(Kind.TASK, "목요 일", day(11, 19), 10 * 60, 60);
        assertNull(book.overlap(repeat(Kind.TASK, day(11, 16), 10 * 60, 60, Repeat.DAILY, day(11, 22)), self));
    }
}
