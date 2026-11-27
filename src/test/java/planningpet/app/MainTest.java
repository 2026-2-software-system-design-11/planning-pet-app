package planningpet.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** docs/porting/build.md 2절의 실행 인자 경계. */
class MainTest {
    private static final String[] NONE = {};

    private static Path linux(String... args) {
        return Main.dataFolder(args, "Linux", Map.of(), "/home/a");
    }

    private static void noDataDir(String... args) {
        var e = assertThrows(IllegalArgumentException.class, () -> linux(args));
        assertEquals("--data-dir 뒤에 폴더 경로를 넣어 주세요", e.getMessage());
    }

    @Test
    void E_MAIN_1_macOS() {
        assertEquals(Path.of("/Users/a/Library/Application Support/PlanningPet"),
                Main.dataFolder(NONE, "Mac OS X", Map.of(), "/Users/a"));
    }

    @Test
    void E_MAIN_2_windows() {
        String appData = "C:\\Users\\a\\AppData\\Roaming";
        assertEquals(Path.of(appData, "PlanningPet"),
                Main.dataFolder(NONE, "Windows 11", Map.of("APPDATA", appData), "C:\\Users\\a"));
    }

    @Test
    void E_MAIN_3_windowsWithoutAppData() {
        assertEquals(Path.of("C:\\Users\\a", ".planningpet"),
                Main.dataFolder(NONE, "Windows 11", Map.of(), "C:\\Users\\a"));
    }

    @Test
    void E_MAIN_4_otherOs() {
        assertEquals(Path.of("/home/a/.planningpet"), linux());
    }

    @Test
    void E_MAIN_5_dataDir() {
        assertEquals(Path.of("/tmp/demo"), linux("--data-dir", "/tmp/demo"));
    }

    @Test
    void E_MAIN_6_dataDirLast() {
        noDataDir("--data-dir");
    }

    @Test
    void E_MAIN_7_dataDirThenFlag() {
        noDataDir("--data-dir", "--autostart");
    }

    @Test
    void E_MAIN_8_dataDirBlank() {
        noDataDir("--data-dir", "");
    }

    @Test
    void E_MAIN_9_autostartOnly() {
        assertEquals(Path.of("/home/a/.planningpet"), linux("--autostart"));
    }

    @Test
    void E_MAIN_10_unknownIgnored() {
        assertEquals(Path.of("/home/a/.planningpet"), linux("--first-run"));
    }
}
