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
        l.setForeground(Color.GRAY);
        return l;
    }

    public static JPanel flow(JComponent... parts) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        for (JComponent c : parts) {
            p.add(c);
        }
        return p;
    }

    /** 펫 말풍선. */
    public static JLabel bubble() {
        JLabel l = new JLabel();
        l.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(new Color(0x999999), 1, true),
                javax.swing.BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        return l;
    }

    public static LocalDate dateOf(JSpinner s) {
        return ((Date) s.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static void setDate(JSpinner s, LocalDate d) {
        s.setValue(Date.from(d.atStartOfDay(ZoneId.systemDefault()).toInstant()));
    }
}
