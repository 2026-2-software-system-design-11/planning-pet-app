package planningpet.app;

import java.nio.file.Path;
import java.time.Clock;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import planningpet.app.controller.AppController;
import planningpet.app.controller.DayWatcher;
import planningpet.app.model.AppData;
import planningpet.app.model.DataStore;
import planningpet.app.model.LoadResult;
import planningpet.app.model.LoadResult.Reason;
import planningpet.app.store.SqliteDataStore;
import planningpet.app.view.AppWindow;
import planningpet.app.view.Tray;
import planningpet.app.view.UnreadableDialog;
import planningpet.common.view.Theme;
import planningpet.pet.controller.PetController;
import planningpet.pet.view.PickDialog;

/** 진입점. 실행 인자로 데이터 폴더를 정하고 클래스를 만들어 잇는다(07 실행 모드). */
public final class Main {
    static final String NO_DATA_DIR = "--data-dir 뒤에 폴더 경로를 넣어 주세요";

    private Main() {
    }

    public static void main(String[] args) {
        Path folder;
        try {
            folder = dataFolder(args); // 저장(M8)과 하나만 실행(S11)이 이 폴더를 쓴다.
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            System.exit(2);
            return;
        }
        // --autostart는 S11에서 트레이와 함께 창을 띄우지 않게 한다. 지금은 창을 띄운다. 하나만 실행(24번)도 S11이다.
        Theme.install(); // 부품을 만들기 전에 건다(프로토타입 Main).
        Clock clock = Clock.systemDefaultZone();
        SwingUtilities.invokeLater(() -> start(new SqliteDataStore(folder, clock), folder, clock, new Swing()));
    }

    /** 창을 띄우고 앱을 끝내는 일. 검사가 창 없이 바꿔 끼운다(docs/porting/main.md). */
    interface Shell {
        /** 못 읽음 창을 띄우고 확인이나 닫기를 누를 때까지 기다린다. */
        void unreadable(Reason reason, Path file);

        /** 고르기 창을 띄운다. 닫기 버튼을 누르면 closed를 부른다. */
        void pick(PickDialog pick, Runnable closed);

        /** 메인 창을 띄운다. */
        void main(JComponent root);

        /** 30초 타이머를 켠다. */
        void timer(Tray tray);

        /** 저장 실패 창을 띄운다(AppController의 errors). */
        void saveFailed(String text);

        /** 앱을 끝낸다. */
        void exit(int code);
    }

    /** 실제 창을 띄운다. 창이 떠야 해서 검사하지 않는다. */
    private static final class Swing implements Shell {
        public void unreadable(Reason reason, Path file) { UnreadableDialog.show(null, reason, file); }
        public void pick(PickDialog pick, Runnable closed) { pick.dialog(closed).setVisible(true); }
        public void main(JComponent root) { AppWindow.frame(root).setVisible(true); }
        public void timer(Tray tray) { tray.start(); }
        public void saveFailed(String text) { AppWindow.saveFailed(text); }
        public void exit(int code) { System.exit(code); }
    }

    /** UC-01. 06 21번 load의 결과로 갈린다. 창은 shell이 띄운다. */
    static void start(DataStore store, Path folder, Clock clock, Shell shell) {
        LoadResult result = store.load();
        switch (result.status()) {
            case UNREADABLE -> {
                // UC-01 2b: 파일을 건드리지 않고 끝낸다. 종료 코드 1은 오류로 끝났다는 뜻이다(docs/porting/main.md).
                shell.unreadable(result.reason(), folder.resolve(SqliteDataStore.FILE));
                shell.exit(1);
            }
            case FIRST_RUN -> {
                // 07 seq-uc01: 첫 실행은 빈 AppData이고 고르기 창과 메인 창이 controller 하나를 같이 쓴다(docs/porting/main.md).
                AppData data = new AppData(clock);
                AppController app = new AppController(data, store, shell::saveFailed);
                PickDialog pick = new PickDialog();
                pick.onStart(new PetController(data, app), data, () -> open(data, app, clock, shell));
                shell.pick(pick, () -> shell.exit(0)); // 프로토타입 Main처럼 0으로 끝낸다. 저장한 것이 없다.
            }
            case LOADED -> open(result.data(), new AppController(result.data(), store, shell::saveFailed), clock, shell);
        }
    }

    /** 메인 창과 30초 타이머. 차례는 프로토타입 create와 main과 같다. 화면(홈 같은 것)은 S3부터 더한다. */
    private static void open(AppData data, AppController app, Clock clock, Shell shell) {
        AppWindow window = new AppWindow(data, app);
        DayWatcher watcher = new DayWatcher(data, app);
        window.watch(() -> Theme.clear(window.root()));
        Theme.clear(window.root());
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
