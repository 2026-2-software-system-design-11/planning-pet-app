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
 * 가운데 줄은 방(왼쪽, 위에 펫 칩과 더하기)과 펫 패널(오른쪽), 아래 줄은 상점이다. 방 화살표는 없다(D-80).
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
    /** 캐릭터 창 안(D-58). 창을 열 때마다 같은 것을 담는다. */
    public final CharacterScreen character;
    /** 캐릭터 창을 연다. 인자가 true면 꾸미기 탭, false면 상태 탭으로 연다(D-93). QA는 창 대신 무엇을 열었는지 받는다. */
    public java.util.function.Consumer<Boolean> characterOpener;
    /** 펫 패널 맨 아래 "{이름} 꾸미기"(D-83). */
    public final JButton dressButton = new JButton();
    /** 펫 패널. */
    private final JPanel petTabs = new JPanel(new GridLayout(1, 0, 8, 0));
    private final JLabel name = new JLabel();
    /** 이름 옆 작은 종류(D-81). */
    private final JLabel kindTag = new JLabel();
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
        character = new CharacterScreen(app);
        characterOpener = dress -> CharacterDialog.open(app, character, dress);
        room.add(chips);
        adopt.addActionListener(e -> AdoptDialog.show(app));
        Card roomCard = new Card(new BorderLayout(), Theme.CARD_RADIUS) {
            @Override
            public void doLayout() {
                super.doLayout();
                int w = room.getWidth();
                Dimension c = chips.getPreferredSize();
                chips.setBounds(14, 12, Math.min(c.width, w - 28), c.height);
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
        app.store.onChange(() -> {
            room.bubble = "";
            room.bubbleKind = null;
        });
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
        kindTag.setFont(Theme.font(11));
        kindTag.setForeground(Theme.INK_SUB);
        JPanel nameRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        nameRow.add(name);
        nameRow.add(Box.createHorizontalStrut(5));
        nameRow.add(kindTag);
        nameRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, nameRow.getPreferredSize().height + 4));
        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        for (JComponent part : new JComponent[] {nameRow, level, expText}) {
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
        JButton more = dressButton;
        more.setFont(Theme.bold(12));
        more.setIcon(Icons.icon(Icons.Kind.GAMEPAD, 16, Theme.ICON));
        more.setBorder(BorderFactory.createEmptyBorder(6, 10, 8, 10));
        // D-83: 누르면 캐릭터 창이 꾸미기 탭으로 열린다.
        more.addActionListener(e -> characterOpener.accept(true));
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

    /** 장신구 칸 하나: 입은 장신구 그림, 없으면 칸 아이콘. 누르면 캐릭터 창을 꾸미기 탭으로 연다(D-93). */
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
        b.addActionListener(e -> characterOpener.accept(true));
        return b;
    }

    /** 아이템을 사거나 펫을 데려온 직후처럼 한 번만 글을 말할 때(D-56). */
    public void sayOnce(String line) {
        said = line;
    }

    private void refresh() {
        Store s = app.store;
        PetState p = s.pet();
        pointStat.set("보유 포인트", String.format("%,dP", s.points));
        int joy = Shop.satisfaction(s.placed);
        joyStat.set("방 꾸미기 만족도", joy + "%");
        joyBar.set(joy / 100.0);
        placedStat.set("배치 아이템", s.placed.size() + " / " + Shop.ITEMS.size());
        chips.removeAll();
        petTabs.removeAll();
        ButtonGroup chipGroup = new ButtonGroup();
        ButtonGroup tabGroup = new ButtonGroup();
        for (String kind : new String[] {"CAT", "DOG"}) {
            PetState q = s.pet(kind);
            if (q == null) {
                continue;
            }
            // D-81: 지은 이름과 레벨, 종류는 이름 옆에 작게.
            Pill chip = new Pill(q.name, null, 13).small(q.kindName()).picture(Pet.face(q)).second("Lv. " + Level.level(q.exp));
            chip.setSelected(q == p);
            // D-80: 펫 칩은 그 펫을 고르기만 한다. 캐릭터 창은 홈 펫 카드, 장신구 칸, "{이름} 꾸미기"로 연다.
            chip.addActionListener(e -> app.pet.select(kind));
            chipGroup.add(chip);
            chips.add(chip);
            Pill tab = new Pill(q.name, null, 13).small(q.kindName()).picture(Pet.face(q)).second("Lv. " + Level.level(q.exp));
            tab.setSelected(q == p);
            tab.addActionListener(e -> app.pet.select(kind));
            tabGroup.add(tab);
            petTabs.add(tab);
        }
        if (s.pets.size() < 2) {
            chips.add(adopt);
        }
        chips.revalidate();
        petTabs.revalidate();
        name.setText(p.name);
        kindTag.setText(p.kindName());
        dressButton.setText(p.name + " 꾸미기");
        level.setText("Lv. " + Level.level(p.exp));
        expText.setText(Level.expText(p.exp));
        expBar.set(Level.expRatio(p.exp));
        slots.removeAll();
        for (String slotName : Wardrobe.SLOTS) {
            slots.add(slot(slotName, p.wear.get(slotName)));
        }
        slots.revalidate();
        long done = s.book.checkables(s.today()).stream().filter(t -> t.done).count();
        stateText.setText("<html>오늘 끝낸 일 <b>" + done + "개</b><br>친밀도 <b>" + Bond.percent(s.book.rewardHistory(), p.kind)
                + "%</b><br>함께한 날 <b>" + Bond.days(s.book.rewardHistory(), p.kind) + "일</b></html>");
        infoText.setText("<html>" + p.kindName() + " <b>" + Ui.esc(p.name) + "</b><br>자세 " + p.pose + "</html>");
        if (s.pets.size() > petCount) {
            said = s.talk;
        }
        petCount = s.pets.size();
        if (said != null) {
            room.bubble = said;
            room.bubbleKind = null;
            said = null;
        }
        room.repaint();
        doLayout();
    }

    /** 검사용: 머리 카드, 펫 칩, 방 말풍선, 상점 카드 줄을 한 줄로. */
    public String summary() {
        StringBuilder b = new StringBuilder();
        b.append(pointStat.getText().replaceAll("<[^>]+>", "")).append(", ")
                .append(joyStat.getText().replaceAll("<[^>]+>", "")).append(", ")
                .append(placedStat.getText().replaceAll("<[^>]+>", "")).append(", ")
                .append(room.bubble.isEmpty() ? "하트" : room.bubble).append(", ").append(shop.cardsText());
        return b.toString();
    }
}
