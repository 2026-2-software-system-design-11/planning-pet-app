package planningpet.proto;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
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
    public final Actions actions;
    public final JPanel root = new JPanel(new BorderLayout()) {
        @Override
        protected void paintComponent(java.awt.Graphics g) {
            super.paintComponent(g);
            if (Images.LANDSCAPE != null) {
                g.drawImage(Images.LANDSCAPE, 0, 0, getWidth(), getHeight(), null);
            }
        }
    };
    private final CardLayout cards = new CardLayout();
    private final JPanel center = new JPanel(cards);
    private final JPanel nav = new JPanel(new GridLayout(0, 1, 0, 6));
    private final ButtonGroup navGroup = new ButtonGroup();
    final JLabel streak = new JLabel();
    final JLabel points = new JLabel();
    final JButton attend = new JButton();

    public App(Store store) {
        this.store = store;
        this.game = new Game(store);
        this.talk = new Talk(store);
        this.actions = new Actions(this);
        root.add(topBar(), BorderLayout.NORTH);
        JPanel navWrap = new Card(new BorderLayout(), Theme.CARD_RADIUS).filled(Theme.SIDE);
        navWrap.setBorder(BorderFactory.createEmptyBorder(12, 10, 12, 10));
        navWrap.add(nav, BorderLayout.NORTH);
        root.add(navWrap, BorderLayout.WEST);
        root.setBorder(BorderFactory.createEmptyBorder(0, 12, 12, 12));
        root.add(center, BorderLayout.CENTER);
        root.setPreferredSize(new Dimension(1000, 680));
        attend.addActionListener(e -> {
            if (game.attend()) {
                store.say("출석했어! +20P");
                store.changed();
            }
        });
        store.onChange(this::refresh);
    }

    private JPanel topBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        JLabel name = new JLabel("플래닝펫");
        name.setFont(Theme.bold(23));
        bar.add(name, BorderLayout.WEST);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        right.add(streak);
        right.add(points);
        right.add(attend);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private String current = "";

    /** 지금 보이는 화면의 이름. */
    public String current() {
        return current;
    }

    /** 내비게이션 버튼과 화면을 함께 넣는다. 화면 위에 제목과 부제를 둔다(D-33). screen이 null이면 비활성 버튼(통계, D-05)이다. */
    public void addScreen(String name, String subtitle, JComponent screen) {
        JToggleButton b = new JToggleButton(name);
        b.putClientProperty(RoundButtonUI.NAV, Boolean.TRUE);
        b.setFont(Theme.bold(20));
        navGroup.add(b);
        nav.add(b);
        if (screen == null) {
            b.setEnabled(false);
            b.setToolTipText("다음 버전에서 열려요");
            return;
        }
        JPanel page = new JPanel(new BorderLayout());
        JLabel title = new JLabel(name);
        title.setFont(Theme.bold(36));
        JLabel sub = new JLabel(subtitle);
        sub.setFont(Theme.font(19));
        sub.setForeground(Theme.INK_DIM);
        JPanel head = Ui.flow(title, sub);
        head.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        page.add(head, BorderLayout.NORTH);
        page.add(screen, BorderLayout.CENTER);
        center.add(page, name);
        b.addActionListener(e -> show(name));
        if (navGroup.getButtonCount() == 1) {
            b.setSelected(true);
            current = name;
        }
    }

    public void show(String name) {
        current = name;
        cards.show(center, name);
        for (var c : nav.getComponents()) {
            JToggleButton b = (JToggleButton) c;
            b.setSelected(b.getText().equals(name));
        }
        store.changed();
    }

    public void refresh() {
        streak.setText("<html>연속 접속 <b>" + store.streak + "일</b></html>");
        points.setText("<html>보유 <b>" + store.points + "P</b></html>");
        boolean attended = store.today().equals(store.attendanceDay);
        attend.setText(attended ? "출석 완료" : "출석 +20P");
        attend.setEnabled(!attended);
    }
}
