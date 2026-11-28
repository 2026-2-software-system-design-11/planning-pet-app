package planningpet.settings.store;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** macOS 등록. 번호는 docs/porting/settings.md S10a 2절. 홈은 @TempDir이라 이 컴퓨터의 LaunchAgents를 건드리지 않는다. */
class OsAutostartTest {
    private static final String APP = "/Applications/PlanningPet.app/Contents/MacOS/PlanningPet";
    @TempDir
    Path home;

    private OsAutostart mac(String app) {
        return new OsAutostart("Mac OS X", home, app);
    }

    @Test
    void tWin4RegisterShowsRegistered() throws IOException {
        OsAutostart a = mac(APP);
        assertFalse(a.registered());
        a.set(true);
        assertTrue(Files.exists(home.resolve("Library/LaunchAgents/com.planningpet.app.plist")));
        assertTrue(a.registered());
    }
}
