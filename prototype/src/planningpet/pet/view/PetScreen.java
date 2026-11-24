package planningpet.pet.view;

import planningpet.app.model.Store;
import planningpet.app.view.App;
import planningpet.common.view.*;
import planningpet.pet.model.Bond;
import planningpet.pet.model.PetState;
import planningpet.pet.model.Wardrobe;
import planningpet.reward.model.Level;
import planningpet.shop.model.Shop;
import planningpet.shop.view.ShopPanel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * 나의 방(05 나의 방, UC-14, UC-15, UC-16, D-47, D-48, D-52, D-53, D-56). 배치는 2026-11-23 나의 방 예시를 따른다.
 * 가운데 줄은 방(왼쪽, 위에 펫 칩과 더하기, 양옆에 화살표)과 펫 패널(오른쪽), 아래 줄은 상점이다.
 * 화면 머리 오른쪽에는 보유 포인트, 방 꾸미기 만족도, 배치 아이템 카드를 둔다.
 */
public final class PetScreen extends JPanel {
    static final int SIDE_W = 300;
    private final App app;
    public final RoomView room;
    public final ShopPanel shop;
    private String said;
    /** 펫 수. 늘었으면 데려온 직후라 그 인사를 방에서 한 번 말한다(D-56). */
    private int petCount;
    /** 머리 오른쪽 카드. */
    public final StatCard.Value pointStat = new StatCard.Value();
    public final StatCard.Value joyStat = new StatCard.Value();
    public final StatCard.Value placedStat = new StatCard.Value();
    private final Bar joyBar = new Bar(70, 10);
    /** 방 위 펫 칩(D-48). */
    private final JPanel chips = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
    public final CircleButton adopt = new CircleButton(Icons.Kind.PLUS, 40, "펫 데려오기", false);
    private final CircleButton left = new CircleButton(Icons.Kind.LEFT, 40, "앞 펫", false);
    private final CircleButton right = new CircleButton(Icons.Kind.RIGHT, 40, "다음 펫", false);
    /** 펫 패널. */
    private final JPanel petTabs = new JPanel(new GridLayout(1, 0, 8, 0));
    private final JLabel name = new JLabel();
    private final JLabel level = new JLabel();
    private final JLabel expText = new JLabel();
    private final Bar expBar = new Bar(0, 10);
    private final CardLayout tabCards = new CardLayout();
    private final JPanel tabBody = new JPanel(tabCards);
    private final Pill[] infoTabs = {new Pill("상태", Icons.Kind.CHART, 12), new Pill("꾸미기", Icons.Kind.SHIRT, 12),
        new Pill("정보", Icons.Kind.INFO, 12)};
    private final JPanel slots = new JPanel(new GridLayout(1, 4, 8, 0));
    private final JLabel stateText = new JLabel();
    private final JLabel infoText = new JLabel();

    public PetScreen(App app) {
        super(new BorderLayout(0, Theme.GAP));
        this.app = app;
        room = new RoomView(app.store);
        room.add(chips);
        room.add(left);
        room.add(right);
        left.addActionListener(e -> app.pet.step(-1));
        right.addActionListener(e -> app.pet.step(1));
        adopt.addActionListener(e -> PickDialog.adopt(app));
        Card roomCard = new Card(new BorderLayout(), Theme.CARD_RADIUS) {
            @Override
            public void doLayout() {
                super.doLayout();
                int w = room.getWidth();
                int h = room.getHeight();
                Dimension c = chips.getPreferredSize();
                chips.setBounds(14, 12, Math.min(c.width, w - 28), c.height);
                left.setBounds(10, (h - 40) / 2, 40, 40);
                right.setBounds(w - 50, (h - 40) / 2, 40, 40);
            }
        };
        roomCard.setBorder(BorderFactory.createEmptyBorder(5, 5, 7, 5));
        roomCard.add(room);
        chips.setOpaque(false);
        JComponent side = petPanel();
        JPanel middle = new JPanel(null) {
            @Override
            public void doLayout() {
                int sideW = Math.max(SIDE_W - 30, Math.min(SIDE_W + 60, getWidth() / 4));
                int roomW = getWidth() - sideW - Theme.GAP;
                roomCard.setBounds(0, 0, roomW, getHeight());
                side.setBounds(roomW + Theme.GAP, 0, sideW, getHeight());
                roomCard.doLayout();
            }
        };
        middle.add(roomCard);
        middle.add(side);
        add(middle, BorderLayout.CENTER);
        // 산 뒤 한 번 하는 말은 다음 데이터 변경까지 남는다. 화면 전환 같은 다시 그리기로는 지우지 않는다(D-56).
        app.store.onChange(() -> room.bubble = "");
        petCount = app.store.pets.size();
        shop = new ShopPanel(app, this::sayOnce);
        shop.setPreferredSize(new Dimension(0, 236));
        add(shop, BorderLayout.SOUTH);
        app.watch(this::refresh);
    }

