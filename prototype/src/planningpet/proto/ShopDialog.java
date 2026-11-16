package planningpet.proto;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/** 상점 창(05, UC-15). 보유 중이거나 포인트가 모자라면 사기 버튼이 비활성이다. */
public final class ShopDialog {
    private ShopDialog() {
    }

    /** onBuy는 아이템 이름을 받는다. 창 없이 그릴 때는 아무것도 하지 않는 값을 넘긴다. */
    public static JPanel content(Store s, java.util.function.Consumer<String> onBuy, JButton close) {
        JPanel root = new JPanel(new BorderLayout(0, 8));
        root.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        JPanel head = new JPanel(new BorderLayout());
        head.add(new JLabel("<html><b>상점</b></html>"), BorderLayout.WEST);
        head.add(new JLabel("<html>보유 <b>" + s.points + "P</b></html>"), BorderLayout.EAST);
        root.add(head, BorderLayout.NORTH);
        JPanel table = new JPanel(new GridLayout(0, 4, 12, 4));
        for (String title : new String[] {"아이템", "가격", "상태", ""}) {
            table.add(new JLabel("<html><b>" + title + "</b></html>"));
        }
        for (Map.Entry<String, Integer> e : Game.ITEMS.entrySet()) {
            boolean owned = s.owned.contains(e.getKey());
            int lack = e.getValue() - s.points;
            JLabel state = new JLabel(owned ? "보유 중" : lack > 0 ? lack + "P 모자라요" : "");
            state.setForeground(!owned && lack > 0 ? Theme.SUNDAY : Theme.INK);
            JButton buy = new JButton("사기");
            buy.setEnabled(!owned && lack <= 0);
            buy.addActionListener(x -> onBuy.accept(e.getKey()));
            table.add(new JLabel(e.getKey()));
            table.add(new JLabel(e.getValue() + "P"));
            table.add(state);
            table.add(buy);
        }
        root.add(table, BorderLayout.CENTER);
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(close, BorderLayout.EAST);
        root.add(bottom, BorderLayout.SOUTH);
        return root;
    }

    public static void open(App app, PetScreen pet) {
        JDialog d = new JDialog(SwingUtilities.getWindowAncestor(app.root), "상점", JDialog.ModalityType.APPLICATION_MODAL);
        JButton close = new JButton("닫기");
        close.addActionListener(e -> d.dispose());
        Runnable[] rebuild = new Runnable[1];
        rebuild[0] = () -> {
            d.setContentPane(content(app.store, item -> {
                if (app.game.buy(item) == 0) {
                    pet.say(item + " 고마워!");
                    app.store.changed();
                    rebuild[0].run();
                }
            }, close));
            d.pack();
        };
        rebuild[0].run();
        d.getRootPane().registerKeyboardAction(e -> d.dispose(),
                javax.swing.KeyStroke.getKeyStroke("ESCAPE"), javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW);
        d.setLocationRelativeTo(app.root);
        d.setVisible(true);
    }
}
