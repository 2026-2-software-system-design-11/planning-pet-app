package planningpet.pet.view;

import planningpet.app.view.App;
import planningpet.common.view.Theme;
import java.awt.BorderLayout;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Window;
import java.awt.event.KeyEvent;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

/**
 * 캐릭터 창(D-58). 메뉴가 아니라 나의 방의 펫 칩, 장신구 칸, "캐릭터에서 꾸미기"로 여는 모달 창이고
 * 캐릭터 화면(CharacterScreen) 하나를 담는다. 운영체제 창 틀의 닫기나 Esc로 닫고, 입혀 본 장신구가 있으면 먼저 묻는다(D-61).
 */
public final class CharacterDialog {
    private CharacterDialog() {
    }

    /** 창 안: 본문 판과 같은 그라데이션 바탕에 여백 16. 창 없이 그릴 때도 쓴다. */
    public static JComponent content(CharacterScreen screen) {
        JPanel p = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setPaint(new GradientPaint(0, 0, Theme.BG_TOP, 0, getHeight(), Theme.BG_BOTTOM));
                g.fillRect(0, 0, getWidth(), getHeight());
                g.dispose();
            }
        };
        p.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        p.add(screen);
        return p;
    }

    /** 창을 연다. dress면 꾸미기 탭으로 연다. 입혀 보던 장신구는 저장된 것에서 다시 시작한다. */
    public static void open(App app, CharacterScreen screen, boolean dress) {
        screen.opened(dress);
        Window owner = SwingUtilities.getWindowAncestor(app.root);
        JDialog d = new JDialog(owner, "캐릭터", JDialog.ModalityType.APPLICATION_MODAL);
        d.setContentPane(content(screen));
        int w = owner == null ? 1000 : owner.getWidth();
        int h = owner == null ? 680 : owner.getHeight();
        d.setSize(Math.max(860, (int) (w * 0.88)), Math.max(560, (int) (h * 0.86)));
        // D-61: 닫기와 Esc는 입혀 본 장신구가 있으면 먼저 묻는다.
        d.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        Runnable close = () -> {
            if (screen.confirmLeave()) {
                d.dispose();
            }
        };
        d.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                close.run();
            }
        });
        d.getRootPane().registerKeyboardAction(e -> close.run(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        d.setLocationRelativeTo(owner);
        d.setVisible(true);
    }
}
