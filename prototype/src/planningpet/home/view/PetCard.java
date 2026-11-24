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
