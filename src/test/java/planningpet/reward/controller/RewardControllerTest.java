package planningpet.reward.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import planningpet.app.controller.AppController;
import planningpet.app.model.AppData;
import planningpet.app.model.DataStore;
import planningpet.app.model.LoadResult;

/** 06 11번을 controller로 부르는 docs/porting/app-window.md 2절의 E-ATT 경계. 시계는 테스트가 옮긴다. */
class RewardControllerTest {
    private static final ZoneId ZONE = ZoneId.systemDefault();
    private LocalDateTime now = LocalDateTime.of(2026, 11, 16, 10, 0);
    private final AppData data = new AppData(new Clock() {
        public ZoneId getZone() { return ZONE; }
        public Clock withZone(ZoneId zone) { return this; }
        public Instant instant() { return now.atZone(ZONE).toInstant(); }
    });
    private final List<String> errors = new ArrayList<>();
    private int saves;
    private int heard;
    private boolean failing;
    private final AppController app = new AppController(data, new DataStore() {
        public LoadResult load() { return LoadResult.firstRun(); }
        public void save(AppData d) throws IOException {
            if (failing) {
                throw new IOException("디스크가 찼다");
            }
            saves++;
        }
    }, errors::add);
    private final RewardController reward = new RewardController(data, app);

    RewardControllerTest() {
        data.onChange(() -> heard++);
    }

    static LocalDate d(int day) { return LocalDate.of(2026, 11, day); }

    @Test
    void E_ATT_1_attend() {
        reward.attend();
        assertEquals(20, data.getPoints());
        assertEquals(d(16), data.getAttendanceDay());
        assertEquals(Set.of(d(16)), data.getAttendDays());
        assertEquals("출석했어! +20P", app.getBubble().getLine());
        assertEquals(1, saves);
        assertEquals(1, heard);
    }
}
