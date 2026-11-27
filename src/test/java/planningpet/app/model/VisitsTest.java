package planningpet.app.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;

/** 06 12번과 docs/porting/rewards.md 2절의 E-VIS 경계. */
class VisitsTest {
    private static final ZoneId ZONE = ZoneId.systemDefault();

    /** 마지막으로 브리핑한 날과 연속 일수를 넣고 시계가 now일 때 recordOpen한 결과와 그 뒤 마지막 날. */
    private static String open(LocalDate last, int streak, String now) {
        AppData data = new AppData(Clock.fixed(LocalDateTime.parse(now).atZone(ZONE).toInstant(), ZONE));
        data.setLastOpenDay(last);
        data.setStreak(streak);
        return new Visits(data).recordOpen(data.today()) + " " + data.getLastOpenDay() + " " + data.getStreak();
    }

    private static LocalDate d(int month, int day) { return LocalDate.of(2026, month, day); }

    @Test
    void T_VIS_1_firstRun() {
        assertEquals("1 2026-11-16 1", open(null, 0, "2026-11-16T10:00"));
    }

    @Test
    void T_VIS_2_yesterday() {
        assertEquals("6 2026-11-16 6", open(d(11, 15), 5, "2026-11-16T10:00"));
    }

    @Test
    void T_VIS_3_today() {
        assertEquals("6 2026-11-16 6", open(d(11, 16), 6, "2026-11-16T10:00"));
    }

    @Test
    void T_VIS_4_gap() {
        assertEquals("1 2026-11-16 1", open(d(11, 13), 6, "2026-11-16T10:00"));
    }

    @Test
    void T_VIS_5_newYear() {
        assertEquals("4 2027-01-01 4", open(d(12, 31), 3, "2027-01-01T10:00"));
    }

    @Test
    void E_VIS_1_newMonth() {
        assertEquals("3 2026-12-01 3", open(d(11, 30), 2, "2026-12-01T10:00"));
    }

    @Test
    void E_VIS_2_lastDayAfterToday() {
        assertEquals("1 2026-11-16 1", open(d(11, 17), 4, "2026-11-16T10:00"));
    }

    @Test
    void E_VIS_3_beforeFourAm() {
        assertEquals("3 2026-11-17 3", open(d(11, 16), 2, "2026-11-18T03:59"));
    }
}
