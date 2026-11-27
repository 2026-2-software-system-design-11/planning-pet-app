package planningpet.pet.view;

import planningpet.common.view.Icons;
import planningpet.common.view.Theme;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * 펫 데려오기 창(05 "펫 데려오기 창", UC-20, D-48). 나의 방의 "+"로 연다.
 * 모양은 2026-11-23 상점 예시의 "캐릭터 해금" 창을 옮겼다(디자인 시스템 10절 "펫 데려오기 창"): 위에 발자국과 제목의 하늘빛 머리 띠,
 * 흰 본문 가운데 큰 펫과 강조선, 굵은 물음, 예시의 가격 알약 자리에 이름 줄, 아래에 같은 높이의 취소와 데려오기 큰 알약.
 * 예시의 가격(1,000P)은 명세에 없어 넣지 않는다. 창은 05대로 `JOptionPane`이고 닫기와 Esc는 운영체제 창 틀과 `JOptionPane`이 맡는다.
 */
public final class AdoptDialog {
    /** 창 안 폭. 예시 창(470)을 앱 글자 크기에 맞춰 줄이되, 글자와 펫이 창에서 차지하는 몫은 예시와 비슷하게 둔다. */
    static final int W = 356;
    private static final int HEAD = 52;
    private static final int PET_BOX = 156;
    /** 본문 아래 색: 흰색에 `DAY`를 0.75 섞은 아주 옅은 하늘빛. */
    private static final java.awt.Color BOTTOM = PickScene.mix(Theme.CARD_TOP, Theme.DAY, 0.75);

    private AdoptDialog() {
    }

    private static JPanel center(JComponent c) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        p.setOpaque(false);
        p.add(c);
        p.setAlignmentX(0.5f);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, c.getPreferredSize().height));
        return p;
    }

    /** 창 바탕: 흰색에서 아래로 아주 옅은 하늘빛(예시 본문 `#FCFCFC`에서 `#F5FAFE`). */
    private static final class Backdrop extends JPanel {
        @Override
        public Dimension getPreferredSize() {
            return new Dimension(W, super.getPreferredSize().height);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setPaint(new GradientPaint(0, HEAD, Theme.CARD_TOP, 0, getHeight(), BOTTOM));
            g.fillRect(0, 0, getWidth(), getHeight());
            g.dispose();
        }
    }

    /** 머리 띠: 하늘빛 바탕(`COOL_BOTTOM`, 예시 `#DDF1FE`), 발자국 24와 아주 굵은 19 제목, 아래 `CARD_EDGE` 선. 닫기는 운영체제 창 틀이 맡아 그리지 않는다. */
    private static final class Head extends JLabel {
        Head(String title) {
            super(title, Icons.icon(Icons.Kind.PAW, 24, Theme.ICON), SwingConstants.LEFT);
            setFont(Theme.heavy(19));
            setForeground(Theme.INK);
            setIconTextGap(10);
            setBorder(BorderFactory.createEmptyBorder(0, 20, 1, 20));
            setPreferredSize(new Dimension(W, HEAD));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setColor(Theme.COOL_BOTTOM);
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(Theme.CARD_EDGE);
            g.fillRect(0, getHeight() - 1, getWidth(), 1);
            g.dispose();
            PickScene.hints((Graphics2D) graphics);
            super.paintComponent(graphics);
        }
    }

}
