package planningpet.schedule.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/** 06 26번과 docs/porting/screen-calc.md 2절의 E-CAL 경계. */
class CalendarCellTest {
    private LocalDate today = day(14);
    private final List<Task> tasks = new ArrayList<>();

    private static LocalDate day(int d) {
        return LocalDate.of(2026, 11, d);
    }

    /** 11월 date일의 일정. start는 "10:00"이고 null이면 시각 없음, doneDay가 0보다 크면 그날 끝냈다. */
    private Task put(Kind kind, String title, int date, String start, int doneDay) {
        return add(kind, title, date, start, null, 0, 0, doneDay);
    }

    /** 첫날 from, 마감 due인 기간 할 일을 date의 start(null이면 시각 없음)에 둔 것. */
    private Task plan(String title, int from, int due, int date, String start, int doneDay) {
        return add(Kind.TASK, title, date, start, null, from, due, doneDay);
    }

    /** series가 있으면 매일 반복의 사본이고, due가 0보다 크면 기간 할 일이다. */
    private Task add(Kind kind, String title, int date, String start, String series, int from, int due, int doneDay) {
        Task t = new Task("id" + tasks.size(), tasks.size() + 1, kind, title, day(date),
                start == null ? null : LocalTime.parse(start), 30, "", false, Category.OTHER, series,
                series == null ? null : Repeat.DAILY, due > 0 ? day(due) : null, due > 0 ? day(from) : null,
                doneDay > 0, doneDay > 0 ? day(doneDay) : null, 0, 0, null, null);
        tasks.add(t);
        return t;
    }

    /** "4개 진함 마감 []"처럼 칸 글(셀 일이 없으면 "수 없음"), 진한지, 마감 제목. 비어 있으면 "비어 있음". */
    private String cell(int date, boolean withFixed) {
        CellPreview p = CalendarCell.preview(new TaskBook(tasks, () -> tasks.size() + 1L, () -> today), day(date),
                withFixed);
        return p.isEmpty() ? "비어 있음" : (p.count() == 0 ? "수 없음" : p.text()) + " " + (p.open() ? "진함" : "흐림")
                + " 마감 " + p.due();
    }

    @Test
    void T_CAL_1_doneMixed() {
        put(Kind.TASK, "알고리즘 과제 2번", 14, "10:00", 14);
        put(Kind.TASK, "자소서 초안", 14, "14:00", 0);
        put(Kind.TASK, "토익 단어", 14, null, 0);
        put(Kind.TASK, "운동", 14, null, 0);
        assertEquals("4개 진함 마감 []", cell(14, false));
    }

    @Test
    void T_CAL_2_fixedOnly() {
        put(Kind.FIXED, "수업", 16, "09:00", 0);
        put(Kind.FIXED, "실습", 16, "13:00", 0);
        assertEquals("비어 있음", cell(16, false));
        assertEquals("2개 진함 마감 []", cell(16, true));
    }

    @Test
    void T_CAL_3_allDone() {
        put(Kind.TASK, "토익 스터디", 13, null, 13);
        assertEquals("1개 흐림 마감 []", cell(13, false));
    }

    @Test
    void T_CAL_4_comingDay() {
        put(Kind.TASK, "은행 서류", 15, null, 0);
        assertEquals("1개 진함 마감 []", cell(15, false));
    }

    @Test
    void T_CAL_6_pastRepeatCopy() {
        today = day(18);
        for (int d = 16; d <= 22; d++) {
            add(Kind.TASK, "운동", d, null, "s", 0, 0, 0);
        }
        assertEquals("1개 흐림 마감 []", cell(17, false));
        assertEquals("1개 진함 마감 []", cell(18, false));
    }

    @Test
    void T_CAL_5_periodDueDay() {
        today = day(16);
        plan("E사 지원서", 16, 22, 16, null, 0);
        assertEquals("비어 있음", cell(16, false));
        assertEquals("1개 진함 마감 [E사 지원서]", cell(22, false));
    }

    @Test
    void T_CAL_8_placedPeriod() {
        today = day(17);
        plan("P", 16, 22, 18, "09:00", 0);
        assertEquals("1개 진함 마감 []", cell(18, false));
        assertEquals("수 없음 흐림 마감 [P]", cell(22, false));
        assertEquals("", CalendarCell.preview(new TaskBook(tasks, () -> 9L, () -> today), day(22), false).text());
    }

    @Test
    void T_CAL_7_fixedSetting() {
        today = day(18);
        put(Kind.FIXED, "수업", 16, "09:00", 0);
        put(Kind.FIXED, "수업", 19, "09:00", 0);
        put(Kind.TASK, "과제", 19, null, 0);
        assertEquals("비어 있음", cell(16, false));
        assertEquals("1개 진함 마감 []", cell(19, false));
        assertEquals("1개 흐림 마감 []", cell(16, true));
        assertEquals("2개 진함 마감 []", cell(19, true));
    }

    @Test
    void E_CAL_1_emptyDay() {
        today = day(18);
        assertEquals("비어 있음", cell(18, false));
        assertEquals("비어 있음", cell(18, true));
    }

    @Test
    void E_CAL_2_twoDueAndDoneDue() {
        today = day(16);
        plan("B", 16, 22, 16, null, 0);
        plan("A", 18, 22, 18, null, 0);
        plan("C", 16, 22, 16, null, 16);
        assertEquals("2개 진함 마감 [B, A]", cell(22, false));
    }

    @Test
    void E_CAL_3_donePeriod() {
        today = day(18);
        plan("P", 16, 22, 16, null, 17);
        assertEquals("비어 있음", cell(16, false));
        assertEquals("1개 흐림 마감 []", cell(17, false));
        assertEquals("비어 있음", cell(22, false));
    }

    @Test
    void E_CAL_4_pastDuePeriod() {
        today = day(25);
        plan("P", 16, 22, 16, null, 0);
        assertEquals("비어 있음", cell(16, false));
        assertEquals("1개 진함 마감 [P]", cell(22, false));
    }

    @Test
    void E_CAL_5_pastMidnight() {
        today = day(21);
        tasks.add(new Task("r", 1, Kind.TASK, "복습", day(20), LocalTime.of(1, 30), 30, "", true, Category.OTHER, null,
                null, null, null, false, null, 0, 0, null, null));
        assertEquals("1개 진함 마감 []", cell(20, false));
        assertEquals("비어 있음", cell(21, false));
    }

    @Test
    void E_CAL_6_fixedToday() {
        today = day(18);
        put(Kind.FIXED, "수업", 18, "09:00", 0);
        assertEquals("1개 진함 마감 []", cell(18, true));
        assertEquals("비어 있음", cell(18, false));
    }

    @Test
    void E_CAL_7_missedPlan() {
        today = day(18);
        plan("P", 16, 22, 17, "09:00", 0);
        assertEquals("비어 있음", cell(17, false));
        assertEquals("비어 있음", cell(18, false));
        assertEquals("1개 진함 마감 [P]", cell(22, false));
    }
}
