package planningpet.app;

import planningpet.app.controller.DayWatcher;
import planningpet.app.model.Store;
import planningpet.app.view.App;
import planningpet.common.view.Theme;
import planningpet.home.view.MainScreen;
import planningpet.pet.view.*;
import planningpet.schedule.view.ScheduleScreen;
import planningpet.settings.view.SettingsScreen;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/** 프로토타입 실행 진입점. --first-run이면 반려동물 고르기부터 한다. 창을 닫으면 끝난다(트레이는 뺐다). */
public final class Main {
    static {
        Theme.install();
    }

    private Main() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            boolean firstRun = java.util.List.of(args).contains("--first-run");
            Store store = firstRun ? MockData.empty() : MockData.sample();
            if (firstRun && !new PickDialog().ask(store)) {
                System.exit(0);
            }
            App app = create(store);
            JFrame frame = new JFrame("플래닝펫 프로토타입");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(app.root);
            frame.setMinimumSize(new java.awt.Dimension(1000, 680));
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            app.control.windowShown();
            new javax.swing.Timer(30_000, e -> app.watcher.tick()).start();
        });
    }

    /** 화면을 모두 붙인 앱. 창 없이 그릴 때도 이것을 쓴다. */
    public static App create(Store store) {
        App app = new App(store);
        MainScreen main = new MainScreen(app);
        app.addScreen("홈", "오늘도 계획을 세우고, 조금씩 성장해요!", main);
        app.watcher = new DayWatcher(app, main);
        app.addScreen("일정관리", "내 일정을 한눈에 확인하고, 체계적으로 관리해요!", new ScheduleScreen(app));
        app.addScreen("pet", ("DOG".equals(store.petKind) ? "강아지" : "고양이") + "와 함께 성장하고, 작은 성취를 모아 보세요!",
                new PetScreen(app));
        app.addScreen("통계", "", null);
        app.addScreen("설정", "플래닝펫을 내게 맞게 바꿔요.", new SettingsScreen());
        app.watch(() -> Theme.clear(app.root));
        Theme.clear(app.root);
        return app;
    }
}
