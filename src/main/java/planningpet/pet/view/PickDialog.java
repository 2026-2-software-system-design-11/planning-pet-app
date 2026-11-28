package planningpet.pet.view;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import planningpet.pet.model.PetKind;

/**
 * 반려동물 고르기 창의 내용(05, UC-17). 첫 실행 때 메인 창보다 먼저 뜬다.
 * 모양은 2026-11-23 고르기 예시를 옮겼다(디자인 시스템 10절 "고르기 창"): 하늘 언덕 장면 위에 창의 줄들을
 * 가운데 한 줄로 둔다. 부품 종류, 글, 기본값은 05 그대로다.
 */
public final class PickDialog {
    public final PickCard cat = new PickCard("고양이", PetKind.CAT, true, "조용하고 편안한 성격으로\n언제나 곁에서 응원해요!");
    public final PickCard dog = new PickCard("강아지", PetKind.DOG, false, "활발하고 다정한 성격으로\n항상 힘이 되어줘요!");
    public final JPanel panel = new Backdrop();

    public PickDialog() {
        ButtonGroup group = new ButtonGroup();
        group.add(cat);
        group.add(dog);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(16, 0, 22, 0));
        // 창을 키우면 위아래 빈 곳만 늘고 내용은 가운데에 모여 있다.
        panel.add(Box.createVerticalGlue());
        JPanel animals = clear(new FlowLayout(FlowLayout.CENTER, 20, 0));
        animals.add(cat);
        animals.add(dog);
        animals.setMaximumSize(new Dimension(Integer.MAX_VALUE, PickCard.H));
        panel.add(animals);
        panel.add(Box.createVerticalGlue());
    }

    private static JPanel clear(LayoutManager layout) {
        JPanel p = new JPanel(layout);
        p.setOpaque(false);
        p.setAlignmentX(0.5f);
        return p;
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
