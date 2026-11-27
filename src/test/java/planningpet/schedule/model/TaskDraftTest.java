package planningpet.schedule.model;

import static java.time.DayOfWeek.MONDAY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;

/** 06 2번과 docs/porting/task-check.md, repeat.md 2절의 경계. */
class TaskDraftTest {
    private static final String EMOJI = new String(Character.toChars(0x1F600));

    /** minute은 앱 하루의 시작 분(D-77)이고 null이면 시각 없음이다. */
    private static TaskDraft draft(Kind kind, String title, LocalDate date, LocalDate due, Integer minute,
            int minutes, String memo, LocalDate placed, LocalDate today) {
        LocalTime start = minute == null ? null : LocalTime.of(minute % Task.DAY / 60, minute % 60);
        boolean nextDay = minute != null && minute >= Task.DAY;
        return new TaskDraft(kind, title, Category.OTHER, date, due, start, minutes, memo, null, null, nextDay, placed,
                today, false);
    }

    /** 11-16의 30분 반복. minute은 그날 시작 분이고 null이면 시각 없음이다. */
    private static TaskDraft repeat(Kind kind, Integer minute, LocalDate due, Repeat how, LocalDate end,
            boolean editing) {
        LocalTime start = minute == null ? null : LocalTime.of(minute / 60, minute % 60);
        return new TaskDraft(kind, "과제", Category.OTHER, day(11, 16), due, start, 30, "", how, end, false, null, null,
                editing);
    }

    private static TaskDraft titled(String title) {
        return draft(Kind.TASK, title, day(11, 16), null, null, 30, "", null, null);
    }

    private static TaskDraft memo(String memo) {
        return draft(Kind.TASK, "과제", day(11, 16), null, null, 30, memo, null, null);
    }

    private static TaskDraft timed(Kind kind, Integer minute, int minutes) {
        return draft(kind, "과제", day(11, 16), null, minute, minutes, "", null, null);
    }

    private static TaskDraft due(LocalDate date, LocalDate due, LocalDate placed, LocalDate today) {
        return draft(Kind.TASK, "과제", date, due, null, 30, "", placed, today);
    }

    private static LocalDate day(int month, int d) {
        return LocalDate.of(2026, month, d);
    }

    /** 오류마다 "칸: 문장". */
    private static List<String> errors(TaskDraft d) {
        return d.validate().stream().map(e -> e.field() + ": " + e.message()).toList();
    }

    @Test
    void T_VAL_1_spacesAround() {
        assertEquals(List.of(), errors(titled("  과제  ")));
    }

    @Test
    void T_VAL_2_onlySpaces() {
        assertEquals(List.of("제목: 제목을 써 주세요"), errors(titled("   ")));
    }

    @Test
    void T_VAL_3_titleLength() {
        assertEquals(List.of("제목: 제목은 100자까지예요"), errors(titled("가".repeat(101))));
        assertEquals(List.of(), errors(titled("가".repeat(100))));
    }

    @Test
    void E_VAL_3_hundredAfterStrip() {
        assertEquals(List.of(), errors(titled(" " + "가".repeat(100) + " ")));
    }

    @Test
    void E_VAL_4_ideographicSpace() {
        assertEquals(List.of("제목: 제목을 써 주세요"), errors(titled("　")));
    }

    @Test
    void E_VAL_5_noBreakSpace() {
        assertEquals(List.of(), errors(titled(" ")));
    }

    @Test
    void E_VAL_20_nullTitle() {
        assertThrows(NullPointerException.class, () -> titled(null).validate());
    }

    @Test
    void T_VAL_10_memoLength() {
        assertEquals(List.of("메모: 메모는 1000자까지예요"), errors(memo("가".repeat(1001))));
    }

    @Test
    void E_VAL_1_hundredEmojiTitle() {
        assertEquals(List.of(), errors(titled(EMOJI.repeat(100))));
        assertEquals(List.of("제목: 제목은 100자까지예요"), errors(titled(EMOJI.repeat(101))));
    }

