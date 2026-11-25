package planningpet.common.view;

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
    /** 창이 뜨면 이 칸에 포커스를 준다. JOptionPane은 처음 값 버튼에 포커스를 주어서, 이름 칸에 바로 쓸 수 없었다. */
    public static void focusWhenShown(JComponent c) {
        c.addAncestorListener(new javax.swing.event.AncestorListener() {
            @Override
            public void ancestorAdded(javax.swing.event.AncestorEvent e) {
                javax.swing.SwingUtilities.invokeLater(c::requestFocusInWindow);
            }

            @Override
            public void ancestorRemoved(javax.swing.event.AncestorEvent e) {
            }

            @Override
            public void ancestorMoved(javax.swing.event.AncestorEvent e) {
            }
        });
    }

    private Ui() {
    }

    /** 걸리는 시간 칸(D-38). 10분부터 720분이고 범위 밖이나 숫자가 아니면 이전 값으로 돌아간다(05). */
    public static javax.swing.JSpinner minutes(int value) {
        return new javax.swing.JSpinner(new Minutes(value));
    }

    /** 걸리는 시간 칸의 값. 5분 단위가 아닌 값은 가까운 5분 값으로 맞추고 rounded에 알린다(D-95). */
    public static final class Minutes extends javax.swing.SpinnerNumberModel {
        /** 맞춘 값을 받는다. 칸 값이 바뀐 뒤에 부른다. */
        public java.util.function.IntConsumer rounded = m -> { };

        Minutes(int value) {
            super(value, 10, 720, 5);
        }

        @Override
        public void setValue(Object v) {
            if (v instanceof Number n && n.intValue() % 5 != 0) {
                int m = Math.round(n.intValue() / 5f) * 5;
                super.setValue(m);
                rounded.accept(m);
                return;
            }
            super.setValue(v);
        }
    }

    /**
     * D-43: 날짜 칸. 화살표가 커서 자리(처음에는 연도) 대신 늘 하루씩 바꾸도록 DateEditor가 아닌 기본 편집기에
     * 날짜 형식을 붙인다. SpinnerDateModel의 기본 단위가 하루다.
     */
    public static void dayEditor(javax.swing.JSpinner s, String pattern) {
        javax.swing.JSpinner.DefaultEditor editor = new javax.swing.JSpinner.DefaultEditor(s);
        javax.swing.JFormattedTextField field = editor.getTextField();
        field.setEditable(true);
        field.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(
                new javax.swing.text.DateFormatter(new java.text.SimpleDateFormat(pattern))));
        s.setEditor(editor);
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

    /** 포인트 얻는 법(D-33). pet 화면과 상점 창이 같이 쓴다. */
    public static final String POINT_GUIDE = "포인트 얻는 법: 할 일과 고정 일정 끝내기 +10P(하루 20번까지), 출석 +20P. 끝내면 경험치도 10 올라요";

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
