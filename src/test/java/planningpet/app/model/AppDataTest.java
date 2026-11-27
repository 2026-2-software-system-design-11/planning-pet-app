package planningpet.app.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.Test;
import planningpet.schedule.model.Category;
import planningpet.schedule.model.Kind;
import planningpet.schedule.model.Repeat;
import planningpet.schedule.model.Task;
import planningpet.schedule.model.TaskDraft;

/** 06 데이터 절 AppData와 docs/porting/app-data.md 2절의 경계. 따로 적지 않으면 지금은 11-18(수) 10:00이다. */
class AppDataTest {
    private final AppData data = at("2026-11-18T10:00");

    private static AppData at(String time) {
        ZoneId zone = ZoneId.systemDefault();
        return new AppData(Clock.fixed(LocalDateTime.parse(time).atZone(zone).toInstant(), zone));
    }

    @Test
    void E_APP_6_midnight() {
        assertEquals(LocalDate.of(2026, 11, 16), at("2026-11-17T00:00").today());
    }

    @Test
    void E_APP_7_endOfAppDay() {
        assertEquals(LocalDate.of(2026, 11, 16), at("2026-11-17T03:59").today());
    }

    @Test
    void E_APP_8_startOfAppDay() {
        assertEquals(LocalDate.of(2026, 11, 17), at("2026-11-17T04:00").today());
    }

    @Test
    void E_APP_9_monthChanges() {
        assertEquals(LocalDate.of(2026, 11, 30), at("2026-12-01T03:59").today());
    }

    @Test
    void E_APP_10_yearChanges() {
        assertEquals(LocalDate.of(2026, 12, 31), at("2027-01-01T03:59").today());
    }

    @Test
    void E_APP_15_bookTakesNextSeq() {
        LocalDate today = data.today();
        List<Task> one = data.getBook().add(new TaskDraft(Kind.TASK, "과제", Category.OTHER, today, null, null, 30, "",
                null, null, false, null, today, false));
        assertEquals(1, one.get(0).getSeq());
        assertEquals(2, data.getNextSeq());
        List<Task> copies = data.getBook().add(new TaskDraft(Kind.FIXED, "수업", Category.OTHER,
                LocalDate.of(2026, 11, 23), null, LocalTime.of(9, 0), 60, "", Repeat.weekly(DayOfWeek.MONDAY),
                LocalDate.of(2026, 12, 7), false, null, today, false));
        assertEquals(List.of(2L, 3L, 4L), copies.stream().map(Task::getSeq).toList());
        assertEquals(5, data.getNextSeq());
    }
}