    @Test
    void E_VAL_2_fiftyOneEmojiTitle() {
        assertEquals(List.of(), errors(titled(EMOJI.repeat(51))));
    }

    @Test
    void E_VAL_6_memoThousand() {
        assertEquals(List.of(), errors(memo("가".repeat(1000))));
    }

    @Test
    void E_VAL_7_thousandEmojiMemo() {
        assertEquals(List.of(), errors(memo(EMOJI.repeat(1000))));
        assertEquals(List.of("메모: 메모는 1000자까지예요"), errors(memo(EMOJI.repeat(1001))));
    }

    @Test
    void T_VAL_4_taskWithoutStart() {
        assertEquals(List.of(), errors(timed(Kind.TASK, null, 30)));
    }

    @Test
    void T_VAL_5_fixedWithoutStart() {
        assertEquals(List.of("시작 시각: 고정 일정은 시작 시각이 있어야 해요"), errors(timed(Kind.FIXED, null, 30)));
    }

    @Test
    void T_VAL_6_offStep() {
        assertEquals(List.of("시작 시각: 5분 단위로 정해 주세요"), errors(timed(Kind.TASK, 9 * 60 + 3, 30)));
    }

    @Test
    void T_VAL_7_endsAtMidnight() {
        assertEquals(List.of(), errors(timed(Kind.TASK, 23 * 60 + 30, 30)));
    }

    @Test
    void T_VAL_8_endsAfterFour() {
        assertEquals(List.of("시작 시각: 다음 날 04:00까지 끝나야 해요"), errors(timed(Kind.TASK, 27 * 60 + 30, 40)));
        assertEquals(List.of(), errors(timed(Kind.TASK, 27 * 60 + 30, 30)));
    }

    @Test
    void T_VAL_20_pastMidnight() {
        assertEquals(List.of(), errors(timed(Kind.TASK, 23 * 60 + 30, 40)));
        assertEquals(List.of(), errors(timed(Kind.TASK, 23 * 60 + 30, 90)));
    }

    @Test
    void T_VAL_9_duration() {
        for (int bad : new int[] {0, 27, 730}) {
            assertEquals(List.of("걸리는 시간: 10분부터 720분까지, 5분 단위예요"), errors(timed(Kind.TASK, null, bad)));
        }
        for (int ok : new int[] {10, 75, 720}) {
            assertEquals(List.of(), errors(timed(Kind.TASK, null, ok)));
        }
    }

    @Test
    void E_VAL_8_firstMinute() {
        assertEquals(List.of(), errors(timed(Kind.TASK, 0, 30)));
    }

    @Test
    void E_VAL_9_endsExactlyAtFour() {
        assertEquals(List.of(), errors(timed(Kind.TASK, 27 * 60 + 50, 10)));
    }

    @Test
    void E_VAL_10_latestStart() {
        assertEquals(List.of("시작 시각: 다음 날 04:00까지 끝나야 해요"), errors(timed(Kind.TASK, 27 * 60 + 55, 10)));
    }

    @Test
    void E_VAL_11_longestEndsAtFour() {
        assertEquals(List.of(), errors(timed(Kind.TASK, 16 * 60, 720)));
    }

    @Test
    void E_VAL_12_longestFiveLate() {
        assertEquals(List.of("시작 시각: 다음 날 04:00까지 끝나야 해요"), errors(timed(Kind.TASK, 16 * 60 + 5, 720)));
    }

    @Test
    void E_VAL_15_durationEdges() {
        assertEquals(List.of("걸리는 시간: 10분부터 720분까지, 5분 단위예요"), errors(timed(Kind.TASK, null, 5)));
        assertEquals(List.of(), errors(timed(Kind.TASK, null, 715)));
        assertEquals(List.of("걸리는 시간: 10분부터 720분까지, 5분 단위예요"), errors(timed(Kind.TASK, null, -5)));
    }

