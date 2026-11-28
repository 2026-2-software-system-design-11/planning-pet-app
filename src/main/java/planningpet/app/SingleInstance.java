package planningpet.app;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.charset.StandardCharsets;
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
    private ServerSocket server;

    /** onSignal은 두 번째 앱이 신호를 보낼 때마다 신호를 받는 스레드에서 불린다. */
    public SingleInstance(Path folder, Runnable onSignal) {
        this.folder = folder;
        this.onSignal = onSignal;
    }

    /**
     * 잠금 파일을 잡으면 127.0.0.1의 빈 포트에서 신호를 기다리고 그 포트를 파일에 적고 true다. 못 잡으면 파일의 포트로 신호를 보내고
     * false다. 잠금 파일을 만들 수 없으면 true로 실행을 잇는다(E-ONE-4, 06 24번이 정하지 않음, 페르소나 투표 전 잠정).
     */
    public boolean acquire() {
        Path file = folder.resolve(FILE);
        try {
            Files.createDirectories(folder);
            FileChannel ch = FileChannel.open(file, StandardOpenOption.CREATE, StandardOpenOption.READ,
                    StandardOpenOption.WRITE);
            if (lock(ch) == null) {
                ch.close();
                signal(file);
                return false;
            }
            channel = ch;
            server = new ServerSocket(0, 50, InetAddress.getLoopbackAddress());
            ch.truncate(0);
            ch.write(ByteBuffer.wrap(String.valueOf(server.getLocalPort()).getBytes(StandardCharsets.US_ASCII)), 0);
            ch.force(false);
            Thread listener = new Thread(this::listen, "하나만 실행");
            listener.setDaemon(true);
            listener.start();
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

    /** 연결 하나가 신호다. 포트를 읽지 못하거나 연결할 수 없으면 보내지 않는다(E-ONE-3). */
    private static void signal(Path file) {
        try (Socket s = new Socket(InetAddress.getLoopbackAddress(),
                Integer.parseInt(Files.readString(file, StandardCharsets.US_ASCII).trim()))) {
            s.getOutputStream().flush();
        } catch (IOException | IllegalArgumentException e) {
            // 실행 중인 앱이 아직 포트를 적지 않았거나 끝나는 중이다. 이 앱은 그대로 끝난다.
        }
    }

    /** 연결이 올 때마다 onSignal을 부른다. 받는 자리가 닫히면 멈춘다. */
    private void listen() {
        while (true) {
            try (Socket s = server.accept()) {
                onSignal.run();
            } catch (IOException e) {
                return;
            }
        }
    }

    /** 받는 자리와 잠금을 놓는다. 앱은 끝날 때 운영체제가 놓으므로 검사만 부른다. */
    void release() throws IOException {
        if (server != null) {
            server.close();
        }
        if (channel != null) {
            channel.close();
        }
    }
}
