package planningpet.app;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** 06 24번. 번호는 docs/porting/tray.md 2절. 잠금 파일은 @TempDir 폴더에 둔다. */
class SingleInstanceTest {
    @TempDir
    Path folder;

    private SingleInstance one(Path dir) {
        return new SingleInstance(dir, () -> { });
    }

    @Test
    void T_ONE_1_firstRunAcquires() {
        assertTrue(one(folder).acquire());
        assertTrue(Files.exists(folder.resolve(SingleInstance.FILE)));
    }

    @Test
    void E_ONE_1_missingFolderIsMade() {
        Path dir = folder.resolve("a").resolve("b");
        assertTrue(one(dir).acquire());
        assertTrue(Files.exists(dir.resolve(SingleInstance.FILE)));
    }

    @Test
    void E_ONE_4_noLockFileStillRuns() throws IOException {
        Path file = Files.writeString(folder.resolve("data"), "");
        assertTrue(one(file).acquire());
    }
}