    @Test
    void T_VAL_21_bothProblems() {
        String both = "시작 시각: 다음 날 04:00까지 끝나야 해요. 5분 단위로 정해 주세요. ";
        String duration = "걸리는 시간: 10분부터 720분까지, 5분 단위예요";
        assertEquals(List.of(both + "03:50(다음 날)이면 둘 다 맞아요"), errors(timed(Kind.TASK, 27 * 60 + 53, 10)));
        assertEquals(List.of(both + "걸리는 시간 25분이면 03:35(다음 날)이 맞아요", duration),
                errors(timed(Kind.TASK, 27 * 60 + 53, 27)));
        assertEquals(List.of(both + "걸리는 시간 720분이면 16:00이 맞아요", duration),
                errors(timed(Kind.TASK, 27 * 60 + 53, 730)));
    }

    @Test
    void T_VAL_14_errorOrder() {
        TaskDraft d = draft(Kind.TASK, "", day(11, 16), null, 27 * 60 + 30, 40, "", null, null);
        assertEquals(List.of("제목: 제목을 써 주세요", "시작 시각: 다음 날 04:00까지 끝나야 해요"), errors(d));
    }

    @Test
    void E_VAL_13_bothProblemsSameDay() {
        assertEquals(List.of("시작 시각: 다음 날 04:00까지 끝나야 해요. 5분 단위로 정해 주세요. 16:00이면 둘 다 맞아요"),
                errors(timed(Kind.TASK, 16 * 60 + 3, 720)));
    }

    @Test
    void E_VAL_14_offStepZeroDuration() {
        assertEquals(List.of("시작 시각: 5분 단위로 정해 주세요", "걸리는 시간: 10분부터 720분까지, 5분 단위예요"),
                errors(timed(Kind.TASK, 27 * 60 + 53, 0)));
    }

    @Test
    void T_VAL_17_dueBeforeStart() {
        List<String> before = List.of("마감: 마감일이 시작일보다 전이에요");
        assertEquals(before, errors(due(day(11, 18), day(11, 17), null, null)));
        assertEquals(List.of(), errors(due(day(11, 18), day(11, 18), null, null)));
        assertEquals(before, errors(due(day(11, 18), day(11, 17), null, day(11, 18))));
    }

    @Test
    void T_VAL_19_fixedWithDue() {
        TaskDraft d = draft(Kind.FIXED, "수업", day(11, 16), day(11, 22), 600, 60, "", null, null);
        assertEquals(List.of("마감: 기간은 할 일만 정할 수 있어요"), errors(d));
    }

    @Test
    void T_VAL_22_dueBeforePlaced() {
        LocalDate placed = day(11, 19);
        assertEquals(List.of("마감: 마감일이 시각을 정해 둔 11/19(목)보다 전이에요"),
                errors(due(day(11, 17), day(11, 18), placed, day(11, 18))));
        assertEquals(List.of(), errors(due(day(11, 17), day(11, 19), placed, day(11, 18))));
        assertEquals(List.of("마감: 마감일이 시작일보다 전이에요"), errors(due(day(11, 17), day(11, 16), placed, day(11, 18))));
    }

    @Test
    void T_VAL_23_dueBeforeToday() {
        LocalDate today = day(11, 18);
        List<String> beforeToday = List.of("마감: 마감일이 오늘보다 전이에요");
        assertEquals(beforeToday, errors(due(day(11, 10), day(11, 17), null, today)));
        assertEquals(List.of("마감: 마감일이 시작일보다 전이에요"), errors(due(day(11, 10), day(11, 9), null, today)));
        assertEquals(List.of(), errors(due(day(11, 10), day(11, 18), null, today)));
        assertEquals(beforeToday, errors(due(day(11, 16), day(11, 17), null, today)));
        assertEquals(beforeToday, errors(due(day(11, 15), day(11, 16), null, today)));
        assertEquals(List.of(), errors(due(day(11, 10), day(11, 16), null, null)));
    }

    @Test
    void T_VAL_24_startAfterPlaced() {
        LocalDate placed = day(11, 19);
        assertEquals(List.of("마감: 시작일이 시각을 정해 둔 11/19(목)보다 후예요"),
                errors(due(day(11, 20), day(11, 22), placed, day(11, 18))));
        assertEquals(List.of("마감: 마감일이 시작일보다 전이에요"), errors(due(day(11, 20), day(11, 18), placed, day(11, 18))));
        assertEquals(List.of(), errors(due(day(11, 19), day(11, 22), placed, day(11, 18))));
    }

