package planningpet.app;

import java.nio.file.Path;
import java.time.Clock;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import planningpet.app.controller.AppController;
import planningpet.app.controller.DayWatcher;
import planningpet.app.model.AppData;
import planningpet.app.model.DataStore;
import planningpet.app.model.LoadResult;
import planningpet.app.view.AppWindow;
import planningpet.app.view.Tray;

/** 진입점. 실행 인자로 데이터 폴더를 정하고 클래스를 만들어 잇는다(07 실행 모드). */
public final class Main {
    static final String NO_DATA_DIR = "--data-dir 뒤에 폴더 경로를 넣어 주세요";

    private Main() {
    }

    public static void main(String[] args) {
        try {
            dataFolder(args); // 저장(M8)과 하나만 실행(S11)이 이 폴더를 쓴다.
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            System.exit(2);
        }
        // --autostart는 S11에서 트레이와 함께 창을 띄우지 않게 한다. 지금은 창을 띄운다.
        // 창 내용(AppWindow)을 데이터와 잇는 일은 S2다. 그때까지는 빈 창이다(docs/porting/frame.md).
        SwingUtilities.invokeLater(() -> AppWindow.frame(new JPanel()).setVisible(true));
    }

    /** 창을 띄우고 앱을 끝내는 일. 검사가 창 없이 바꿔 끼운다(docs/porting/main.md). */
    interface Shell {
        /** 메인 창을 띄운다. */
        void main(JComponent root);

        /** 30초 타이머를 켠다. */
        void timer(Tray tray);

        /** 저장 실패 창을 띄운다(AppController의 errors). */
        void saveFailed(String text);
    }

    /** UC-01. 06 21번 load의 결과로 갈린다. 창은 shell이 띄운다. */
    static void start(DataStore store, Path folder, Clock clock, Shell shell) {
        LoadResult result = store.load();
        switch (result.status()) {
            case LOADED -> open(result.data(), new AppController(result.data(), store, shell::saveFailed), clock, shell);
        }
    }

    /** 메인 창과 30초 타이머. 차례는 프로토타입 create와 main과 같다. 화면(홈 같은 것)은 S3부터 더한다. */
    private static void open(AppData data, AppController app, Clock clock, Shell shell) {
        AppWindow window = new AppWindow(data, app);
        DayWatcher watcher = new DayWatcher(data, app);
        shell.main(window.root());
        app.onWindowShown();
        shell.timer(new Tray(watcher, clock));
    }

    /** 데이터 폴더. --data-dir 뒤에 경로가 없으면 IllegalArgumentException. 모르는 인자는 무시한다. */
    public static Path dataFolder(String[] args) {
        return dataFolder(args, System.getProperty("os.name"), System.getenv(), System.getProperty("user.home"));
    }

    /** 운영체제 값을 바꿔 끼우는 검사용 입구. */
    static Path dataFolder(String[] args, String os, Map<String, String> env, String home) {
        Path chosen = null;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--data-dir")) {
                if (i + 1 == args.length || args[i + 1].isBlank() || args[i + 1].startsWith("--")) {
                    throw new IllegalArgumentException(NO_DATA_DIR);
                }
                chosen = Path.of(args[++i]);
            }
        }
        return chosen != null ? chosen : osFolder(os, env, home);
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
