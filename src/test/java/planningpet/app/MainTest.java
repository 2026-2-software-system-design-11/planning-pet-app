package planningpet.app;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** docs/porting/build.md 2절의 실행 인자 경계. */
class MainTest {
    private static final String[] NONE = {};

    private static Path linux(String... args) {
        return Main.dataFolder(args, "Linux", Map.of(), "/home/a");
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
    void E_MAIN_9_autostartOnly() {
        assertEquals(Path.of("/home/a/.planningpet"), linux("--autostart"));
    }

    @Test
    void E_MAIN_10_unknownIgnored() {
        assertEquals(Path.of("/home/a/.planningpet"), linux("--first-run"));
    }
}
