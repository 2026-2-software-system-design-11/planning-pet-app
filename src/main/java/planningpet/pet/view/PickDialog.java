package planningpet.pet.view;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

/**
 * 반려동물 고르기 창의 내용(05, UC-17). 첫 실행 때 메인 창보다 먼저 뜬다.
 * 모양은 2026-11-23 고르기 예시를 옮겼다(디자인 시스템 10절 "고르기 창"): 하늘 언덕 장면 위에 창의 줄들을
 * 가운데 한 줄로 둔다. 부품 종류, 글, 기본값은 05 그대로다.
 */
public final class PickDialog {
    public final JPanel panel = new Backdrop();

    public PickDialog() {
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(16, 0, 22, 0));
        // 창을 키우면 위아래 빈 곳만 늘고 내용은 가운데에 모여 있다.
        panel.add(Box.createVerticalGlue());
        panel.add(Box.createVerticalGlue());
    }

    /** 창 바탕(PickScene.backdrop). 폭은 800 이상이다. */
    private static final class Backdrop extends JPanel {
        Backdrop() {
            setOpaque(true);
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            return new Dimension(Math.max(800, d.width), d.height);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            PickScene.backdrop(g, getWidth(), getHeight());
            g.dispose();
        }
    }
}
