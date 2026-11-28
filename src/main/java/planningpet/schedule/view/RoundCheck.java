package planningpet.schedule.view;

import java.awt.BasicStroke;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import planningpet.common.view.Theme;

/** 할 일 줄의 둥근 체크(05 입력 항목 4, D-59). 안 끝냄은 흰 바탕에 CARD_LINE 테두리다(docs/porting/task-rows.md S3r). */
final class RoundCheck {
    static final int SIZE = 20;

    private RoundCheck() {
    }

    /** 기본 체크박스에 둥근 체크 그림을 건다. 동작(선택, 동작 알림)은 JCheckBox 그대로다. */
    static JCheckBox style(JCheckBox box) {
        box.setIcon(new Mark());
        box.setOpaque(false);
        box.setFocusPainted(false);
        box.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        box.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return box;
    }

    private static final class Mark implements Icon {
        @Override
        public void paintIcon(Component c, Graphics graphics, int x, int y) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.translate(x, y);
            int s = SIZE - 1;
            g.setColor(Theme.CARD_TOP);
            g.fillRoundRect(0, 0, s, s, 8, 8);
            g.setColor(Theme.CARD_LINE);
            g.setStroke(new BasicStroke(1.5f));
            g.drawRoundRect(1, 1, s - 2, s - 2, 7, 7);
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
