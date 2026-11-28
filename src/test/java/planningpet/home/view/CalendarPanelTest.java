package planningpet.home.view;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Component;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import planningpet.app.model.AppData;

/** 홈 달력의 몸. 번호는 docs/porting/home.md S3f 2절. */
class CalendarPanelTest {
    private static final ZoneId ZONE = ZoneId.systemDefault();
    private AppData data;

    private CalendarPanel panel(String now, DayOfWeek start) {
        data = new AppData(Clock.fixed(LocalDateTime.parse(now).atZone(ZONE).toInstant(), ZONE));
        data.setWeekStart(start);
        CalendarPanel c = new CalendarPanel(data, day -> { });
        c.refresh();
        return c;
    }

    /** 칸 수와 날짜 칸이 아닌 빈 자리. */
    private static String layout(CalendarPanel c) {
        Component[] cs = c.grid.getComponents();
        List<Integer> empty = new ArrayList<>();
        for (int i = 0; i < cs.length; i++) {
            if (!(cs[i] instanceof DayCell)) {
                empty.add(i);
            }
        }
        return cs.length + " empty " + empty;
    }

    @Test
    void eMon1MondayStart() {
        CalendarPanel c = panel("2026-11-16T10:00", DayOfWeek.MONDAY);
        assertEquals("42 empty [0, 1, 2, 3, 4, 5, 36, 37, 38, 39, 40, 41]", layout(c));
    }
}
