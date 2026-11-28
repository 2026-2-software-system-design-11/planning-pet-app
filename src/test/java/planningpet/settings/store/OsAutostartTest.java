package planningpet.settings.store;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * macOS와 Windows 등록. 번호는 docs/porting/settings.md S10a 2절. 홈은 @TempDir이고 명령은 적기만 하는 대역이라 이 컴퓨터의
 * LaunchAgents와 레지스트리를 건드리지 않는다.
 */
class OsAutostartTest {
    private static final String APP = "/Applications/PlanningPet.app/Contents/MacOS/PlanningPet";
    private static final String EXE = "C:\\Program Files\\PlanningPet\\PlanningPet.exe";
    private static final String RUN = "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Run";
    @TempDir
    Path home;
    /** 대역이 받은 명령. 명령마다 한 줄이다. */
    private final List<String> ran = new ArrayList<>();
    /** reg query와 그 밖 명령의 종료 코드, 출력. */
    private int query = 1;
    private OsAutostart.Result other = new OsAutostart.Result(0, "");

    private OsAutostart mac(String app) {
        return new OsAutostart("Mac OS X", home, app, c -> {
            throw new AssertionError("macOS는 명령을 돌리지 않는다: " + c);
        });
    }

    private OsAutostart windows(String app) {
        return new OsAutostart("Windows 11", home, app, c -> {
            ran.add(String.join(" ", c));
            return c.get(1).equals("query") ? new OsAutostart.Result(query, "") : other;
        });
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
    void eAuto5EscapesPath() throws IOException {
        mac("/Applications/A & B <1>/PlanningPet").set(true);
        String text = Files.readString(mac(APP).plist());
        assertTrue(text.contains("<string>/Applications/A &amp; B &lt;1&gt;/PlanningPet</string>"), text);
    }

    @Test
    void eAuto6CannotWrite() throws IOException {
        Files.createDirectories(home.resolve("Library"));
        Files.writeString(home.resolve("Library/LaunchAgents"), "파일");
        assertThrows(IOException.class, () -> mac(APP).set(true));
        assertFalse(mac(APP).registered());
    }

    @Test
    void eAuto7SetWhenUnavailableChangesNothing() {
        assertThrows(IllegalStateException.class, () -> mac(null).set(true));
        assertFalse(Files.exists(home.resolve("Library")));
    }

    @Test
    void eAuto8LinuxIsUnavailable() throws IOException {
        mac(APP).set(true);
        OsAutostart linux = new OsAutostart("Linux", home, APP, c -> {
            throw new AssertionError("Linux는 명령을 돌리지 않는다: " + c);
        });
        assertFalse(linux.available());
        assertFalse(linux.registered());
    }

    @Test
    void eAuto9WindowsRegisters() throws IOException {
        assertFalse(windows(null).available());
        windows(EXE).set(true);
        assertEquals(List.of("reg add " + RUN + " /v PlanningPet /t REG_SZ /d \\\"" + EXE + "\\\" --autostart /f"), ran);
    }

    @Test
    void eAuto10WindowsReadsQueryCode() {
        query = 0;
        assertTrue(windows(EXE).registered());
        query = 1;
        assertFalse(windows(EXE).registered());
        assertEquals(List.of("reg query " + RUN + " /v PlanningPet", "reg query " + RUN + " /v PlanningPet"), ran);
        OsAutostart broken = new OsAutostart("Windows 11", home, EXE, c -> {
            throw new IOException("reg가 없다");
        });
        assertFalse(broken.registered());
    }

    @Test
    void eAuto11WindowsDeletesOnlyWhenRegistered() throws IOException {
        windows(EXE).set(false);
        query = 0;
        windows(EXE).set(false);
        String q = "reg query " + RUN + " /v PlanningPet";
        assertEquals(List.of(q, q, "reg delete " + RUN + " /v PlanningPet /f"), ran);
    }

    @Test
    void eAuto12WindowsRefusalIsReason() {
        other = new OsAutostart.Result(1, "액세스가 거부되었습니다.");
        IOException e = assertThrows(IOException.class, () -> windows(EXE).set(true));
        assertEquals("액세스가 거부되었습니다.", e.getMessage());
    }
}
