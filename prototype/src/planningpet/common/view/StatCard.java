package planningpet.common.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** 화면 머리 오른쪽의 수치 카드(D-47): 아이콘, 위 작은 이름과 아래 굵은 값, 오른쪽 버튼(선택). */
public final class StatCard {
    private StatCard() {
    }

    /** 화면 머리 오른쪽의 수치 카드 하나. 나의 방처럼 화면이 따로 쓸 때도 이것을 쓴다. */
    public static Card of(Icons.Kind icon, Value stat, javax.swing.JComponent action) {
        Card c = new Card(new BorderLayout(10, 0), Theme.CARD_RADIUS);
        c.setBorder(BorderFactory.createEmptyBorder(8, 14, 12, 16));
        JLabel mark = new JLabel(Icons.icon(icon, 30, icon == Icons.Kind.COIN ? Theme.COIN : Theme.ICON));
        c.add(mark, BorderLayout.WEST);
        c.add(stat, BorderLayout.CENTER);
        if (action != null) {
            JPanel a = new JPanel(new java.awt.GridBagLayout());
            a.add(action);
            c.add(a, BorderLayout.EAST);
        }
        return c;
    }

    /** 통계 카드의 글자: 위 작은 이름과 아래 굵은 값. 글자(getText)는 "연속 접속 5일"처럼 한 줄이다. */
    public static final class Value extends JLabel {
        private String caption = "";
        private String value = "";

        public void set(String caption, String value) {
            this.caption = caption;
            this.value = value;
            setText("<html>" + caption + " <b>" + value + "</b></html>");
            revalidate();
            repaint();
        }

        @Override
        public Dimension getPreferredSize() {
            java.awt.FontMetrics c = getFontMetrics(Theme.bold(11));
            java.awt.FontMetrics v = getFontMetrics(Theme.heavy(20));
            return new Dimension(Math.max(c.stringWidth(caption), v.stringWidth(value)) + 2, 42);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setFont(Theme.bold(11));
            g.setColor(Theme.INK_SUB);
            int top = (getHeight() - 42) / 2;
            g.drawString(caption, 0, top + 13);
            g.setFont(Theme.heavy(20));
            g.setColor(Theme.INK);
            g.drawString(value, 0, top + 38);
            g.dispose();
        }
    }
}
