package planningpet.home.view;

import planningpet.app.model.Store;
import planningpet.common.view.*;
import planningpet.pet.model.PetState;
import planningpet.pet.view.Pet;
import planningpet.reward.model.DailyProgress;
import planningpet.reward.model.Level;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

/**
 * 홈의 펫 카드(05 메인, D-59). 하늘과 풀밭 장면 왼쪽에 고른 펫(D-48), 오른쪽 위에 이름과 레벨, 경험치 막대,
 * 흰 작은 카드에 "오늘 끝낸 일"과 칸 막대(06의 13번), 그 아래 펫 옆 말풍선. 누르면 캐릭터 창이 뜬다(D-58).
 * 배치는 2026-11-24 홈 예시의 펫 카드를 따른다. 값은 디자인 시스템 10절 "홈"에 있다.
 */
public final class PetCard extends JComponent {
    /** 바깥 틀 안쪽 여백과 줄 사이. */
    private static final int PAD = 14;
    private static final int GAP = 8;
    private final Store store;
    private final Runnable open;
    private final Bar expBar = new Bar(0, 8).track(Theme.TRACK_DEEP);
    private final Bar doneBar = new Bar(0, 8).track(Theme.TRACK_DEEP);
    private boolean over;
    private boolean down;
    /** 마지막으로 그린 말풍선 자리와 잘렸는지(툴팁에 전체 글을 보인다). */
    private Rectangle bubbleBox;
    private boolean bubbleCut;
    /** 펫 숨쉬기(PetArt의 t). 창에 붙어 보일 때만 돈다. 창 없이 그릴 때는 0이다. */
    private javax.swing.Timer clock;
    private long start;
    private double t;

    public PetCard(Store store, Runnable open) {
        this.store = store;
        this.open = open;
        setLayout(null);
        add(expBar);
        add(doneBar);
        setFocusable(true);
        setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        setToolTipText("");
        getAccessibleContext().setAccessibleName("펫 카드. 누르면 캐릭터 창이 열려요");
        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                over = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                over = false;
                down = false;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                down = true;
                requestFocusInWindow();
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                boolean click = down && contains(e.getPoint());
                down = false;
                repaint();
                if (click && javax.swing.SwingUtilities.isLeftMouseButton(e)) {
                    open.run();
                }
            }
        };
        addMouseListener(mouse);
        addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                repaint();
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                repaint();
            }
        });
        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "open");
        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "open");
        getActionMap().put("open", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                open.run();
            }
        });
        store.onChange(this::refresh);
        refresh();
    }

    /** 화면 읽기 프로그램에는 누르는 버튼으로 알린다. 이름과 설명(펫, 레벨, 오늘 끝낸 일, 말)은 refresh가 채운다. */
    @Override
    public javax.accessibility.AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = new AccessibleJComponent() {
                @Override
                public javax.accessibility.AccessibleRole getAccessibleRole() {
                    return javax.accessibility.AccessibleRole.PUSH_BUTTON;
                }
            };
        }
        return accessibleContext;
    }

    /** 누른 것과 같다(QA가 부른다). */
    public void click() {
        open.run();
    }

    /** "나비". */
    public String nameText() {
        return store.pet().name;
    }

    /** "Lv. 3". */
    public String levelText() {
        return "Lv. " + Level.level(store.pet().exp);
    }

    /** "경험치 80 / 110", 레벨 10이면 "최고 레벨"(06의 10번). */
    public String expText() {
        return Level.expText(store.pet().exp);
    }

    /** "오늘 끝낸 일 1 / 4". 오늘 일정이 없으면 "0 / 0"이다(06의 13번). */
    public String doneText() {
        DailyProgress p = DailyProgress.of(store.book, store.today());
        return "오늘 끝낸 일 " + p.done() + " / " + p.total();
    }

    /** 칸 막대의 켜진 칸 수와 칸 수(이어진 막대면 칸 수 0). 검사용. */
    public String doneBarText() {
        return Math.round(doneBar.ratio() * 100) + "% " + doneBar.segments() + "칸";
    }

    public String bubbleText() {
        return store.talk == null ? "" : store.talk;
    }

    private void refresh() {
        PetState p = store.pet();
        expBar.set(Level.expRatio(p.exp));
        DailyProgress d = DailyProgress.of(store.book, store.today());
        doneBar.set((int) d.done(), d.total());
        getAccessibleContext().setAccessibleDescription(nameText() + " " + levelText() + ", " + expText() + ", "
                + doneText() + (bubbleText().isBlank() ? "" : ", " + bubbleText()));
        revalidate();
        repaint();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        if (clock == null) {
            start = System.nanoTime();
            clock = new javax.swing.Timer(40, e -> {
                if (isShowing()) {
                    t = (System.nanoTime() - start) / 1e9;
