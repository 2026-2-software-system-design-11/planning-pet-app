package planningpet.schedule.model;

import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.SUNDAY;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

/** 06 14번과 docs/porting/screen-calc.md 2절의 E-DEF 경계. */
class DefaultDatesTest {
    private static LocalDate d(String date) {
        return LocalDate.parse(date.length() == 5 ? "2026-" + date : date);
    }

    /** defaultStart(date, now)의 분. now는 "11-16T15:23"처럼 적고 해가 다르면 "2027-01-01T00:30"이다. */
    private static int start(String date, String now) {
        return DefaultDates.defaultStart(d(date), LocalDateTime.parse(now.startsWith("20") ? now : "2026-" + now));
    }

    private static void due(String date, DayOfWeek weekStart, String expected) {
        assertEquals(d(expected), DefaultDates.dueFor(d(date), weekStart));
    }

    private static void schedule(String shown, String today, String expected) {
        assertEquals(d(expected), DefaultDates.forAdd(Screen.SCHEDULE, null, d(shown), d(today)));
    }

    @Test
    void T_DEF_1_main() {
        assertEquals(d("11-20"), DefaultDates.forAdd(Screen.MAIN, d("11-20"), null, d("11-16")));
    }

    @Test
    void T_DEF_2_todayInShownWeek() {
        schedule("11-16", "11-18", "11-18");
    }

    @Test
    void T_DEF_3_nextWeek() {
        schedule("11-23", "11-18", "11-23");
    }

    @Test
    void T_DEF_4_pastWeek() {
        schedule("11-09", "11-18", "11-09");
    }

    @Test
    void E_DEF_1_todayFirstAndLastDay() {
        schedule("11-16", "11-16", "11-16");
        schedule("11-16", "11-22", "11-22");
    }

    @Test
    void E_DEF_2_todayJustOutside() {
        schedule("11-16", "11-15", "11-16");
        schedule("11-16", "11-23", "11-16");
    }

    @Test
    void E_DEF_3_weekAcrossYear() {
        schedule("12-28", "2027-01-01", "2027-01-01");
    }

    @Test
    void E_DEF_4_mainPastDay() {
        assertEquals(d("11-10"), DefaultDates.forAdd(Screen.MAIN, d("11-10"), null, d("11-18")));
    }

    @Test
    void T_DEF_6_todayNow() {
        assertEquals(925, start("11-16", "11-16T15:23"));
    }

    @Test
    void T_DEF_7_todayBeforeNine() {
        assertEquals(540, start("11-16", "11-16T07:00"));
    }

    @Test
    void T_DEF_8_otherDay() {
        assertEquals(540, start("11-17", "11-16T15:23"));
    }

    @Test
    void T_DEF_9_pastMidnight() {
        assertEquals(1440, start("11-16", "11-16T23:57"));
    }

    @Test
    void T_DEF_14_lateNight() {
        assertEquals(1530, start("11-20", "11-21T01:28"));
        assertEquals(540, start("11-21", "11-21T01:28"));
        assertEquals(1675, start("11-20", "11-21T03:58"));
    }

    @Test
    void E_DEF_5_nowOnAndAfterFiveMinutes() {
        assertEquals(925, start("11-16", "11-16T15:25"));
        assertEquals(925, start("11-16", "11-16T15:21"));
    }

    @Test
    void E_DEF_6_nowWithSeconds() {
        assertEquals(930, start("11-16", "11-16T15:25:30"));
    }

    @Test
    void E_DEF_7_aroundNine() {
        assertEquals(540, start("11-16", "11-16T08:56"));
        assertEquals(540, start("11-16", "11-16T09:00"));
        assertEquals(545, start("11-16", "11-16T09:01"));
    }

    @Test
    void E_DEF_8_justBeforeMidnight() {
        assertEquals(1435, start("11-16", "11-16T23:55"));
        assertEquals(1440, start("11-16", "11-16T23:59:30"));
    }

    @Test
    void E_DEF_9_midnight() {
        assertEquals(1440, start("11-20", "11-21T00:00"));
        assertEquals(540, start("11-21", "11-21T00:00"));
    }

    @Test
    void E_DEF_10_aroundLastStart() {
        assertEquals(1670, start("11-20", "11-21T03:50"));
        assertEquals(1675, start("11-20", "11-21T03:51"));
        assertEquals(1675, start("11-20", "11-21T03:59:59"));
    }

    @Test
    void E_DEF_11_endOfAppDay() {
        assertEquals(540, start("11-20", "11-21T04:00"));
        assertEquals(540, start("11-21", "11-21T04:00"));
    }

    @Test
    void E_DEF_12_pastDay() {
        assertEquals(540, start("11-15", "11-16T10:00"));
        assertEquals(540, start("11-14", "11-16T01:00"));
    }

    @Test
    void E_DEF_13_nightAcrossYear() {
        assertEquals(1470, start("12-31", "2027-01-01T00:30"));
    }

    @Test
    void T_DEF_5_fixedRepeatEnd() {
        assertEquals(d("2027-03-01"), DefaultDates.repeatEndFor(Kind.FIXED, d("11-16")));
    }

    @Test
    void T_DEF_10_taskRepeatEnd() {
        assertEquals(d("12-14"), DefaultDates.repeatEndFor(Kind.TASK, d("11-16")));
    }

    @Test
    void E_DEF_14_repeatEndAcrossMonthAndYear() {
        assertEquals(d("2027-01-28"), DefaultDates.repeatEndFor(Kind.TASK, d("12-31")));
        assertEquals(d("06-13"), DefaultDates.repeatEndFor(Kind.FIXED, d("02-28")));
    }

    @Test
    void T_DEF_11_dueMondayStart() {
        due("11-18", MONDAY, "11-22");
        due("11-22", MONDAY, "11-29");
    }

    @Test
    void T_DEF_15_dueSundayStart() {
        due("11-18", SUNDAY, "11-21");
        due("11-21", SUNDAY, "11-28");
        due("11-22", SUNDAY, "11-28");
        schedule("11-22", "11-18", "11-22");
    }

    @Test
    void E_DEF_15_dueFromFirstDay() {
        due("11-16", MONDAY, "11-22");
        due("11-15", SUNDAY, "11-21");
    }

    @Test
    void E_DEF_16_dueAcrossMonthAndYear() {
        due("12-31", MONDAY, "2027-01-03");
        due("11-29", MONDAY, "12-06");
        due("2027-01-02", SUNDAY, "2027-01-09");
    }
}
