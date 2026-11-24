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
        previewRoom = new RoomView(app.store, true);
        add(menu(), BorderLayout.WEST);
        add(center(), BorderLayout.CENTER);
        previewCard = preview();
        add(previewCard, BorderLayout.EAST);
        app.watch(this::refresh);
    }

    /** 왼쪽 메뉴와 쇼핑백 그림. */
    private JComponent menu() {
        ButtonGroup modes = new ButtonGroup();
        modes.add(shopMode);
        modes.add(ownedMode);
        JPanel col = new JPanel(new GridLayout(0, 1, 0, 8));
        Object[][] rows = {{shopMode, Icons.Kind.CART}, {ownedMode, Icons.Kind.BOX}, {previewToggle, Icons.Kind.EYE}};
        for (Object[] r : rows) {
            JToggleButton b = (JToggleButton) r[0];
            Icons.Kind k = (Icons.Kind) r[1];
            b.setFont(Theme.bold(13));
            b.setHorizontalAlignment(SwingConstants.LEFT);
            b.setIconTextGap(8);
            b.setBorder(BorderFactory.createEmptyBorder(9, 14, 11, 12));
            b.addChangeListener(e -> b.setIcon(Icons.icon(k, 20, b.isSelected() ? java.awt.Color.WHITE : Theme.ICON)));
            b.setIcon(Icons.icon(k, 20, b.isSelected() ? java.awt.Color.WHITE : Theme.ICON));
            col.add(b);
        }
        shopMode.addActionListener(e -> {
            first = 0;
            app.redraw();
        });
        ownedMode.addActionListener(e -> {
            first = 0;
            app.redraw();
        });
        previewToggle.addActionListener(e -> app.redraw());
        JComponent bag = new JComponent() {
            @Override
            protected void paintComponent(Graphics graphics) {
                if (Images.SHOP_BAG != null) {
                    Graphics2D g = (Graphics2D) graphics.create();
                    g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                    Images.fitBottom(g, Images.SHOP_BAG, 0, 0, getWidth(), getHeight());
                    g.dispose();
                }
            }
        };
        bag.setPreferredSize(new Dimension(130, 40));
        JPanel left = new JPanel(new BorderLayout(0, 8));
        left.add(col, BorderLayout.NORTH);
        left.add(bag, BorderLayout.CENTER);
        left.setPreferredSize(new Dimension(146, 0));
        return left;
    }

    /** 가운데: 분류 탭과 검색, 카드 줄. */
    private JComponent center() {
        ButtonGroup group = new ButtonGroup();
        JPanel tabRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0)) {
            @Override
            public void doLayout() {
                // 다섯 탭이 한 줄에 안 들어가면(최소 창) 아이콘을 빼고 글자만 둔다.
                for (Pill p : tabs) {
                    p.showIcon(true);
                }
                int need = 0;
                for (Pill p : tabs) {
                    need += p.getPreferredSize().width + 5;
                }
                for (Pill p : tabs) {
                    p.showIcon(need <= getWidth());
                }
                super.doLayout();
            }
        };
        for (int i = 0; i < TABS.size(); i++) {
            String name = TABS.get(i);
            Pill p = new Pill(name, TAB_ICONS[i], 12);
            p.setSelected(i == 0);
            p.addActionListener(e -> {
                category = name;
                first = 0;
                app.redraw();
            });
            group.add(p);
            tabs[i] = p;
            tabRow.add(p);
        }
        search.setFont(Theme.font(13));
        search.setColumns(10);
        search.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Theme.CARD_EDGE, 1, true),
                BorderFactory.createEmptyBorder(6, 30, 6, 10)));
        search.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void changed() {
                first = 0;
                app.redraw();
            }

            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                changed();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                changed();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                changed();
            }
        });
        JLabel glass = new JLabel(Icons.icon(Icons.Kind.SEARCH, 16, Theme.INK_SUB));
        JPanel searchBox = new JPanel(null) {
            @Override
            public void doLayout() {
                search.setBounds(0, 0, getWidth(), getHeight());
                glass.setBounds(9, (getHeight() - 16) / 2, 16, 16);
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(Math.max(110, Math.min(170, ShopPanel.this.getWidth() / 8)), 34);
            }
        };
        searchBox.add(glass);
        searchBox.add(search);
        searchBox.setComponentZOrder(glass, 0);
        JPanel searchWrap = new JPanel(new GridBagLayout());
        searchWrap.add(searchBox);
        JPanel top = new JPanel(new BorderLayout(8, 0));
        top.add(tabRow, BorderLayout.CENTER);
        top.add(searchWrap, BorderLayout.EAST);
        prev.addActionListener(e -> {
            first = Math.max(0, first - perPage());
            app.redraw();
        });
        next.addActionListener(e -> {
            first += perPage();
            app.redraw();
        });
        JPanel row = new JPanel(new BorderLayout(4, 0));
        row.add(prev, BorderLayout.WEST);
        row.add(cards, BorderLayout.CENTER);
        row.add(next, BorderLayout.EAST);
        JLabel guide = new JLabel(Ui.POINT_GUIDE, Icons.icon(Icons.Kind.INFO, 14, Theme.ICON), SwingConstants.LEFT);
        guide.setFont(Theme.regular(11));
        guide.setForeground(Theme.INK_SUB);
        guide.setIconTextGap(5);
        guide.setToolTipText(Ui.POINT_GUIDE);
        JPanel c = new JPanel(new BorderLayout(0, 6));
        c.add(top, BorderLayout.NORTH);
        c.add(row, BorderLayout.CENTER);
        c.add(guide, BorderLayout.SOUTH);
        return c;
    }

    /** 카드 줄에 들어가는 카드 수. */
    private int perPage() {
        int w = cards.getWidth() > 0 ? cards.getWidth() : 6 * (ShopCard.W + 10);
        return Math.max(2, (w + 10) / (ShopCard.W + 10));
    }

    /** 오른쪽 미리보기 칸. */
    private JPanel preview() {
        Card c = new Card(new BorderLayout(0, 6), 16);
        c.setBorder(BorderFactory.createEmptyBorder(8, 10, 10, 10));
        JLabel title = new JLabel("미리보기", Icons.icon(Icons.Kind.ROOM, 16, Theme.ICON), SwingConstants.LEFT);
        title.setFont(Theme.bold(14));
        title.setIconTextGap(6);
        CircleButton close = new CircleButton(Icons.Kind.CLOSE, 24, "미리보기 닫기", true);
        close.addActionListener(e -> {
            previewToggle.setSelected(false);
            app.redraw();
        });
        JPanel head = new JPanel(new BorderLayout());
        head.add(title, BorderLayout.WEST);
        head.add(close, BorderLayout.EAST);
        c.add(head, BorderLayout.NORTH);
        c.add(previewRoom, BorderLayout.CENTER);
