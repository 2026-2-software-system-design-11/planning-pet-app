package planningpet.app.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;

/** docs/porting/store.md 2절의 E-APP 경계(사본, 되돌리기). 지금은 11-18(수) 10:00이다. */
class AppDataCopyTest {
    private static final ZoneId ZONE = ZoneId.systemDefault();
    private static final Clock CLOCK = Clock.fixed(LocalDateTime.parse("2026-11-18T10:00").atZone(ZONE).toInstant(), ZONE);
    private final AppData data = Filled.data(CLOCK);

    @Test
    void E_APP_16_copyHasSameValues() {
        assertEquals(Filled.values(data), Filled.values(data.copy()));
    }

    @Test
    void E_APP_21_listenersStay() {
        int[] heard = {0};
        data.onChange(() -> heard[0]++);
        AppData copy = data.copy();
        copy.changed();
        assertEquals(0, heard[0]);
        data.restore(copy);
        data.changed();
        assertEquals(1, heard[0]);
    }
}
