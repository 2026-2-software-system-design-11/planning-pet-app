package planningpet.proto;

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
    private final RoomView room;
    private final JPanel items = new JPanel();
    private String said;

    public PetScreen(App app) {
        super(new BorderLayout(0, 8));
        this.app = app;
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 12));
        room = new RoomView(app.store);
        room.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        JPanel top = new JPanel(new BorderLayout(12, 0));
        top.add(name, BorderLayout.WEST);
        top.add(expBar, BorderLayout.CENTER);
        top.add(expText, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);
        add(room, BorderLayout.CENTER);
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(Ui.flow(new JLabel("<html><b>내 아이템</b></html>"), Ui.hint("누르면 방에 놓거나 치워요")),
                BorderLayout.NORTH);
        bottom.add(items, BorderLayout.CENTER);
        bottom.setPreferredSize(new Dimension(0, 80));
        add(bottom, BorderLayout.SOUTH);
        app.store.onChange(this::refresh);
    }

    /** 상점에서 산 뒤처럼 한 번만 다른 말을 할 때. */
    public void say(String line) {
        said = line;
    }

    private void refresh() {
        Store s = app.store;
        name.setText("<html><b>" + s.petName + "</b> Lv." + Rules.level(s.exp) + "</html>");
        expBar.setValue((int) (Rules.expRatio(s.exp) * 1000));
        expText.setText(Rules.expText(s.exp));
        long done = s.forDate(s.today()).stream().filter(t -> t.done).count();
        room.bubble = said != null ? said : done > 0 ? "오늘 " + done + "개 끝냈어." : "오늘은 아직 끝낸 일이 없어.";
        said = null;
        items.removeAll();
        items.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 6, 4));
        for (String item : Game.ITEMS.keySet()) {
            if (!s.owned.contains(item)) {
                continue;
            }
            boolean placed = s.placed.contains(item);
            JToggleButton b = new JToggleButton(placed ? item + " ✓" : item, placed);
            b.setForeground(placed ? Color.DARK_GRAY : Color.GRAY);
            b.addActionListener(e -> {
                if (!s.placed.remove(item)) {
                    s.placed.add(item);
                }
                s.changed();
            });
            items.add(b);
        }
        JButton shop = new JButton("<html><b>상점</b></html>");
        shop.addActionListener(e -> ShopDialog.open(app, this));
        items.add(shop);
        items.revalidate();
        room.repaint();
    }
}
