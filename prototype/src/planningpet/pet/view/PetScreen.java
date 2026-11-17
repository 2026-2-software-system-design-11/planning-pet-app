package planningpet.pet.view;

import planningpet.app.model.Store;
import planningpet.app.view.App;
import planningpet.common.model.Rules;
import planningpet.common.view.*;
import planningpet.shop.model.Shop;
import planningpet.shop.view.ShopDialog;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JToggleButton;

/** pet 화면(05 pet, UC-14, UC-16). */
public final class PetScreen extends JPanel {
    private final App app;
    private final JLabel name = new JLabel();
    private final JProgressBar expBar = new JProgressBar(0, 1000);
    private final JLabel expText = new JLabel();
    public final RoomView room;
    public final JPanel items = new JPanel();
    private String said;
    private boolean wasVisible;
    public final JLabel decoText = new JLabel();
    /** D-45: 상점 버튼은 아이템 줄과 떨어진 오른쪽 끝이다. */
    final JButton shopButton = new JButton("상점");
    private final JProgressBar decoBar = new JProgressBar(0, Shop.ITEMS.size());

    public PetScreen(App app) {
        super(new BorderLayout(0, 8));
        this.app = app;
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 12));
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
    public void say(String line) {
        said = line;
    }

    private void refresh() {
        Store s = app.store;
        name.setText(s.petName + " Lv." + Rules.level(s.exp));
        name.setFont(Theme.font(23));
        expBar.setValue((int) (Rules.expRatio(s.exp) * 1000));
        expText.setText(Rules.expText(s.exp));
        long done = s.checkables(s.today()).stream().filter(t -> t.done).count();
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
