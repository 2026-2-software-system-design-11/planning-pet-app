package planningpet.app.view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import planningpet.app.controller.AppController;
import planningpet.app.controller.CloseAction;
import planningpet.app.model.AppData;
import planningpet.common.view.Backdrop;
import planningpet.common.view.Card;
import planningpet.common.view.Icons;
import planningpet.common.view.RoundButtonUI;
import planningpet.common.view.StatCard;
import planningpet.common.view.Theme;
import planningpet.pet.view.PetArt;
import planningpet.reward.controller.RewardController;

/**
 * 앱의 창(05 창 구조): 창 바탕 안에 왼쪽 사이드바, 오른쪽 본문 판(위 줄은 화면 머리와 오른쪽 카드, 가운데는 고른 화면).
 * 창 없이 만들 수 있고, 창은 frame이 만든다(docs/porting/frame.md).
 */
public final class AppWindow {
    /** 05 창 구조의 창 제목. */
    public static final String TITLE = "플래닝펫 (Planning Pet)";
    /** 05 그 밖의 창과 알림의 저장 실패 창 제목. */
    static final String SAVE_FAILED = "저장하지 못했어요";
    private static final String COMMON = "공통";
    /** 메뉴와 화면 머리의 아이콘(디자인 시스템 9절). 표에 없는 이름은 발자국이다. */
    private static final Map<String, Icons.Kind> ICONS = Map.of("홈", Icons.Kind.HOME, "일정관리", Icons.Kind.CALENDAR,
            "나의 방", Icons.Kind.ROOM, "통계", Icons.Kind.CHART, "설정", Icons.Kind.GEAR);
    private final AppData data;
    private final AppController app;
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
    private final ButtonGroup navGroup = new ButtonGroup();
    /** 화면 머리 오른쪽에 자기 카드를 둔 화면의 이름(D-47). */
    private final Set<String> ownRight = new HashSet<>();
    private String current = "";