    /** 화면 머리 오른쪽의 수치 카드 셋(D-47, D-52). */
    public JComponent headRight() {
        Card points = StatCard.of(Icons.Kind.COIN, pointStat, null);
        JPanel joy = new JPanel(new BorderLayout(10, 0));
        joy.add(joyStat, BorderLayout.WEST);
        JPanel barWrap = new JPanel(new java.awt.GridBagLayout());
        barWrap.add(joyBar);
        joy.add(barWrap, BorderLayout.CENTER);
        Card joyCard = new Card(new BorderLayout(), Theme.CARD_RADIUS);
        joyCard.setBorder(BorderFactory.createEmptyBorder(8, 16, 12, 16));
        joyCard.add(joy);
        Card placed = StatCard.of(Icons.Kind.SOFA, placedStat, null);
        JPanel row = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        row.add(points);
        row.add(joyCard);
        row.add(placed);
        return row;
    }

    /** 오른쪽 펫 패널: 펫 탭, 초상과 이름과 레벨과 경험치, 상태와 꾸미기와 정보 탭(05 나의 방). */
    private JComponent petPanel() {
        Card c = new Card(new BorderLayout(0, 10), Theme.CARD_RADIUS);
        c.setBorder(BorderFactory.createEmptyBorder(12, 12, 14, 12));
        c.add(petTabs, BorderLayout.NORTH);
        JComponent portrait = new JComponent() {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                int size = Math.min(getWidth(), getHeight() - 4);
                Pet.drawAt(g, app.store.pet(), getWidth() / 2, getHeight() - 4, size);
                g.dispose();
            }
        };
        portrait.setPreferredSize(new Dimension(104, 104));
        name.setFont(Theme.bold(15));
        name.setIcon(Icons.icon(Icons.Kind.PAW, 16, Theme.ICON));
        name.setIconTextGap(6);
        level.setFont(Theme.heavy(26));
        expText.setFont(Theme.font(12));
        expText.setForeground(Theme.INK_SUB);
        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        for (JComponent part : new JComponent[] {name, level, expText}) {
            part.setAlignmentX(Component.LEFT_ALIGNMENT);
            info.add(part);
        }
        info.add(Box.createVerticalStrut(6));
        expBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        expBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 10));
        info.add(expBar);
        JPanel who = new JPanel(new BorderLayout(10, 0));
        who.add(portrait, BorderLayout.WEST);
        JPanel infoWrap = new JPanel(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints k = new java.awt.GridBagConstraints();
        k.weightx = 1;
        k.fill = java.awt.GridBagConstraints.HORIZONTAL;
        infoWrap.add(info, k);
        who.add(infoWrap, BorderLayout.CENTER);
        Card whoCard = new Card(new BorderLayout(), 14);
        whoCard.setBorder(BorderFactory.createEmptyBorder(6, 8, 10, 12));
        whoCard.add(who);
        ButtonGroup tg = new ButtonGroup();
        JPanel tabRow = new JPanel(new GridLayout(1, 3, 6, 0));
        String[] names = {"상태", "꾸미기", "정보"};
        for (int i = 0; i < infoTabs.length; i++) {
            String n = names[i];
            tg.add(infoTabs[i]);
            tabRow.add(infoTabs[i]);
            infoTabs[i].addActionListener(e -> tabCards.show(tabBody, n));
        }
        infoTabs[1].setSelected(true);
        for (JLabel l : new JLabel[] {stateText, infoText}) {
            l.setFont(Theme.font(13));
            l.setVerticalAlignment(JLabel.TOP);
        }
        tabBody.add(stateText, "상태");
        tabBody.add(slots, "꾸미기");
        tabBody.add(infoText, "정보");
        tabCards.show(tabBody, "꾸미기");
        JButton more = new JButton("캐릭터에서 꾸미기");
        more.setFont(Theme.bold(12));
        more.setIcon(Icons.icon(Icons.Kind.GAMEPAD, 16, Theme.ICON));
        more.setBorder(BorderFactory.createEmptyBorder(6, 10, 8, 10));
        more.addActionListener(e -> app.show("캐릭터"));
        JPanel lower = new JPanel(new BorderLayout(0, 8));
        lower.add(tabRow, BorderLayout.NORTH);
        lower.add(tabBody, BorderLayout.CENTER);
        lower.add(more, BorderLayout.SOUTH);
        JPanel body = new JPanel(new BorderLayout(0, 10));
        body.add(whoCard, BorderLayout.NORTH);
        body.add(lower, BorderLayout.CENTER);
        c.add(body, BorderLayout.CENTER);
        return c;
    }

    /** 장신구 칸 하나: 입은 장신구 그림, 없으면 칸 아이콘. 누르면 캐릭터 화면으로 간다. */
    private JComponent slot(String slotName, String worn) {
        Icons.Kind kind = switch (slotName) {
            case Wardrobe.NECK -> Icons.Kind.COLLAR;
            case Wardrobe.HAT -> Icons.Kind.HAT;
            case Wardrobe.ACCESSORY -> Icons.Kind.BOW;
            default -> Icons.Kind.SHIRT;
        };
        JButton b = new JButton() {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                int s = Math.min(getWidth(), getHeight()) - 4;
                int x = (getWidth() - s) / 2;
                Card.shadow(g, x, 1, s, s, 14);
                g.setColor(worn != null ? Theme.MINT_PALE : Theme.CARD_TOP);
                g.fillRoundRect(x, 1, s, s, 14, 14);
                g.setColor(worn != null ? Theme.MINT : Theme.CARD_EDGE);
                g.setStroke(new java.awt.BasicStroke(worn != null ? 2f : 1f));
                g.drawRoundRect(x, 1, s, s, 14, 14);
                java.awt.image.BufferedImage im = worn == null ? null : Images.wear(worn);
                int p = s * 3 / 5;
                if (im != null) {
                    Images.fit(g, im, x + (s - p) / 2, 1 + (s - p) / 2, p, p);
                } else {
                    Icons.draw(g, kind, x + (s - p) / 2, 1 + (s - p) / 2, p, worn != null ? Theme.ICON : Theme.MINT_LINE);
                }
                g.dispose();
            }
        };
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setToolTipText(slotName + ": " + (worn == null ? "안 입었어요" : worn));
        b.setPreferredSize(new Dimension(56, 58));
        b.addActionListener(e -> app.show("캐릭터"));
        return b;
    }

    /** 아이템을 사거나 펫을 데려온 직후처럼 한 번만 글을 말할 때(D-56). */
    public void sayOnce(String line) {
        said = line;
    }

    private void refresh() {
        Store s = app.store;
        long done = s.book.checkables(s.today()).stream().filter(t -> t.done).count();
        if (said != null) {
            room.bubble = said;
            said = null;
        } else if ("pet".equals(app.current()) && !wasVisible) {
            room.bubble = done > 0 ? "오늘 " + done + "개 끝냈어." : "오늘은 아직 끝낸 일이 없어.";
        }
        wasVisible = "pet".equals(app.current());
        decoText.setText("가진 아이템 " + s.owned.size() + " / " + Shop.ITEMS.size());
        decoBar.setValue(s.owned.size());
        items.removeAll();
        items.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 6, 4));
        for (String item : Shop.ITEMS.keySet()) {
            if (!s.owned.contains(item)) {
                continue;
            }
            boolean placed = s.placed.contains(item);
            JToggleButton b = new JToggleButton(placed ? item + " ✓" : item, placed);
            b.addActionListener(e -> {
                app.pet.toggle(item);
            });
            items.add(b);
        }
        items.revalidate();
        room.repaint();
    }
}
