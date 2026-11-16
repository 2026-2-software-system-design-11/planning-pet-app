package planningpet.proto;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/** 프로토타입 실행 진입점. --first-run이면 반려동물 고르기부터 한다. 창을 닫으면 끝난다(트레이는 뺐다). */
public final class Main {
    static {
        javax.swing.UIManager.put("swing.boldMetal", Boolean.FALSE);
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
            app.store.talk = app.talk.onWindowShown(app.game);
            app.store.changed();
        });
    }

    /** 화면을 모두 붙인 앱. 창 없이 그릴 때도 이것을 쓴다. */
    public static App create(Store store) {
        App app = new App(store);
        app.addScreen("홈", new MainScreen(app));
        app.addScreen("일정관리", new ScheduleScreen(app));
        app.addScreen("pet", new PetScreen(app));
        app.addScreen("통계", null);
        app.addScreen("설정", new SettingsScreen());
        return app;
    }
}
