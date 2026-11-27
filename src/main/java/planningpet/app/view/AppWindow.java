package planningpet.app.view;

import java.awt.Dimension;
import javax.swing.JFrame;

/** 앱의 창 하나(05 1절). 화면 다섯 개는 뒤 단위에서 채운다. */
public final class AppWindow extends JFrame {
    /** 05 1절의 창 제목. */
    public static final String TITLE = "플래닝펫 (Planning Pet)";

    public AppWindow() {
        super(TITLE);
        // 25번(트레이에 남기)은 S11 단위다. 그때까지는 닫으면 끝난다.
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1000, 680));
        pack();
        setLocationRelativeTo(null);
    }
}
