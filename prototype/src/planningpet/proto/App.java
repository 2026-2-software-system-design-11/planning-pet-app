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
    public final JPanel root = new JPanel(new BorderLayout());
    private final CardLayout cards = new CardLayout();
    private final JPanel center = new JPanel(cards);
    private final JPanel nav = new JPanel(new GridLayout(0, 1, 0, 6));
    private final ButtonGroup navGroup = new ButtonGroup();
    private final JLabel streak = new JLabel();
    private final JLabel points = new JLabel();
    private final JButton attend = new JButton();

    public App(Store store) {
        this.store = store;
        this.game = new Game(store);
        this.talk = new Talk(store);
        this.actions = new Actions(this);
        root.add(topBar(), BorderLayout.NORTH);
        JPanel navWrap = new JPanel(new BorderLayout());
        navWrap.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        navWrap.add(nav, BorderLayout.NORTH);
        root.add(navWrap, BorderLayout.WEST);
        root.add(center, BorderLayout.CENTER);
        root.setPreferredSize(new Dimension(1000, 680));
        attend.addActionListener(e -> {
            if (game.attend()) {
                store.talk = "출석했어! +20P";
                store.changed();
            }
        });
        store.onChange(this::refresh);
    }

    private JPanel topBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        JLabel name = new JLabel("<html><b>플래닝펫</b></html>");
        bar.add(name, BorderLayout.WEST);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        right.add(streak);
        right.add(points);
        right.add(attend);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    /** 내비게이션 버튼과 화면을 함께 넣는다. screen이 null이면 비활성 버튼(통계, D-05)이다. */
    public void addScreen(String name, JComponent screen) {
        JToggleButton b = new JToggleButton(name);
        navGroup.add(b);
        nav.add(b);
        if (screen == null) {
            b.setEnabled(false);
            b.setToolTipText("다음 버전에서 열려요");
            return;
        }
        center.add(screen, name);
        b.addActionListener(e -> show(name));
        if (navGroup.getButtonCount() == 1) {
            b.setSelected(true);
        }
    }

    public void show(String name) {
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
