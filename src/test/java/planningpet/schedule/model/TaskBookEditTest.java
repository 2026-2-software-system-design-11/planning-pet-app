package planningpet.schedule.model;

import static java.time.DayOfWeek.MONDAY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
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

    /** 할 일 하나를 둔다. due가 있으면 첫날 from인 기간 할 일, rewardDay가 있으면 그날 끝내 10P, 경험치 10을 받았다. */
    private Task put(String title, LocalDate date, LocalTime start, LocalDate from, LocalDate due, String series,
            LocalDate rewardDay) {
        int reward = rewardDay == null ? 0 : 10;
        Task t = new Task("id" + next, next++, Kind.TASK, title, date, start, 30, "", false, Category.OTHER, series,
                series == null ? null : Repeat.DAILY, due, from, rewardDay != null, rewardDay, reward, reward,
                rewardDay, null);
        tasks.add(t);
        return t;
    }

    private Task put(String title, LocalDate date, LocalTime start, LocalDate from, LocalDate due, String series) {
        return put(title, date, start, from, due, series, null);
    }

    /** 영어 단어: 11-16~11-20 매일 21:00 사본 5개. 11-18 사본은 그날 끝내 보상을 받았다(T-EDIT-43). */
    private void english() {
        for (int d = 16; d <= 20; d++) {
            put("영어 단어", day(11, d), LocalTime.of(21, 0), null, null, "S", d == 18 ? day(11, 18) : null);
        }
        book();
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

    /** 제목, 유형, 메모만 바꾼 수정 창의 값. */
    private static TaskDraft draft(Task t, String title, Category category, String memo) {
        return new TaskDraft(t.getKind(), title, category, t.getDate(), t.getDueDate(), t.getStartTime(),
                t.getDurationMinutes(), memo, t.getRepeat(), null, t.isNextDay(), null, null, true);
    }

    /** 영어 단어의 11-18 사본은 끝냄과 보상이 그대로다. */
    private void doneKept() {
        Task t = at(day(11, 18));
        assertEquals(List.of(true, 10, 10), List.of(t.isDone(), t.getRewardPoints(), t.getRewardExp()));
    }

    /** 수정 창의 값. 유형과 메모는 t의 값이다. 기간 할 일의 날짜 칸은 첫날이다. */
    private static TaskDraft draft(Task t, String title, LocalDate date, LocalDate due, LocalTime start, int minutes) {
        return new TaskDraft(t.getKind(), title, t.getCategory(), date, due, start, minutes, t.getMemo(), t.getRepeat(),
                null, false, null, null, true);
    }

    /** 시각 고치기 창의 값. 날짜는 day이고 minute은 앱 하루의 시작 분(D-77)이다. 나머지 칸은 t의 값이다. */
    private static TaskDraft time(Task t, LocalDate day, int minute, int minutes) {
        return new TaskDraft(t.getKind(), t.getTitle(), t.getCategory(), day, t.getDueDate(),
                LocalTime.of(minute % Task.DAY / 60, minute % 60), minutes, t.getMemo(), t.getRepeat(), null,
                minute >= Task.DAY, null, null, true, true);
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

    @Test
    void T_EDIT_11_dueThenPeriodOff() {
        today = day(11, 16);
        Task t = put("P", day(11, 16), null, day(11, 16), day(11, 22), null);
        book().update(t, draft(t, "P", day(11, 16), day(11, 29), null, 30), EditScope.ONLY_THIS);
        assertEquals(List.of(day(11, 16), day(11, 29)), List.of(t.getStartDay(), t.getDueDate()));
        book.update(t, draft(t, "P", day(11, 16), null, null, 30), EditScope.ONLY_THIS);
        assertEquals(Arrays.asList(null, null), Arrays.asList(t.getStartDay(), t.getDueDate()));
        assertEquals(List.of(t), book.dayPlan(day(11, 16)).untimed());
        assertTrue(book.dayPlan(day(11, 17)).isEmpty());
    }

    @Test
    void T_EDIT_14_laterDueKeepsPlacedTime() {
        today = day(11, 17);
        Task t = new Task("p", 1, Kind.TASK, "P", day(11, 18), LocalTime.of(9, 0), 90, "초안", false, Category.OTHER,
                null, null, day(11, 22), day(11, 16), false, null, 0, 0, null, null);
        tasks.add(t);
        book().update(t, draft(t, "P", day(11, 16), day(11, 24), null, 90), EditScope.ONLY_THIS);
        assertEquals(Arrays.asList(day(11, 16), day(11, 18), LocalTime.of(9, 0), 90, "초안", day(11, 24)),
                Arrays.asList(t.getStartDay(), t.getDate(), t.getStartTime(), t.getDurationMinutes(), t.getMemo(),
                        t.getDueDate()));
        assertEquals("~11/24까지", book.dueText(t));
        assertEquals(List.of(t), book.dayPlan(day(11, 18)).timed());
        assertTrue(book.dayPlan(day(11, 22)).isEmpty());
    }

    @Test
    void T_EDIT_41_pastDuePeriod() {
        Task t = put("Q", day(11, 10), null, day(11, 10), day(11, 16), null);
        book().update(t, draft(t, "Q2", day(11, 10), day(11, 16), null, 60), EditScope.ONLY_THIS);
        assertEquals(Arrays.asList("Q2", 60, day(11, 10), day(11, 10), day(11, 16)), Arrays.asList(t.getTitle(),
                t.getDurationMinutes(), t.getDate(), t.getStartDay(), t.getDueDate()));
        assertEquals(List.of(t), book.dayPlan(day(11, 16)).dueToday());
    }

    @Test
    void E_EDIT_4_copyNeverPeriod() {
        Task t = put("운동", day(11, 17), null, null, null, "S");
        book().update(t, draft(t, "운동", day(11, 17), day(11, 20), null, 30), EditScope.ONLY_THIS);
        assertEquals(Arrays.asList(null, null), Arrays.asList(t.getStartDay(), t.getDueDate()));
    }

    @Test
    void E_EDIT_5_periodOnDropsTime() {
        Task t = put("과제", day(11, 16), LocalTime.of(14, 0), null, null, null);
        book().update(t, draft(t, "과제", day(11, 16), day(11, 20), LocalTime.of(14, 0), 60), EditScope.ONLY_THIS);
        assertEquals(Arrays.asList(day(11, 16), day(11, 16), day(11, 20), null, false, 60), Arrays.asList(t.getDate(),
                t.getStartDay(), t.getDueDate(), t.getStartTime(), t.isNextDay(), t.getDurationMinutes()));
    }

    @Test
    void E_EDIT_6_untimedPeriodFirstDay() {
        Task t = put("P", day(11, 16), null, day(11, 16), day(11, 22), null);
        book().update(t, draft(t, "P", day(11, 18), day(11, 22), null, 30), EditScope.ONLY_THIS);
        assertEquals(List.of(day(11, 18), day(11, 18)), List.of(t.getDate(), t.getStartDay()));
    }

    @Test
    void E_EDIT_7_placedPeriodOff() {
        Task t = put("P", day(11, 19), LocalTime.of(14, 0), day(11, 16), day(11, 22), null);
        book().update(t, draft(t, "P", day(11, 20), null, LocalTime.of(15, 0), 30), EditScope.ONLY_THIS);
        assertEquals(Arrays.asList(day(11, 20), LocalTime.of(15, 0), null, null),
                Arrays.asList(t.getDate(), t.getStartTime(), t.getStartDay(), t.getDueDate()));
    }

    @Test
    void T_EDIT_1_doneTaskKeepsReward() {
        Task t = put("과제", day(11, 16), null, null, null, null, day(11, 16));
        book().update(t, draft(t, "과제", day(11, 20), null, null, 120), EditScope.ONLY_THIS);
        assertEquals(Arrays.asList(day(11, 20), 120, true, day(11, 16), 10, 10, day(11, 16)),
                Arrays.asList(t.getDate(), t.getDurationMinutes(), t.isDone(), t.getDoneDay(), t.getRewardPoints(),
                        t.getRewardExp(), t.getRewardDay()));
    }

    @Test
    void T_EDIT_10_taskRepeatAfter() {
        book().add(new TaskDraft(Kind.TASK, "운동", Category.OTHER, day(11, 16), null, null, 30, "", Repeat.DAILY,
                day(11, 22), false, null, null, false));
        Task t = at(day(11, 19));
        book.update(t, draft(t, "운동 30분", day(11, 19), null, null, 30), EditScope.THIS_AND_AFTER);
        assertEquals(List.of("운동", "운동", "운동", "운동 30분", "운동 30분", "운동 30분", "운동 30분"), each(Task::getTitle));
    }

    @Test
    void T_EDIT_43_doneCopyStaysInDialog() {
        english();
        Task t = at(day(11, 17));
        book.update(t, draft(t, "영어 단어 복습", Category.OTHER, "30개"), EditScope.THIS_AND_AFTER);
        String a = "영어 단어";
        String b = "영어 단어 복습";
        assertEquals(List.of(a, b, a, b, b), each(Task::getTitle));
        assertEquals(List.of("", "30개", "", "30개", "30개"), each(Task::getMemo));
        doneKept();
    }

    @Test
    void E_EDIT_11_editDoneCopy() {
        english();
        Task t = at(day(11, 18));
        book.update(t, draft(t, "복습", Category.STUDY, ""), EditScope.THIS_AND_AFTER);
        assertEquals(List.of("영어 단어", "영어 단어", "복습", "복습", "복습"), each(Task::getTitle));
        assertEquals(List.of(Category.OTHER, Category.OTHER, Category.STUDY, Category.STUDY, Category.STUDY),
                each(Task::getCategory));
        doneKept();
    }

    @Test
    void E_EDIT_1_kindDiffers() {
        Task t = put("과제", day(11, 16), null, null, null, null);
        TaskDraft fixed = new TaskDraft(Kind.FIXED, "바꿈", Category.OTHER, day(11, 17), null, LocalTime.of(10, 0), 60,
                "", null, null, false, null, null, true);
        assertThrows(IllegalArgumentException.class, () -> book().update(t, fixed, EditScope.ONLY_THIS));
        assertEquals(List.of("과제", day(11, 16), 30), List.of(t.getTitle(), t.getDate(), t.getDurationMinutes()));
    }

    @Test
    void E_EDIT_2_titleCategoryMemo() {
        Task t = put("과제", day(11, 16), null, null, null, null);
        book().update(t, draft(t, "  과제2  ", Category.EXERCISE, "준비물"), EditScope.ONLY_THIS);
        assertEquals(List.of("과제2", Category.EXERCISE, "준비물"), List.of(t.getTitle(), t.getCategory(), t.getMemo()));
    }

    @Test
    void E_EDIT_3_notCopyAfter() {
        Task t = put("과제", day(11, 16), null, null, null, null);
        put("과제", day(11, 17), null, null, null, null);
        book().update(t, draft(t, "새", Category.OTHER, ""), EditScope.THIS_AND_AFTER);
        assertEquals(List.of("새", "과제"), each(Task::getTitle));
    }

    @Test
    void E_EDIT_9_nextDayStartAfter() {
        weekly();
        Task t = at(day(11, 30));
        LocalTime one = LocalTime.of(1, 0);
        book.update(t, new TaskDraft(Kind.FIXED, "대학수학", Category.OTHER, day(11, 30), null, one, 75, "",
                t.getRepeat(), null, true, null, null, true), EditScope.THIS_AND_AFTER);
        assertEquals(List.of(T1030, T1030, one, one, one, one, one), each(Task::getStartTime));
        assertEquals(List.of(false, false, true, true, true, true, true), each(Task::isNextDay));
    }

    @Test
    void E_EDIT_12_notChecked() {
        Task t = put("과제", day(11, 16), null, null, null, null);
        book().update(t, draft(t, "  ", day(11, 16), null, null, 5), EditScope.ONLY_THIS);
        assertEquals(List.of("", 5), List.of(t.getTitle(), t.getDurationMinutes()));
    }

    @Test
    void T_EDIT_4_setTime() {
        Task t = put("과제", day(11, 18), null, null, null, null);
        book().update(t, time(t, day(11, 18), 14 * 60, 60), EditScope.ONLY_THIS);
        assertEquals(List.of(LocalTime.of(14, 0), 60), List.of(t.getStartTime(), t.getDurationMinutes()));
        assertEquals(List.of(t), book.dayPlan(day(11, 18)).timed());
    }

    @Test
    void T_EDIT_9_periodMovesToListDay() {
        Task t = put("P", day(11, 16), null, day(11, 16), day(11, 22), null);
        book().update(t, time(t, day(11, 18), 14 * 60, 30), EditScope.ONLY_THIS);
        assertEquals(List.of(day(11, 18), LocalTime.of(14, 0), day(11, 16), day(11, 22)),
                List.of(t.getDate(), t.getStartTime(), t.getStartDay(), t.getDueDate()));
        assertEquals(List.of(t), book.dayPlan(day(11, 18)).timed());
        assertTrue(book.dayPlan(day(11, 17)).isEmpty() && book.dayPlan(day(11, 19)).isEmpty());
    }

    @Test
    void T_EDIT_22_pastRowOpensToday() {
        Task t = put("지원서", day(11, 12), null, day(11, 12), day(11, 20), null);
        book().update(t, time(t, day(11, 18), 14 * 60, 30), EditScope.ONLY_THIS);
        assertEquals(List.of(day(11, 18), LocalTime.of(14, 0), day(11, 20)),
                List.of(t.getDate(), t.getStartTime(), t.getDueDate()));
    }

    @Test
    void E_EDIT_18_nextDayStart() {
        Task t = put("과제", day(11, 18), null, null, null, null);
        book().update(t, time(t, day(11, 18), 25 * 60, 30), EditScope.ONLY_THIS);
        assertEquals(List.of(LocalTime.of(1, 0), true), List.of(t.getStartTime(), t.isNextDay()));
    }

    @Test
    void T_EDIT_48_night4MovesLaterCopies() {
        english();
        Task t = at(day(11, 17));
        book.update(t, time(t, day(11, 18), 4 * 60, 30), EditScope.THIS_AND_AFTER);
        LocalTime nine = LocalTime.of(21, 0);
        LocalTime four = LocalTime.of(4, 0);
        assertEquals(List.of(day(11, 16), day(11, 18), day(11, 18), day(11, 20), day(11, 21)), each(Task::getDate));
        assertEquals(List.of(nine, four, nine, four, four), each(Task::getStartTime));
        assertEquals(List.of(false, false, true, false, false), each(Task::isDone));
    }

    @Test
    void T_EDIT_5_clearedJoinsUntimedBySeq() {
        put("a", day(11, 18), null, null, null, null);
        put("b", day(11, 19), null, null, null, null);
        Task t = put("c", day(11, 18), LocalTime.of(10, 0), null, null, null);
        put("d", day(11, 19), null, null, null, null);
        put("e", day(11, 18), null, null, null, null);
        book().clearTime(t, EditScope.ONLY_THIS);
        assertNull(t.getStartTime());
        assertEquals(List.of(1L, 3L, 5L), book.dayPlan(day(11, 18)).untimed().stream().map(Task::getSeq).toList());
    }

    @Test
    void T_EDIT_6_fixedClearTime() {
        Task t = new Task("f", 1, Kind.FIXED, "실습", day(11, 18), LocalTime.of(10, 0), 60, "", false, Category.OTHER,
                null, null, null, null, false, null, 0, 0, null, null);
        tasks.add(t);
        book().clearTime(t, EditScope.ONLY_THIS);
        assertEquals(LocalTime.of(10, 0), t.getStartTime());
    }

    @Test
    void T_EDIT_12_clearCopyTimes() {
        LocalTime s = LocalTime.of(7, 0);
        book().add(new TaskDraft(Kind.TASK, "운동", Category.OTHER, day(11, 16), null, s, 30, "", Repeat.DAILY,
                day(11, 22), false, null, null, false));
        book.clearTime(at(day(11, 18)), EditScope.ONLY_THIS);
        assertEquals(Arrays.asList(s, s, null, s, s, s, s), each(Task::getStartTime));
        book.clearTime(at(day(11, 19)), EditScope.THIS_AND_AFTER);
        assertEquals(Arrays.asList(s, s, null, null, null, null, null), each(Task::getStartTime));
    }

    @Test
    void T_EDIT_13_clearPeriodFromToday() {
        Task t = put("P", day(11, 20), LocalTime.of(14, 0), day(11, 16), day(11, 22), null);
        book().clearTime(t, EditScope.ONLY_THIS);
        assertEquals(Arrays.asList(null, day(11, 18), day(11, 16), day(11, 22)),
                Arrays.asList(t.getStartTime(), t.getDate(), t.getStartDay(), t.getDueDate()));
        for (int d = 18; d <= 21; d++) {
            assertEquals(List.of(t), book.dayPlan(day(11, d)).period());
        }
        assertEquals(List.of(t), book.dayPlan(day(11, 22)).dueToday());
        assertTrue(book.dayPlan(day(11, 16)).isEmpty() && book.dayPlan(day(11, 17)).isEmpty());
    }

    @Test
    void E_EDIT_20_clearPeriodNotStarted() {
        Task t = put("P", day(11, 21), LocalTime.of(10, 0), day(11, 20), day(11, 25), null);
        book().clearTime(t, EditScope.ONLY_THIS);
        assertEquals(day(11, 20), t.getDate());
    }

    @Test
    void E_EDIT_21_clearPeriodPastDue() {
        Task t = put("Q", day(11, 15), LocalTime.of(10, 0), day(11, 10), day(11, 16), null);
        book().clearTime(t, EditScope.ONLY_THIS);
        assertEquals(day(11, 16), t.getDate());
    }

    @Test
    void T_EDIT_43_doneCopyStaysInTimeEditor() {
        english();
        Task t = at(day(11, 17));
        book.update(t, time(t, day(11, 17), 22 * 60, 30), EditScope.THIS_AND_AFTER);
        LocalTime nine = LocalTime.of(21, 0);
        LocalTime ten = LocalTime.of(22, 0);
        assertEquals(List.of(nine, ten, nine, ten, ten), each(Task::getStartTime));
        book.clearTime(t, EditScope.THIS_AND_AFTER);
        assertEquals(Arrays.asList(nine, null, nine, null, null), each(Task::getStartTime));
        doneKept();
    }

    @Test
    void E_EDIT_13_laterTitleMemoKept() {
        english();
        Task u = at(day(11, 19));
        u.setTitle("다른 제목");
        u.setMemo("m");
        Task t = at(day(11, 17));
        book.update(t, time(t, day(11, 17), 22 * 60, 30), EditScope.THIS_AND_AFTER);
        assertEquals(List.of(LocalTime.of(22, 0), "다른 제목", "m"), List.of(u.getStartTime(), u.getTitle(), u.getMemo()));
    }

    @Test
    void E_EDIT_19_night4OnlyThis() {
        english();
        Task t = at(day(11, 17));
        book.update(t, time(t, day(11, 18), 4 * 60, 30), EditScope.ONLY_THIS);
        LocalTime nine = LocalTime.of(21, 0);
        assertEquals(List.of(day(11, 16), day(11, 18), day(11, 18), day(11, 19), day(11, 20)), each(Task::getDate));
        assertEquals(List.of(nine, LocalTime.of(4, 0), nine, nine, nine), each(Task::getStartTime));
    }

    @Test
    void E_EDIT_22_clearNotCopyAfter() {
        Task t = put("과제", day(11, 16), LocalTime.of(10, 0), null, null, null);
        put("과제", day(11, 17), LocalTime.of(10, 0), null, null, null);
        book().clearTime(t, EditScope.THIS_AND_AFTER);
        assertEquals(Arrays.asList(null, LocalTime.of(10, 0)), each(Task::getStartTime));
    }

    @Test
    void E_EDIT_23_clearDoneCopyAfter() {
        english();
        book.clearTime(at(day(11, 18)), EditScope.THIS_AND_AFTER);
        LocalTime nine = LocalTime.of(21, 0);
        assertEquals(Arrays.asList(nine, nine, null, null, null), each(Task::getStartTime));
        doneKept();
    }

    @Test
    void T_EDIT_42_pastDuePeriodTime() {
        Task t = put("Q", day(11, 10), null, day(11, 10), day(11, 16), null);
        book().update(t, time(t, day(11, 16), 10 * 60, 30), EditScope.ONLY_THIS);
        assertEquals(Arrays.asList(day(11, 10), null), Arrays.asList(t.getDate(), t.getStartTime()));
    }

    @Test
    void E_EDIT_14_doneTaskTime() {
        Task t = put("과제", day(11, 18), null, null, null, null, day(11, 18));
        book().update(t, time(t, day(11, 18), 14 * 60, 60), EditScope.ONLY_THIS);
        assertEquals(List.of(LocalTime.of(14, 0), 60, true), List.of(t.getStartTime(), t.getDurationMinutes(), t.isDone()));
    }

    @Test
    void E_EDIT_15_listDayBeforeToday() {
        Task t = put("P", day(11, 16), null, day(11, 16), day(11, 22), null);
        book().update(t, time(t, day(11, 17), 10 * 60, 30), EditScope.ONLY_THIS);
        assertEquals(Arrays.asList(day(11, 16), null), Arrays.asList(t.getDate(), t.getStartTime()));
    }

    @Test
    void E_EDIT_16_dueDay() {
        Task t = put("P", day(11, 16), null, day(11, 16), day(11, 18), null);
        book().update(t, time(t, day(11, 18), 14 * 60, 30), EditScope.ONLY_THIS);
        assertEquals(List.of(day(11, 18), LocalTime.of(14, 0)), List.of(t.getDate(), t.getStartTime()));
        assertEquals(List.of(t), book.dayPlan(day(11, 18)).timed());
    }

    @Test
    void E_EDIT_17_missedPlanSetAgain() {
        Task t = put("P", day(11, 17), LocalTime.of(10, 0), day(11, 16), day(11, 22), null);
        assertTrue(t.missed(today));
        book().update(t, time(t, day(11, 18), 14 * 60, 30), EditScope.ONLY_THIS);
        assertEquals(List.of(day(11, 18), LocalTime.of(14, 0), false), List.of(t.getDate(), t.getStartTime(), t.missed(today)));
        assertEquals(List.of(t), book.dayPlan(day(11, 18)).timed());
    }
}
