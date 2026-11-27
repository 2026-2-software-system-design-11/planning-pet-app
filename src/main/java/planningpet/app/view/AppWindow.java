package planningpet.app.view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import planningpet.app.controller.AppController;
import planningpet.app.model.AppData;
import planningpet.common.view.Backdrop;
import planningpet.common.view.Card;
import planningpet.common.view.Icons;
import planningpet.common.view.RoundButtonUI;
import planningpet.common.view.StatCard;
import planningpet.common.view.Theme;
import planningpet.reward.controller.RewardController;

/**
 * 앱의 창(05 창 구조): 창 바탕 안에 왼쪽 사이드바, 오른쪽 본문 판(위 줄은 화면 머리와 오른쪽 카드, 가운데는 고른 화면).
 * 창 없이 만들 수 있고, 창은 frame이 만든다(docs/porting/frame.md).
 */
public final class AppWindow {
    /** 05 창 구조의 창 제목. */
    public static final String TITLE = "플래닝펫 (Planning Pet)";
    private static final String COMMON = "공통";
    private final AppData data;
    private final JPanel nav = new JPanel(new GridLayout(0, 1, 0, 6));
    private final Card navWrap = new Card(new BorderLayout(), 22).filled(Theme.SIDE);
    private final Backdrop root = new Backdrop(new BorderLayout(Theme.GAP, 0), navWrap);
    private final Card main = new Card(new BorderLayout(0, 10), 24).filled(Theme.PANEL);
    private final JPanel center = new JPanel(new CardLayout());
    /** 화면마다 다른 머리(아이콘, 제목, 부제). 화면과 함께 바뀐다. */
    private final JPanel heads = new JPanel(new CardLayout());
    /** 화면 머리 오른쪽 카드(D-47). 화면과 함께 바뀐다. */
    private final JPanel rights = new JPanel(new CardLayout());
    final StatCard.Value streak = new StatCard.Value();
    final StatCard.Value points = new StatCard.Value();
    final JButton attend = new JButton();
    private final List<Runnable> views = new ArrayList<>();

    public AppWindow(AppData data, AppController app) {
        this.data = data;
        RewardController reward = new RewardController(data, app);
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
        main.add(topRow(), BorderLayout.NORTH);
        main.add(center, BorderLayout.CENTER);
        root.add(main, BorderLayout.CENTER);
        root.setPreferredSize(new Dimension(1000, 680));
        attend.addActionListener(e -> reward.attend());
        watch(this::refresh);
    }

    /** 창 내용(창 바탕). */
    public JComponent root() {
        return root;
    }

    /** 데이터 변경 알림과 다시 그리기 신호를 함께 받는다. */
    public void watch(Runnable refresh) {
        data.onChange(refresh);
        views.add(refresh);
    }

    /** 화면 전환이나 주 옮기기처럼 model을 바꾸지 않을 때 화면을 다시 그린다(D-46). */
    public void redraw() {
        views.forEach(Runnable::run);
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

    /** 본문 위 줄: 왼쪽은 화면 머리, 오른쪽은 홈, 일정관리, 설정에 같은 연속 접속, 출석, 보유 포인트(05 창 구조). */
    private JPanel topRow() {
        JPanel row = new JPanel(new BorderLayout(Theme.GAP, 0));
        row.add(heads, BorderLayout.CENTER);
        attend.putClientProperty(RoundButtonUI.PRIMARY, Boolean.TRUE);
        attend.setFont(Theme.bold(13));
        attend.setBorder(BorderFactory.createEmptyBorder(7, 14, 9, 14));
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.add(StatCard.of(Icons.Kind.FLAME, streak, attend));
        right.add(StatCard.of(Icons.Kind.COIN, points, null));
        rights.add(right, COMMON);
        row.add(rights, BorderLayout.EAST);
        return row;
    }

    /** 카드 값과 출석 단추(06 11번: 오늘 출석했으면 "출석 완료"로 흐림). 앱 날짜가 바뀐 알림에도 이것이 단추를 되돌린다(23번). */
    private void refresh() {
        streak.set("연속 접속", data.getStreak() + "일");
        points.set("보유", String.format("%,dP", data.getPoints()));
        boolean attended = data.today().equals(data.getAttendanceDay());
        attend.setText(attended ? "출석 완료" : "출석 +20P");
        attend.setEnabled(!attended);
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