    @Test
    void E_VAL_16_fixedNoStartWithDue() {
        TaskDraft d = draft(Kind.FIXED, "수업", day(11, 16), day(11, 22), null, 60, "", null, null);
        assertEquals(List.of("마감: 기간은 할 일만 정할 수 있어요", "시작 시각: 고정 일정은 시작 시각이 있어야 해요"), errors(d));
    }

    @Test
    void E_VAL_17_dueNextYear() {
        assertEquals(List.of(), errors(due(day(12, 31), LocalDate.of(2027, 1, 1), null, day(12, 31))));
    }

    @Test
    void E_VAL_18_placedNextYear() {
        assertEquals(List.of("마감: 마감일이 시각을 정해 둔 1/2(토)보다 전이에요"),
                errors(due(day(12, 30), day(12, 31), LocalDate.of(2027, 1, 2), day(12, 30))));
    }

    @Test
    void E_VAL_19_everyFieldWrong() {
        TaskDraft d = draft(Kind.FIXED, "", day(11, 18), day(11, 17), null, 0, "가".repeat(1001), null, null);
        assertEquals(List.of("제목", "마감", "시작 시각", "걸리는 시간", "메모"),
                d.validate().stream().map(FieldError::field).toList());
    }

    @Test
    void T_VAL_11_taskRepeatWithoutStart() {
        assertEquals(List.of(), errors(repeat(Kind.TASK, null, null, Repeat.DAILY, day(12, 13), false)));
    }

    @Test
    void T_VAL_12_endBeforeDate() {
        List<String> before = List.of("반복: 반복 끝나는 날이 날짜보다 전이에요");
        assertEquals(before, errors(repeat(Kind.FIXED, 600, null, Repeat.weekly(MONDAY), day(11, 15), false)));
        assertEquals(before, errors(repeat(Kind.FIXED, 600, null, Repeat.DAILY, day(11, 15), false)));
    }

    @Test
    void T_VAL_15_noDayPicked() {
        assertEquals(List.of("반복: 요일을 하나 이상 골라 주세요"),
                errors(repeat(Kind.TASK, null, null, Repeat.weekly(), day(11, 30), false)));
    }

    @Test
    void T_VAL_18_periodRepeat() {
        assertEquals(List.of("반복: 기간 안에 하는 할 일은 반복할 수 없어요"),
                errors(repeat(Kind.TASK, null, day(11, 22), Repeat.DAILY, day(11, 22), false)));
    }

    @Test
    void E_VAL_21_editingEndBeforeDate() {
        assertEquals(List.of(), errors(repeat(Kind.TASK, null, null, Repeat.DAILY, day(11, 15), true)));
    }

    @Test
    void E_VAL_22_editingNoDay() {
        assertEquals(List.of(), errors(repeat(Kind.TASK, null, null, Repeat.weekly(), day(11, 30), true)));
    }

    @Test
    void E_VAL_25_noDayAndEndBefore() {
        assertEquals(List.of("반복: 요일을 하나 이상 골라 주세요"),
                errors(repeat(Kind.TASK, null, null, Repeat.weekly(), day(11, 15), false)));
    }

    @Test
    void E_VAL_26_periodAndNoDay() {
        assertEquals(List.of("반복: 기간 안에 하는 할 일은 반복할 수 없어요"),
                errors(repeat(Kind.TASK, null, day(11, 22), Repeat.weekly(), day(11, 15), false)));
    }

    @Test
    void E_VAL_27_noEnd() {
        assertThrows(NullPointerException.class, () -> repeat(Kind.TASK, null, null, Repeat.DAILY, null, false).validate());
    }

    @Test
    void E_VAL_28_editingPeriodRepeat() {
        assertEquals(List.of(), errors(repeat(Kind.TASK, null, day(11, 22), Repeat.DAILY, day(11, 22), true)));
    }
}
