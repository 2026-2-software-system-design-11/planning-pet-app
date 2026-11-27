package planningpet.schedule.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;

/** 06 2번(반복 칸 빼고)과 docs/porting/task-check.md 2절의 경계. */
class TaskDraftTest {
    private static final String EMOJI = new String(Character.toChars(0x1F600));

    /** minute은 앱 하루의 시작 분(D-77)이고 null이면 시각 없음이다. */
    private static TaskDraft draft(Kind kind, String title, LocalDate date, LocalDate due, Integer minute,
            int minutes, String memo, LocalDate placed, LocalDate today) {
        LocalTime start = minute == null ? null : LocalTime.of(minute % Task.DAY / 60, minute % 60);
        boolean nextDay = minute != null && minute >= Task.DAY;
        return new TaskDraft(kind, title, Category.OTHER, date, due, start, minutes, memo, nextDay, placed, today);
    }

    private static TaskDraft titled(String title) {
        return draft(Kind.TASK, title, day(11, 16), null, null, 30, "", null, null);
    }

    private static TaskDraft memo(String memo) {
        return draft(Kind.TASK, "과제", day(11, 16), null, null, 30, memo, null, null);
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
        assertEquals(List.of("제목: 제목은 100자까지예요"), errors(titled(EMOJI.repeat(100))));
    }

    @Test
    void E_VAL_2_fiftyOneEmojiTitle() {
        assertEquals(List.of("제목: 제목은 100자까지예요"), errors(titled(EMOJI.repeat(51))));
    }

    @Test
    void E_VAL_6_memoThousand() {
        assertEquals(List.of(), errors(memo("가".repeat(1000))));
    }

    @Test
    void E_VAL_7_thousandEmojiMemo() {
        assertEquals(List.of("메모: 메모는 1000자까지예요"), errors(memo(EMOJI.repeat(1000))));
    }
}
