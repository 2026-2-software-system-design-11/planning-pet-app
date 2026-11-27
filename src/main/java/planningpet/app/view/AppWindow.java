package planningpet.app.view;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;
import javax.swing.JFrame;
import planningpet.common.view.Icons;
import planningpet.common.view.Theme;

/**
 * 앱의 창(05 창 구조). 창 없이 만들 수 있고, 창은 frame이 만든다(docs/porting/frame.md).
 */
public final class AppWindow {
    /** 05 창 구조의 창 제목. */
    public static final String TITLE = "플래닝펫 (Planning Pet)";

    /** 창(05 창 구조, D-47). 창을 띄우는 일은 Main이 한다. 25번(트레이에 남기)은 S11이라 그때까지는 닫으면 끝난다. */
    public static JFrame frame(JComponent content) {
        JFrame f = new JFrame(TITLE);
        f.setIconImage(icon());
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setContentPane(content);
        f.setMinimumSize(new Dimension(1000, 680));
        f.pack();
        f.setLocationRelativeTo(null);
        return f;
    }

    /** 창 제목 줄의 발자국 아이콘(D-47). */
    static BufferedImage icon() {
        BufferedImage im = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = im.createGraphics();
        Icons.draw(g, Icons.Kind.PAW, 0, 0, 64, Theme.ICON);
        g.dispose();
        return im;
    }
}
