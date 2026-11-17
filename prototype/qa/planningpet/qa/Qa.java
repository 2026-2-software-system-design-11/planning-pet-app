package planningpet.qa;

import planningpet.common.view.RoundButtonUI;
import planningpet.schedule.model.Planner;
import planningpet.schedule.view.AutoPlaceDialog;
import planningpet.shop.view.ShopDialog;
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

    /** 이어지는 흐름: 앞 흐름의 단계를 "앞" 표시로 먼저 돈다. */
    Qa after(java.util.function.UnaryOperator<Qa> earlier) {
        depth++;
        earlier.apply(this);
        depth--;
        return this;
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

    /** 자동 배치 결과 창의 위쪽 글과 표를 "제목|날짜|시각|분"으로 읽는다. */
    String placement(Planner.Placement p) {
        JPanel root = AutoPlaceDialog.content(app.store, sched.monday(), p, new JButton("취소"), new JButton("적용"));
        JLabel head = find(root, JLabel.class);
        JTable table = find(root, JTable.class);
        List<String> out = new ArrayList<>();
        for (int r = 0; r < table.getRowCount(); r++) {
            List<String> cols = new ArrayList<>();
            for (int k = 0; k < table.getColumnCount(); k++) {
                cols.add(String.valueOf(table.getValueAt(r, k)));
            }
            out.add(String.join("|", cols));
        }
        return head.getText() + " / " + String.join(" / ", out);
    }

    /** 상점 표를 "이름 상태 사기(켜짐, 꺼짐, 버튼 없음)"으로 읽는다. */
    String shop() {
        JPanel root = ShopDialog.content(app.store, x -> { }, new JButton("닫기"));
        List<String> out = new ArrayList<>();
        Container grid = findGrid(root);
        Component[] cells = grid.getComponents();
        for (int i = 4; i + 3 < cells.length; i += 4) {
            String state = plain(((JLabel) cells[i + 2]).getText());
            out.add(plain(((JLabel) cells[i]).getText()) + " " + (state.isEmpty() ? "-" : state)
                    + (cells[i + 3] instanceof JButton b ? (b.isEnabled() ? " 켜짐" : " 꺼짐") : " 버튼 없음"));
        }
        return String.join(" / ", out);
    }

    /** pet 화면: 꾸미기 진행도, 방 말풍선, 내 아이템(흐린 것은 "(흐림)"). */
    String pet() {
        List<String> items = new ArrayList<>();
        for (Component c : pet.items.getComponents()) {
            if (c instanceof javax.swing.AbstractButton b) {
                items.add(b.getText() + (Boolean.TRUE.equals(b.getClientProperty(RoundButtonUI.DIM)) ? "(흐림)" : ""));
            }
        }
        return pet.decoText.getText() + ", " + pet.room.bubble + ", " + String.join(" ", items);
    }

    private static Container findGrid(Container c) {
        if (c instanceof JPanel p && p.getLayout() instanceof GridLayout g && g.getColumns() == 4) {
            return c;
        }
        for (Component child : c.getComponents()) {
            if (child instanceof Container k && findGrid(k) != null) {
                return findGrid(k);
            }
        }
        return null;
    }
}
