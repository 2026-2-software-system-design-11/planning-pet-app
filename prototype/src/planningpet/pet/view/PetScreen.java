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
        JPanel top = new JPanel(new BorderLayout(12, 0));
        top.add(name, BorderLayout.WEST);
        top.add(expBar, BorderLayout.CENTER);
        top.add(expText, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);
        Card roomCard = new Card(new BorderLayout());
        roomCard.add(room);
        add(roomCard, BorderLayout.CENTER);
        JPanel bottom = new Card(new BorderLayout());
        decoBar.setPreferredSize(new Dimension(90, 10));
        bottom.add(Ui.flow(new JLabel("<html><b>내 아이템</b></html>"), Ui.hint("누르면 방에 놓거나 치워요"), decoText, decoBar),
                BorderLayout.NORTH);
        JPanel row = new JPanel(new BorderLayout());
        row.add(items, BorderLayout.CENTER);
        shopButton.addActionListener(e -> ShopDialog.open(app, this));
        row.add(Ui.flow(shopButton), BorderLayout.EAST);
        bottom.add(row, BorderLayout.CENTER);
        bottom.add(Ui.hint(Ui.POINT_GUIDE), BorderLayout.SOUTH);
        bottom.setPreferredSize(new Dimension(0, 130));
        add(bottom, BorderLayout.SOUTH);
        app.watch(this::refresh);
    }

    /** 상점에서 산 뒤처럼 한 번만 다른 말을 할 때. */
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