    public AppWindow(AppData data, AppController app) {
        this.data = data;
        this.app = app;
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

    /** 지금 보이는 화면의 이름. */
    public String current() {
        return current;
    }

    /** 메뉴 단추와 화면을 함께 넣는다. 화면 위에 아이콘, 제목, 부제를 둔다(D-33). 처음 넣은 화면을 고른다. */
    public void addScreen(String name, String subtitle, JComponent screen) {
        addScreen(name, subtitle, screen, null);
    }

    /** right가 있으면 화면 머리 오른쪽에 공통 카드(연속 접속, 출석, 보유 포인트) 대신 그것을 둔다(D-47). */
    public void addScreen(String name, String subtitle, JComponent screen, JComponent right) {
        Icons.Kind kind = ICONS.getOrDefault(name, Icons.Kind.PAW);
        JToggleButton b = new JToggleButton(name);
        b.putClientProperty(RoundButtonUI.NAV, Boolean.TRUE);
        b.setFont(Theme.bold(17));
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setIcon(Icons.icon(kind, 22, Theme.ICON));
        b.setDisabledIcon(Icons.icon(kind, 22, Theme.INK_DIM));
        b.setIconTextGap(10);
        b.setBorder(BorderFactory.createEmptyBorder(11, 12, 13, 8));
        navGroup.add(b);
        nav.add(b);
        JLabel icon = new JLabel(Icons.icon(kind, 40, Theme.ICON));
        JLabel title = new JLabel(name);
        title.setFont(Theme.heavy(26));
        // 기본 창(1000x680)에서 오른쪽 카드가 넓은 화면은 부제가 잘렸다. 자리가 모자라면 발자국을 빼고 12로 줄이고,
        // 그래도 모자라면 가운데에 가까운 띄어쓰기에서 두 줄로 나눈다.
        JLabel sub = new JLabel(subtitle, Icons.icon(Icons.Kind.PAW, 13, Theme.ICON), SwingConstants.LEFT) {
            @Override
            public void setBounds(int x, int y, int w, int h) {
                super.setBounds(x, y, w, h);
                boolean full = w >= getFontMetrics(Theme.font(13)).stringWidth(subtitle) + 6 + 13;
                boolean one = full || w >= getFontMetrics(Theme.font(12)).stringWidth(subtitle);
                String text = one ? subtitle : twoLines(subtitle);
                if (!text.equals(getText()) || full != (getIcon() != null)) {
                    setFont(Theme.font(full ? 13 : 12));
                    setIcon(full ? Icons.icon(Icons.Kind.PAW, 13, Theme.ICON) : null);
                    setText(text);
                }
            }
        };
        sub.setHorizontalTextPosition(SwingConstants.LEFT);
        sub.setIconTextGap(6);
        sub.setFont(Theme.font(13));
        sub.setForeground(Theme.INK_SUB);
        JPanel words = new JPanel(new BorderLayout(0, 2));
        words.add(title, BorderLayout.NORTH);
        words.add(sub, BorderLayout.SOUTH);
        JPanel head = new JPanel(new BorderLayout(10, 0));
        head.setBorder(BorderFactory.createEmptyBorder(6, 4, 0, 0));
        head.add(icon, BorderLayout.WEST);
        head.add(words, BorderLayout.CENTER);
        heads.add(head, name);
        if (right != null) {
            JPanel r = new JPanel(new BorderLayout());
            r.add(right, BorderLayout.EAST);
            rights.add(r, name);
            ownRight.add(name);
        }
        center.add(screen, name);
        b.addActionListener(e -> show(name));
        if (navGroup.getButtonCount() == 1) {
            b.setSelected(true);
            current = name;
        }
    }

    /** 그 화면과 화면 머리, 오른쪽 카드를 보이고 그 메뉴만 고른다. model을 바꾸지 않아 알림 없이 다시 그린다(D-46). */
    public void show(String name) {
        current = name;
        flip(center, name);
        flip(heads, name);
        flip(rights, ownRight.contains(name) ? name : COMMON);
        for (Component c : nav.getComponents()) {
            JToggleButton b = (JToggleButton) c;
            b.setSelected(b.getText().equals(name));
        }
        redraw();
    }

    private static void flip(JPanel cards, String name) {
        ((CardLayout) cards.getLayout()).show(cards, name);
    }

    /** 사이드바 위의 로고(05 창 구조의 앱 이름). */
    private JPanel logo() {
        JPanel p = new JPanel(new BorderLayout(0, 2));
        JComponent face = new JComponent() {
            @Override
            protected void paintComponent(Graphics graphics) {
                // 앱이 그리는 고양이 얼굴(PetArt). 크기가 바뀌어도 흐려지지 않는다. 높이 64 칸에 폭 88까지 그린다.
                Graphics2D g = (Graphics2D) graphics.create();
                int w = Math.min(getWidth(), 88);
                PetArt.drawLogo(g, (getWidth() - w) / 2.0, 0, w, getHeight());
                g.dispose();
            }
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

    /** 가운데에 가장 가까운 띄어쓰기에서 두 줄로 나눈 HTML. 띄어쓰기가 없으면 그대로다. */
    static String twoLines(String text) {
        int mid = text.length() / 2;
        int best = -1;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == ' ' && (best < 0 || Math.abs(i - mid) < Math.abs(best - mid))) {
                best = i;
            }
        }
        return best < 0 ? text : "<html>" + text.substring(0, best) + "<br>" + text.substring(best + 1) + "</html>";
    }

    /** 06 25번 창 닫기(seq-uc19). 창의 닫기 버튼이 부른다. 트레이가 없으면 끝내고, 있으면 숨기고 처음이면 안내한다. */
    public void close(boolean traySupported, Runnable hide, Runnable notice) {
        CloseAction action = app.onClose(traySupported);
        if (action == CloseAction.QUIT) {
            app.quit();
            return;
        }
        hide.run();
        if (action == CloseAction.HIDE_WITH_NOTICE) {
            notice.run();
        }
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

    /**
     * 저장 실패 창(05 그 밖의 창과 알림). AppController의 errors로 넘긴다. 부모 없이 화면 가운데에 뜨고 확인을 누를 때까지
     * 기다린다(docs/porting/pick.md).
     */
    public static void saveFailed(String text) {
        savePane(text).createDialog(null, SAVE_FAILED).setVisible(true);
    }

    /** 저장 실패 창 내용: 오류 창에 06 22번 문장. 버튼 글은 로캘을 따르지 않게 "확인"으로 정한다(못 읽음 창과 같다). */
    static JOptionPane savePane(String text) {
        return new JOptionPane(text, JOptionPane.ERROR_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[] {"확인"}, "확인");
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
