package planningpet.settings.store;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import planningpet.settings.model.Autostart;

/** 06 25번의 운영체제 등록. macOS는 LaunchAgents의 plist다(docs/porting/settings.md S10a). */
public final class OsAutostart implements Autostart {
    static final String LABEL = "com.planningpet.app";
    private final String os;
    private final Path home;
    private final String app;

    /** 운영체제 이름(os.name), 홈 폴더, 설치된 앱 자리(jpackage.app-path, D-114, 실행 JAR면 null). 검사는 @TempDir 홈을 넣는다. */
    OsAutostart(String os, Path home, String app) {
        this.os = os;
        this.home = home;
        this.app = app;
    }

    /** 이 컴퓨터의 값. 검사하지 않는다(docs/porting/settings.md S10b 3절). */
    public static OsAutostart here() {
        return new OsAutostart(System.getProperty("os.name"), Path.of(System.getProperty("user.home")),
                System.getProperty("jpackage.app-path"));
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
