package planningpet.common.view;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicComboBoxUI;

/**
 * 고르는 칸(디자인 시스템 5절, D-97). 옆의 입력 칸과 같은 흰 바탕과 얇은 테두리이고, 칸 안 오른쪽 끝에
 * 바탕 없는 작은 아래 꺾쇠를 둔다. 칸 어디를 눌러도 목록이 열린다(BasicComboBoxUI 그대로).
 * Theme.install이 JComboBox의 기본 모양으로 건다.
 */
public final class ComboUI extends BasicComboBoxUI {
    /** UIManager가 JComboBox마다 부른다. */
    public static ComponentUI createUI(JComponent c) {
        return new ComboUI();
    }

    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        comboBox.setBorder(UIManager.getBorder("TextField.border"));
        comboBox.setBackground(Theme.CARD_TOP);
    }

    /** 바탕 없는 DOWN 12 꺾쇠. 누를 수 없으면 INK_DIM이다. */
    @Override
    protected JButton createArrowButton() {
        JButton b = new JButton(Icons.icon(Icons.Kind.DOWN, 12, Theme.ICON));
        b.setDisabledIcon(Icons.icon(Icons.Kind.DOWN, 12, Theme.INK_DIM));
        b.setUI(new BasicButtonUI());
        b.setBorder(BorderFactory.createEmptyBorder());
        b.setContentAreaFilled(false);
        b.setOpaque(false);
        b.setFocusable(false);
        b.setName("ComboBox.arrowButton");
        return b;
    }
}
