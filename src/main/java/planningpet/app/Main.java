package planningpet.app;

import javax.swing.SwingUtilities;
import planningpet.app.view.AppWindow;

/** 진입점. 클래스를 만들어 잇고 창을 띄운다(07 실행 모드). */
public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AppWindow().setVisible(true));
    }
}
