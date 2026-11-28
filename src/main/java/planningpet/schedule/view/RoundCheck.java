package planningpet.schedule.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import planningpet.common.view.Theme;

/**
 * 할 일 줄의 둥근 체크(05 입력 항목 4, D-59). 안 끝냄은 흰 바탕에 CARD_LINE 테두리, 끝냄은 민트 채움(MINT_TOP에서 MINT_DEEP)에
 * 흰 체크다. 마우스를 올리면 테두리가 MINT, 키보드 초점은 바깥 민트 고리다(docs/porting/task-rows.md S3r).
 */
final class RoundCheck {
    static final int SIZE = 20;

    private RoundCheck() {
    }

    /** 기본 체크박스에 둥근 체크 그림을 건다. 동작(선택, 동작 알림)은 JCheckBox 그대로다. */
    static JCheckBox style(JCheckBox box) {
        box.setIcon(new Mark());
        box.setOpaque(false);
        box.setFocusPainted(false);
        box.setRolloverEnabled(true);
        box.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        box.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return box;
    }

    private static final class Mark implements Icon {
        @Override
        public void paintIcon(Component c, Graphics graphics, int x, int y) {
            AbstractButton b = (AbstractButton) c;
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.translate(x, y);
            int s = SIZE - 1;
            boolean over = b.getModel().isRollover();
            if (b.isSelected()) {
                g.setPaint(new GradientPaint(0, 0, Theme.MINT_TOP, 0, s, Theme.MINT_DEEP));
                g.fillRoundRect(0, 0, s, s, 8, 8);
                g.setColor(Color.WHITE);
                g.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                Path2D p = new Path2D.Double();
                p.moveTo(s * 0.27, s * 0.52);
                p.lineTo(s * 0.44, s * 0.69);
                p.lineTo(s * 0.74, s * 0.34);
                g.draw(p);
            } else {
                g.setColor(over ? Theme.MINT_PALE : Theme.CARD_TOP);
                g.fillRoundRect(0, 0, s, s, 8, 8);
                g.setColor(over ? Theme.MINT : Theme.CARD_LINE);
                g.setStroke(new BasicStroke(1.5f));
                g.drawRoundRect(1, 1, s - 2, s - 2, 7, 7);
            }
            if (b.isFocusOwner()) {
                g.setColor(new Color(0x1F, 0xB9, 0x9C, 110));
                g.setStroke(new BasicStroke(1.5f));
                g.drawRoundRect(-2, -2, s + 4, s + 4, 11, 11);
            }
            g.dispose();
        }

        @Override
        public int getIconWidth() {
            return SIZE;
        }

        @Override
        public int getIconHeight() {
            return SIZE;
        }
    }
}
