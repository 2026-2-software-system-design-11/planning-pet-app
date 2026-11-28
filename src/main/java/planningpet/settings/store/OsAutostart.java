package planningpet.settings.store;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import planningpet.settings.model.Autostart;

/** 06 25번의 운영체제 등록. macOS는 LaunchAgents의 plist다(docs/porting/settings.md S10a). */
public final class OsAutostart implements Autostart {
    static final String LABEL = "com.planningpet.app";
    private final String os;
    private final Path home;
    private final String app;
    private final Runner runner;

    /** 명령을 돌리는 일. 검사는 부른 명령을 적는 대역을 넣어 이 컴퓨터의 레지스트리를 건드리지 않는다. */
    interface Runner {
        /** 명령을 끝까지 돌린다. 돌리지 못하면 IOException이다. */
        Result run(List<String> command) throws IOException;
    }

    /** 종료 코드와 표준 출력, 오류 출력을 합친 글. */
    record Result(int code, String output) {
    }

    /**
     * 운영체제 이름(os.name), 홈 폴더, 설치된 앱 자리(jpackage.app-path, D-114, 실행 JAR면 null), 명령 실행. 검사는 @TempDir 홈과
     * 대역을 넣는다.
     */
    OsAutostart(String os, Path home, String app, Runner runner) {
        this.os = os;
        this.home = home;
        this.app = app;
        this.runner = runner;
    }

    /** 이 컴퓨터의 값과 실제 명령 실행. 검사하지 않는다(docs/porting/settings.md S10b 3절). */
    public static OsAutostart here() {
        return new OsAutostart(System.getProperty("os.name"), Path.of(System.getProperty("user.home")),
                System.getProperty("jpackage.app-path"), OsAutostart::exec);
    }

    @Override
    public boolean available() {
        return app != null && !app.isBlank() && mac();
    }

    @Override
    public boolean registered() {
        return mac() && Files.exists(plist());
    }

    @Override
    public void set(boolean on) throws IOException {
        if (!available()) {
            throw new IllegalStateException("설치 파일로 설치했을 때만 쓸 수 있어요");
        }
        if (on) {
            Files.createDirectories(plist().getParent());
            Files.writeString(plist(), plistText());
        } else {
            Files.deleteIfExists(plist());
        }
    }

    private boolean mac() {
        return os.startsWith("Mac");
    }

    /** 실제 프로세스. 출력은 이 컴퓨터의 글자 인코딩(native.encoding)으로 읽는다. */
    private static Result exec(List<String> command) throws IOException {
        Process p = new ProcessBuilder(command).redirectErrorStream(true).start();
        try (var out = p.inputReader()) {
            String text = String.join("\n", out.lines().toList()).strip();
            return new Result(p.waitFor(), text);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException(e);
        }
    }

    Path plist() {
        return home.resolve("Library").resolve("LaunchAgents").resolve(LABEL + ".plist");
    }

    /** RunAtLoad인 LaunchAgent. 앱 자리를 XML 글자로 바꿔 넣는다. */
    private String plistText() {
        String path = app.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
                <plist version="1.0">
                <dict>
                  <key>Label</key>
                  <string>%s</string>
                  <key>ProgramArguments</key>
                  <array>
                    <string>%s</string>
                    <string>--autostart</string>
                  </array>
                  <key>RunAtLoad</key>
                  <true/>
                </dict>
                </plist>
                """.formatted(LABEL, path);
    }
}
