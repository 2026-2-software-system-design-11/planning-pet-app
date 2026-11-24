package planningpet.shop.view;

import planningpet.app.model.Store;
import planningpet.app.view.App;
import planningpet.common.view.*;
import planningpet.pet.view.RoomView;
import planningpet.shop.model.Shop;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

/**
 * 나의 방 아래 칸의 상점(D-53, UC-15, UC-16). 왼쪽 메뉴(꾸미기 상점, 보유한 아이템, 미리보기), 가운데 분류 탭과 검색과 카드 줄,
 * 오른쪽 미리보기(고른 아이템을 놓은 방, 이름, 가격, 취소, 구매하기)다.
 */
public final class ShopPanel extends Card {
    public static final List<String> TABS = List.of("전체", "가구", "장식", "장난감", "기타");
    private static final Icons.Kind[] TAB_ICONS = {Icons.Kind.GRID, Icons.Kind.SOFA, Icons.Kind.PLANT, Icons.Kind.BALL, Icons.Kind.DOTS};

    private final App app;
    /** 산 뒤 펫이 한 번 하는 말을 받는다(D-56). */
    private final Consumer<String> thanks;
    public final JToggleButton shopMode = new JToggleButton("꾸미기 상점", true);
    public final JToggleButton ownedMode = new JToggleButton("보유한 아이템");
    public final JToggleButton previewToggle = new JToggleButton("미리보기", true);
    public final JTextField search = new JTextField() {
        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            if (getText().isEmpty()) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g.setColor(Theme.INK_DIM);
                g.setFont(getFont());
                g.drawString("아이템 검색...", getInsets().left, (getHeight() + g.getFontMetrics().getAscent()) / 2 - 2);
                g.dispose();
            }
        }
    };
    private final Pill[] tabs = new Pill[TABS.size()];
    /** 카드 줄. 폭이 바뀌어 한 줄에 들어가는 카드 수가 달라지면 다시 채운다. */
    public final JPanel cards = new JPanel(new GridLayout(1, 0, 10, 0)) {
        @Override
        public void doLayout() {
            if (getWidth() > 0 && perPage() != shownPer) {
                shownPer = perPage();
                refill();
            }
            super.doLayout();
        }
    };
    private int shownPer;
    public final CircleButton prev = new CircleButton(Icons.Kind.LEFT, 30, "앞 아이템", true);
    public final CircleButton next = new CircleButton(Icons.Kind.RIGHT, 30, "다음 아이템", true);
    private final JPanel previewCard;
    public final RoomView previewRoom;
    public final JLabel previewName = new JLabel();
    public final JLabel previewPrice = new JLabel();
    public final JLabel lack = new JLabel(" ");
    public final JButton cancel = new JButton("취소");
    /** 구매하기, 방에 놓기, 방에서 치우기 가운데 하나. */
    public final JButton action = new JButton("구매하기");
    private String category = "전체";
    public String selected;
    private int first;

    public ShopPanel(App app, Consumer<String> thanks) {
        super(new BorderLayout(Theme.GAP, 0), Theme.CARD_RADIUS);
        this.app = app;
        this.thanks = thanks;
        setBorder(BorderFactory.createEmptyBorder(10, 12, 12, 12));
