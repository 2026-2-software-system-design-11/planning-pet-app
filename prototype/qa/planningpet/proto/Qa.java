package planningpet.proto;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;

/** QA가 화면에서 읽는 값. 화면 부품의 글자를 그대로 읽는다. */
final class Qa extends QaDrive {
    Qa(String flow) {
        super(flow);
    }

    static String plain(String html) {
        return html == null ? "" : html.replaceAll("(?i)<br>", " ").replaceAll("<[^>]+>", "")
                .replace("&lt;", "<").replace("&gt;", ">").replace("&amp;", "&").replaceAll("\\s+", " ").strip();
    }

    int count() {
        return app.store.today().equals(app.store.rewardCountDay) ? app.store.rewardCount : 0;
    }

    String top() {
        return plain(app.streak.getText()) + ", " + plain(app.points.getText()) + ", " + app.attend.getText()
                + (app.attend.isEnabled() ? "" : "(비활성)");
    }

    String progress() {
        var today = app.store.checkables(app.store.today());
        return today.isEmpty() ? "비어 있음" : today.stream().filter(t -> t.done).count() + "/" + today.size();
    }

    String level() {
        return main.levelName.getText() + " " + main.levelText.getText();
    }

    /** 메인 목록을 줄마다 "10:00~11:00 [x] 제목 60분"처럼 읽는다. */
    String rows() {
        return rows(main.rows);
    }

    String schedRows() {
        return rows(sched.untimed);
    }

    private static String rows(Container list) {
        List<String> out = new ArrayList<>();
        for (Component c : list.getComponents()) {
            List<String> parts = new ArrayList<>();
            collect(c, parts);
            out.add(String.join(" ", parts));
        }
        return String.join(" / ", out);
    }

    private static void collect(Component c, List<String> parts) {
        if (c instanceof JCheckBox b) {
            parts.add(b.isSelected() ? "[x]" : "[ ]");
        } else if (c instanceof JLabel l && !plain(l.getText()).isEmpty()) {
            parts.add(plain(l.getText()));
        } else if (c instanceof Container k && !(c instanceof JButton)) {
            for (Component child : k.getComponents()) {
                collect(child, parts);
            }
        }
    }

    /** 달력 칸 글자와 제목 색. 끝내지 않은 일이 있으면 "(진함)", 모두 끝냈으면 "(흐림)". */
    String cell(int day) {
        for (Component c : main.calendar.grid.getComponents()) {
            if (c instanceof JLabel l && plain(l.getText()).matches(day + "( .*)?")) {
                String t = l.getText();
                return plain(t) + (t.contains("#28977F") ? "(진함)" : t.contains("#8A96A0") ? "(흐림)" : "");
            }
        }
        return "없음";
    }

    String schedBubble() {
        return plain(sched.bubble.getText());
    }
}
