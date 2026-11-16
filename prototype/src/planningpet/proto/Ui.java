package planningpet.proto;

import java.awt.Color;
import java.awt.FlowLayout;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;

/** 화면 여러 곳이 같이 쓰는 작은 부품. */
public final class Ui {
    private Ui() {
    }

    public static JLabel hint(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(Theme.INK_DIM);
        l.setFont(Theme.font(12));
        return l;
    }

    public static JPanel flow(JComponent... parts) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        for (JComponent c : parts) {
            p.add(c);
        }
        return p;
    }

    /** HTML 라벨에 넣을 글자. 제목의 꺾쇠와 앰퍼샌드가 태그로 읽히지 않게 한다. */
    public static String esc(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    /** 칸에 max자를 넘게 써지지 않게 한다(붙여 넣으면 max자에서 자른다). */
    public static void limit(javax.swing.text.JTextComponent c, int max) {
        ((javax.swing.text.AbstractDocument) c.getDocument()).setDocumentFilter(new javax.swing.text.DocumentFilter() {
            @Override
            public void replace(FilterBypass fb, int off, int len, String text, javax.swing.text.AttributeSet a)
                    throws javax.swing.text.BadLocationException {
                int room = max - fb.getDocument().getLength() + len;
                String t = text == null ? "" : text;
                super.replace(fb, off, len, t.length() > room ? t.substring(0, Math.max(0, room)) : t, a);
            }
        });
    }

    /** 펫 말풍선. */
    public static JLabel bubble() {
        JLabel l = new JLabel() {
            @Override
            protected void paintComponent(java.awt.Graphics graphics) {
                java.awt.Graphics2D g = (java.awt.Graphics2D) graphics.create();
                g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(Theme.CARD_TOP);
                g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, Theme.BUBBLE_RADIUS, Theme.BUBBLE_RADIUS);
                g.setColor(Theme.CARD_LINE);
                g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, Theme.BUBBLE_RADIUS, Theme.BUBBLE_RADIUS);
                g.dispose();
                super.paintComponent(graphics);
            }
        };
        l.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 14, 8, 14));
        return l;
    }

    public static LocalDate dateOf(JSpinner s) {
        return ((Date) s.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static void setDate(JSpinner s, LocalDate d) {
        s.setValue(Date.from(d.atStartOfDay(ZoneId.systemDefault()).toInstant()));
    }
}
