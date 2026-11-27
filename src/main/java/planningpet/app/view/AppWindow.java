package planningpet.app.view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import planningpet.common.view.Backdrop;
import planningpet.common.view.Card;
import planningpet.common.view.Icons;
import planningpet.common.view.Theme;

/**
 * 앱의 창(05 창 구조): 창 바탕 안에 왼쪽 사이드바, 오른쪽 본문 판(위 줄은 화면 머리와 오른쪽 카드, 가운데는 고른 화면).
 * 창 없이 만들 수 있고, 창은 frame이 만든다(docs/porting/frame.md).
 */
public final class AppWindow {
    /** 05 창 구조의 창 제목. */
    public static final String TITLE = "플래닝펫 (Planning Pet)";
    private final JPanel nav = new JPanel(new GridLayout(0, 1, 0, 6));
    private final Card navWrap = new Card(new BorderLayout(), 22).filled(Theme.SIDE);
    private final Backdrop root = new Backdrop(new BorderLayout(Theme.GAP, 0), navWrap);
    private final Card main = new Card(new BorderLayout(0, 10), 24).filled(Theme.PANEL);
    private final JPanel center = new JPanel(new CardLayout());

    public AppWindow() {
        navWrap.setBorder(BorderFactory.createEmptyBorder(18, 8, 12, 8));
        navWrap.add(logo(), BorderLayout.NORTH);
        JPanel navTop = new JPanel(new BorderLayout());
        navTop.setBorder(BorderFactory.createEmptyBorder(18, 0, 0, 0));
        navTop.add(nav, BorderLayout.NORTH);
        navWrap.add(navTop, BorderLayout.CENTER);
        navWrap.setPreferredSize(new Dimension(140, 0));
        root.add(navWrap, BorderLayout.WEST);
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        main.setBorder(BorderFactory.createEmptyBorder(10, 12, 14, 12));
        main.add(center, BorderLayout.CENTER);
        root.add(main, BorderLayout.CENTER);
        root.setPreferredSize(new Dimension(1000, 680));
    }

    /** 창 내용(창 바탕). */
    public JComponent root() {
        return root;
    }

    /** 사이드바 위의 로고(05 창 구조의 앱 이름). 얼굴 자리는 S2가 고양이 얼굴(PetArt)을 그린다. */
    private JPanel logo() {
        JPanel p = new JPanel(new BorderLayout(0, 2));
        JComponent face = new JComponent() {
        };
        face.setPreferredSize(new Dimension(0, 64));
        JLabel name = new JLabel("플래닝펫", SwingConstants.CENTER);
        name.setFont(Theme.heavy(22));
        JLabel en = new JLabel("Planning Pet", SwingConstants.CENTER);
        en.setFont(Theme.font(11));
        en.setForeground(Theme.INK_SUB);
        JPanel words = new JPanel(new BorderLayout());
        words.add(name, BorderLayout.NORTH);
        words.add(en, BorderLayout.SOUTH);
        p.add(face, BorderLayout.NORTH);
        p.add(words, BorderLayout.CENTER);
        return p;
    }

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
