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

    public void sayOnce(String line) {
        said = line;
    }

    private void refresh() {
        Store s = app.store;
        name.setText(s.pet().name + " Lv." + Level.level(s.pet().exp));
        name.setFont(Theme.font(23));
        expBar.setValue((int) (Level.expRatio(s.pet().exp) * 1000));
        expText.setText(Level.expText(s.pet().exp));
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
