package planningpet.app.view;

import planningpet.app.controller.AppController;
import planningpet.app.controller.DayWatcher;
import planningpet.app.model.Store;
import planningpet.common.view.*;
import planningpet.home.controller.HomeController;
import planningpet.pet.controller.PetController;
import planningpet.pet.model.Talk;
import planningpet.reward.controller.RewardController;
import planningpet.reward.model.Game;
import planningpet.schedule.controller.TaskController;
import planningpet.shop.controller.ShopController;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Map;
import javax.swing.SwingConstants;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

/** 창 틀(05 "창 구조"): 위 막대, 왼쪽 내비게이션, 가운데 화면 전환. 창 없이도 만들 수 있다. */
public final class App {
    public final Store store;
    public final Game game;
    public final Talk talk;
    public final AppController control;
    public final TaskController schedule;
    public final HomeController home;
    public final ShopController shop;
    public final PetController pet;
    private final RewardController reward;
    /** 창 바탕(디자인 시스템 1절): 아쿠아 그라데이션, 왼쪽 아래에 구름과 덤불. */
    public final JPanel root = new JPanel(new BorderLayout(Theme.GAP, 0)) {
        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setPaint(new GradientPaint(0, 0, Theme.BG_TOP, 0, getHeight(), Theme.BG_BOTTOM));
            g.fillRect(0, 0, getWidth(), getHeight());
            g.dispose();
        }

        @Override
        protected void paintChildren(Graphics graphics) {
            super.paintChildren(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            int side = navWrap.getX() + navWrap.getWidth();
            g.clipRect(0, 0, side + 4, getHeight());
            java.awt.image.BufferedImage sleeper = "통계".equals(current) ? planningpet.stats.view.StatsScreen.sleepingCat() : null;
            if (sleeper != null) {
                // 통계 예시: 사이드바 아래 구름 자리에 방석에서 자는 고양이.
                int sw = (int) (side * 0.9);
                int sh = sw * sleeper.getHeight() / sleeper.getWidth();
                g.drawImage(sleeper, 4, getHeight() - sh - 6, sw, sh, null);
            } else if (Images.CLOUD != null) {
                int cw = (int) (side * 0.62);
                int ch = cw * Images.CLOUD.getHeight() / Images.CLOUD.getWidth();
                g.drawImage(Images.CLOUD, side - cw - 4, getHeight() - (int) (side * 1.05) - ch, cw, ch, null);
            }
            if (Images.MEADOW != null && sleeper == null) {
                // 예시에서 잘라 낸 덤불은 왼쪽 아래가 창 모서리처럼 둥글다. 그 모서리가 창 밖으로 나가게 조금 내려 그린다.
                int mw = (int) (side * 0.86);
                int mh = mw * Images.MEADOW.getHeight() / Images.MEADOW.getWidth();
                g.drawImage(Images.MEADOW, -mw / 14, getHeight() - mh + mh / 14, mw, mh, null);
            }
            g.dispose();
        }
    };
    private final CardLayout cards = new CardLayout();
    private final JPanel center = new JPanel(cards);
    /** 화면마다 다른 머리(아이콘, 제목, 부제). 화면과 함께 바뀐다. */
    private final CardLayout headCards = new CardLayout();
    private final JPanel heads = new JPanel(headCards);
    private final JPanel nav = new JPanel(new GridLayout(0, 1, 0, 6));
    private final Card navWrap = new Card(new BorderLayout(), 22).filled(Theme.SIDE);
    /** 메뉴와 화면 머리의 아이콘(디자인 시스템 9절). */
    private static final Map<String, Icons.Kind> ICONS = Map.of("홈", Icons.Kind.HOME, "일정관리", Icons.Kind.CALENDAR,
            "캐릭터", Icons.Kind.GAMEPAD, "나의 방", Icons.Kind.ROOM, "통계", Icons.Kind.CHART, "설정", Icons.Kind.GEAR);
    /** 화면 머리 오른쪽 카드(D-47). 화면과 함께 바뀐다. */
    private final CardLayout rightCards = new CardLayout();
    private final JPanel rights = new JPanel(rightCards);
    private static final String COMMON = "공통";
    private final java.util.Set<String> ownRight = new java.util.HashSet<>();
    private final ButtonGroup navGroup = new ButtonGroup();
    public final StatCard.Value streak = new StatCard.Value();
    public final StatCard.Value points = new StatCard.Value();
    public final JButton attend = new JButton();
    /** 06의 23번. Main.create가 넣는다. */
    public DayWatcher watcher;
    private final java.util.List<Runnable> views = new java.util.ArrayList<>();

