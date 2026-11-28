package planningpet.app;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** 06 24번. 번호는 docs/porting/tray.md 2절. 잠금 파일은 @TempDir 폴더에 두고 검사마다 잡은 것을 놓는다. */
class SingleInstanceTest {
    @TempDir
    Path folder;
    private final List<SingleInstance> held = new ArrayList<>();
    /** 받은 신호 수. */
    private final Semaphore signals = new Semaphore(0);

    @AfterEach
    void releaseAll() throws IOException {
        for (SingleInstance one : held) {
            one.release();
        }
    }

    private SingleInstance one(Path dir) {
        SingleInstance one = new SingleInstance(dir, signals::release);
        held.add(one);
        return one;
    }

    @Test
    void T_ONE_1_firstRunAcquires() throws IOException {
        assertTrue(one(folder).acquire());
        assertTrue(Integer.parseInt(Files.readString(folder.resolve(SingleInstance.FILE)).trim()) > 0);
    }

    @Test
    void T_ONE_2_secondRunSignalsFirst() throws InterruptedException {
        assertTrue(one(folder).acquire());
        assertFalse(one(folder).acquire());
        assertTrue(signals.tryAcquire(5, TimeUnit.SECONDS));
    }

    @Test
    void E_ONE_1_missingFolderIsMade() {
        Path dir = folder.resolve("a").resolve("b");
        assertTrue(one(dir).acquire());
        assertTrue(Files.exists(dir.resolve(SingleInstance.FILE)));
    }

    @Test
    void E_ONE_2_everyRunSignals() throws InterruptedException {
        assertTrue(one(folder).acquire());
        assertFalse(one(folder).acquire());
        assertFalse(one(folder).acquire());
        assertTrue(signals.tryAcquire(2, 5, TimeUnit.SECONDS));
    }

    @Test
    void E_ONE_3_unreadablePortSendsNothing() throws IOException, InterruptedException {
        assertTrue(one(folder).acquire());
        Files.writeString(folder.resolve(SingleInstance.FILE), "abc");
        assertFalse(one(folder).acquire());
        assertFalse(signals.tryAcquire(1, TimeUnit.SECONDS));
    }

    @Test
    void E_ONE_4_noLockFileStillRuns() throws IOException {
        Path file = Files.writeString(folder.resolve("data"), "");
        assertTrue(one(file).acquire());
    }
}
