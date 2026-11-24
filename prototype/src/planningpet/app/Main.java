package planningpet.app;

import planningpet.app.controller.DayWatcher;
import planningpet.app.model.*;
import planningpet.app.view.App;
import planningpet.common.view.Theme;
import planningpet.home.view.MainScreen;
import planningpet.pet.view.*;
import planningpet.schedule.view.ScheduleScreen;
import planningpet.settings.view.SettingsScreen;
import planningpet.stats.view.StatsScreen;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * 프로토타입 실행 진입점. --first-run이면 반려동물 고르기부터 한다. 창을 닫으면 끝난다(트레이는 뺐다).
 * 앱 날짜는 컴퓨터 시계다(06의 1번). --sample-clock이면 샘플 시계(2026-11-14 15:23)에 멈춘다. QA와 화면 그림은 늘 샘플 시계다.
 */
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
            boolean realClock = !java.util.List.of(args).contains("--sample-clock");
            if (realClock) {
                store.now = java.time.LocalDateTime.now().withNano(0);
            }
            if (firstRun && !new PickDialog().ask(store)) {
                System.exit(0);
            }
            App app = create(store);
            JFrame frame = new JFrame("플래닝펫 (Planning Pet)");
            frame.setIconImage(appIcon());
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(app.root);
            frame.setMinimumSize(new java.awt.Dimension(1000, 680));
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            app.control.windowShown();
            new javax.swing.Timer(30_000, e -> {
                if (realClock) {
                    store.now = java.time.LocalDateTime.now().withNano(0);
                }
                app.watcher.tick();
            }).start();
        });
    }

    /** 창 제목 줄의 발자국 아이콘(예시 2026-11-23의 제목 줄). */
    private static java.awt.Image appIcon() {
        java.awt.image.BufferedImage im = new java.awt.image.BufferedImage(64, 64, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g = im.createGraphics();
        planningpet.common.view.Icons.draw(g, planningpet.common.view.Icons.Kind.PAW, 0, 0, 64, Theme.ICON);
        g.dispose();
        return im;
    }

    /** 화면을 모두 붙인 앱. 창 없이 그릴 때도 이것을 쓴다. */
    public static App create(Store store) {
        App app = new App(store);
        MainScreen main = new MainScreen(app);
        app.addScreen("홈", "오늘도 계획을 세우고, 조금씩 성장해요!", main);
        app.watcher = new DayWatcher(app, main);
        app.addScreen("일정관리", "내 일정을 한눈에 확인하고, 체계적으로 관리해요!", new ScheduleScreen(app));
        PetScreen room = new PetScreen(app);
        // D-59, D-80: 홈 펫 카드를 누르면 캐릭터 창을 상태 탭으로 연다. 나의 방의 펫 칩은 펫을 고르기만 한다.
        main.openCharacter = () -> room.characterOpener.accept(false);
        app.addScreen("나의 방", "나만의 공간을 꾸미고, 반려동물과 함께 시간을 보내요!", room, room.headRight());
        StatsScreen stats = new StatsScreen(app);
        app.addScreen("통계", "나의 계획 실천과 일정 완료 추이를 한눈에 확인해요!", stats, stats.headControls());
        app.addScreen("설정", "플래닝펫을 내게 맞게 바꿔요.", new SettingsScreen(app));
        app.watch(() -> Theme.clear(app.root));
        Theme.clear(app.root);
        return app;
    }
}
