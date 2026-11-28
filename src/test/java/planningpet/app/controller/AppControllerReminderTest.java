package planningpet.app.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import planningpet.app.model.AppData;
import planningpet.app.model.Apps;
import planningpet.app.model.MemoryStore;
import planningpet.schedule.model.Reminder;
import planningpet.schedule.model.Tasks;

/** 06 17번을 부르는 dueReminders. 번호는 docs/porting/tray.md 2절. 할 일은 11-16 15:00 "통계 과제"다. */
class AppControllerReminderTest {
    private final AppData data = Apps.data("2026-11-16T14:50", Tasks.task("통계 과제", Tasks.d(16)).at("15:00").task());
    private final AppController app = new AppController(data, new MemoryStore(), e -> { });

    static List<String> texts(AppController app, String time) {
        return app.dueReminders(LocalDateTime.parse("2026-11-16T" + time)).stream().map(Reminder::text).toList();
    }

    @Test
    void E_NTF_8_sentKeptWhileRunning() {
        assertEquals(List.of("통계 과제 10분 뒤 시작"), texts(app, "14:50:00"));
        assertEquals(List.of(), texts(app, "14:50:30"));
    }

    @Test
    void E_NTF_9_restartedAppRemindsAgain() {
        texts(app, "14:50:00");
        AppController again = new AppController(data, new MemoryStore(), e -> { });
        assertEquals(List.of("통계 과제 5분 뒤 시작"), texts(again, "14:55:00"));
    }
}
