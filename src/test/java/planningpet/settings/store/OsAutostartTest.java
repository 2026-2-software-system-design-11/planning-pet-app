package planningpet.settings.store;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    @Test
    void tWin5RemovedByOsShowsUnregistered() throws IOException {
        OsAutostart a = mac(APP);
        a.set(true);
        Files.delete(a.plist());
        assertFalse(a.registered());
    }

    @Test
    void tWin6JarIsUnavailable() {
        assertFalse(mac(null).available());
        assertFalse(mac("").available());
        assertTrue(mac(APP).available());
    }

    @Test
    void eAuto1PlistRunsAppWithAutostartAtLoad() throws IOException {
        mac(APP).set(true);
        String text = Files.readString(mac(APP).plist());
        assertTrue(text.contains("<key>Label</key>\n  <string>com.planningpet.app</string>"), text);
        assertTrue(text.contains("<array>\n    <string>" + APP + "</string>\n    <string>--autostart</string>\n  </array>"), text);
        assertTrue(text.contains("<key>RunAtLoad</key>\n  <true/>"), text);
    }

    @Test
    void eAuto2MakesMissingFolder() throws IOException {
        assertFalse(Files.exists(home.resolve("Library")));
        mac(APP).set(true);
        assertTrue(mac(APP).registered());
    }

    @Test
    void eAuto3RegisterAgainOverwrites() throws IOException {
        mac(APP).set(true);
        mac("/Applications/New.app/Contents/MacOS/PlanningPet").set(true);
        try (var files = Files.list(home.resolve("Library/LaunchAgents"))) {
            assertEquals(1, files.count());
        }
        String text = Files.readString(mac(APP).plist());
        assertTrue(text.contains("/Applications/New.app/") && !text.contains("PlanningPet.app"), text);
    }

    @Test
    void eAuto4UnregisterWithoutRegistration() throws IOException {
        mac(APP).set(false);
        assertFalse(mac(APP).registered());
        assertFalse(Files.exists(home.resolve("Library/LaunchAgents")));
    }

    @Test
    void eAuto6CannotWrite() throws IOException {
        Files.createDirectories(home.resolve("Library"));
        Files.writeString(home.resolve("Library/LaunchAgents"), "파일");
        assertThrows(IOException.class, () -> mac(APP).set(true));
        assertFalse(mac(APP).registered());
    }

    @Test
    void eAuto8LinuxIsUnavailable() throws IOException {
        mac(APP).set(true);
        OsAutostart linux = new OsAutostart("Linux", home, APP);
        assertFalse(linux.available());
        assertFalse(linux.registered());
    }
}
