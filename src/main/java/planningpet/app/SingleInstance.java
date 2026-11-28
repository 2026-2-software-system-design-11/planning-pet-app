package planningpet.app;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/** 06 24번. 데이터 폴더의 잠금 파일로 앱이 하나만 실행되게 한다(07 실행 모드, docs/porting/tray.md). */
public final class SingleInstance {
    static final String FILE = "planningpet.lock";
    /** 잠그는 자리. Windows는 잠근 영역을 다른 프로세스가 읽지 못해 포트를 적는 파일 앞쪽을 피한다. */
    private static final long LOCK_AT = Long.MAX_VALUE - 1;
    private final Path folder;
    private final Runnable onSignal;
    private FileChannel channel;

    /** onSignal은 두 번째 앱이 신호를 보낼 때마다 신호를 받는 스레드에서 불린다. */
    public SingleInstance(Path folder, Runnable onSignal) {
        this.folder = folder;
        this.onSignal = onSignal;
    }

    /**
     * 잠금 파일을 잡으면 true, 못 잡으면 false다. 잠금 파일을 만들 수 없으면 true로 실행을 잇는다(E-ONE-4, 06 24번이 정하지
     * 않음, 페르소나 투표 전 잠정).
     */
    public boolean acquire() {
        Path file = folder.resolve(FILE);
        try {
            Files.createDirectories(folder);
            FileChannel ch = FileChannel.open(file, StandardOpenOption.CREATE, StandardOpenOption.READ,
                    StandardOpenOption.WRITE);
            if (lock(ch) == null) {
                ch.close();
                return false;
            }
            channel = ch;
        } catch (IOException e) {
            return true;
        }
        return true;
    }

    /** 잡지 못하면 null. 같은 JVM이 이미 잡았으면 OverlappingFileLockException이라 이것도 못 잡은 것이다. */
    private static FileLock lock(FileChannel ch) throws IOException {
        try {
            return ch.tryLock(LOCK_AT, 1, false);
        } catch (OverlappingFileLockException e) {
            return null;
        }
    }

}
