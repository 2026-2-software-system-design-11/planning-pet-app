package planningpet.app;

import java.nio.file.Path;
import java.util.Map;
import javax.swing.SwingUtilities;
import planningpet.app.view.AppWindow;

/** 진입점. 실행 인자로 데이터 폴더를 정하고 클래스를 만들어 잇는다(07 실행 모드). */
public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        dataFolder(args); // 저장(M8)과 하나만 실행(S11)이 이 폴더를 쓴다.
        // --autostart는 S11에서 트레이와 함께 창을 띄우지 않게 한다. 지금은 창을 띄운다.
        SwingUtilities.invokeLater(() -> new AppWindow().setVisible(true));
    }

    /** 데이터 폴더. 모르는 인자는 무시한다. */
    public static Path dataFolder(String[] args) {
        return dataFolder(args, System.getProperty("os.name"), System.getenv(), System.getProperty("user.home"));
    }

    /** 운영체제 값을 바꿔 끼우는 검사용 입구. */
    static Path dataFolder(String[] args, String os, Map<String, String> env, String home) {
        return osFolder(os, env, home);
    }

    /** 06 21번의 운영체제 데이터 폴더. */
    private static Path osFolder(String os, Map<String, String> env, String home) {
        String appData = env.get("APPDATA");
        if (os.startsWith("Windows") && appData != null && !appData.isBlank()) {
            return Path.of(appData, "PlanningPet");
        }
        if (os.startsWith("Mac")) {
            return Path.of(home, "Library", "Application Support", "PlanningPet");
        }
        return Path.of(home, ".planningpet");
    }
}
