package planningpet.reward.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

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

    @Test
    void E_ATT_2_sameDayAgainChangesNothing() {
        reward.attend();
        reward.attend();
        assertEquals(20, data.getPoints());
        assertEquals("출석했어! +20P", app.getBubble().getLine());
        assertEquals(2, heard);
    }

    @Test
    void E_ATT_3_appDayEnd() {
        data.setAttendanceDay(d(16));
        now = LocalDateTime.of(2026, 11, 17, 3, 59);
        reward.attend();
        assertEquals(0, data.getPoints());
        now = LocalDateTime.of(2026, 11, 17, 4, 0);
        reward.attend();
        assertEquals(20, data.getPoints());
        assertEquals(d(17), data.getAttendanceDay());
    }

    @Test
    void E_ATT_4_saveFailsRestores() {
        data.setPoints(30);
        failing = true;
        reward.attend();
        assertEquals(30, data.getPoints());
        assertNull(data.getAttendanceDay());
        assertEquals(Set.of(), data.getAttendDays());
        assertEquals("", app.getBubble().getLine());
        assertEquals(List.of("출석 저장에 실패해서 바꾸기 전으로 돌렸어요."), errors);
    }
}
