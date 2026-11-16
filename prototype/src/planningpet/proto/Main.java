package planningpet.proto;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/** 프로토타입 실행 진입점. 창을 닫으면 끝난다(트레이는 프로토타입에서 뺐다). */
public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            App app = create(MockData.sample());
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
        javax.swing.UIManager.put("swing.boldMetal", Boolean.FALSE);
        App app = new App(store);
        app.addScreen("홈", new MainScreen(app));
        app.addScreen("일정관리", soon());
        app.addScreen("pet", soon());
        app.addScreen("통계", null);
        app.addScreen("설정", soon());
        return app;
    }

    private static JLabel soon() {
        return new JLabel("다음 단계에서 붙여요", SwingConstants.CENTER);
    }
}