    public App(Store store) {
        this.store = store;
        this.game = new Game(store);
        this.talk = new Talk(store);
        this.control = new AppController(store, talk);
        this.schedule = new TaskController(this);
        this.home = new HomeController(control, store, talk);
        this.shop = new ShopController(control, store);
        this.pet = new PetController(control, store);
        this.reward = new RewardController(control, store, game);
        navWrap.setBorder(BorderFactory.createEmptyBorder(18, 8, 12, 8));
        navWrap.add(logo(), BorderLayout.NORTH);
        JPanel navTop = new JPanel(new BorderLayout());
        navTop.setBorder(BorderFactory.createEmptyBorder(18, 0, 0, 0));
        navTop.add(nav, BorderLayout.NORTH);
        navWrap.add(navTop, BorderLayout.CENTER);
        navWrap.setPreferredSize(new Dimension(140, 0));
        root.add(navWrap, BorderLayout.WEST);
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        Card main = new Card(new BorderLayout(0, 10), 24).filled(Theme.PANEL);
        main.setBorder(BorderFactory.createEmptyBorder(10, 12, 14, 12));
        main.add(topRow(), BorderLayout.NORTH);
        main.add(center, BorderLayout.CENTER);
        root.add(main, BorderLayout.CENTER);
        root.setPreferredSize(new Dimension(1000, 680));
        attend.addActionListener(e -> reward.attend());
        watch(this::refresh);
    }

    /** 데이터 변경 알림과 다시 그리기 신호를 함께 받는다. */
    public void watch(Runnable refresh) {
        store.onChange(refresh);
        views.add(refresh);
    }

    /** 화면 전환이나 주 옮기기처럼 model을 바꾸지 않을 때 화면을 다시 그린다(D-46). */
    public void redraw() {
        views.forEach(Runnable::run);
    }

    /** 사이드바 위의 로고(05 창 구조의 앱 이름). */
    private JPanel logo() {
        JPanel p = new JPanel(new BorderLayout(0, 2));
        javax.swing.JComponent face = new javax.swing.JComponent() {
            @Override
            protected void paintComponent(Graphics graphics) {
                java.awt.image.BufferedImage im = Images.LOGO;
                if (im != null) {
                    Graphics2D g = (Graphics2D) graphics.create();
                    g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                    Images.fit(g, im, 0, 0, getWidth(), getHeight());
                    g.dispose();
                }
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

    /** 본문 위 줄: 왼쪽은 화면 머리, 오른쪽은 모든 화면에 같은 연속 접속, 보유 포인트, 출석(05 창 구조). */
    private JPanel topRow() {
        JPanel row = new JPanel(new BorderLayout(Theme.GAP, 0));
        row.add(heads, BorderLayout.CENTER);
        attend.putClientProperty(RoundButtonUI.PRIMARY, Boolean.TRUE);
        attend.setFont(Theme.bold(13));
        attend.setBorder(BorderFactory.createEmptyBorder(7, 14, 9, 14));
        Card streakCard = StatCard.of(Icons.Kind.FLAME, streak, attend);
        Card pointCard = StatCard.of(Icons.Kind.COIN, points, null);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.add(streakCard);
        right.add(pointCard);
        rights.add(right, COMMON);
        row.add(rights, BorderLayout.EAST);
        return row;
    }

    private String current = "";

    /** 지금 보이는 화면의 이름. */
    public String current() {
        return current;
    }

    /** 내비게이션 버튼과 화면을 함께 넣는다. 화면 위에 제목과 부제를 둔다(D-33). screen이 null이면 비활성 버튼(통계, D-05)이다. */
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
        if (screen == null) {
            b.setEnabled(false);
            b.setToolTipText("다음 버전에서 열려요");
            return;
        }
        JLabel icon = new JLabel(Icons.icon(kind, 40, Theme.ICON));
        JLabel title = new JLabel(name);
        title.setFont(Theme.heavy(26));
        JLabel sub = new JLabel(subtitle, Icons.icon(Icons.Kind.PAW, 13, Theme.ICON), SwingConstants.LEFT);
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

    public void show(String name) {
        current = name;
        cards.show(center, name);
        headCards.show(heads, name);
        rightCards.show(rights, ownRight.contains(name) ? name : COMMON);
        for (var c : nav.getComponents()) {
            JToggleButton b = (JToggleButton) c;
            b.setSelected(b.getText().equals(name));
        }
        redraw();
    }

    public void refresh() {
        streak.set("연속 접속", store.streak + "일");
        points.set("보유", String.format("%,dP", store.points));
        boolean attended = store.today().equals(store.attendanceDay);
        attend.setText(attended ? "출석 완료" : "출석 +20P");
        attend.setEnabled(!attended);
    }
}
