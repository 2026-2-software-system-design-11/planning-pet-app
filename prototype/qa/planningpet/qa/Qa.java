package planningpet.qa;

import planningpet.common.view.RoundButtonUI;
import planningpet.schedule.model.Planner;
import planningpet.schedule.view.AutoPlaceDialog;
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
        var today = app.store.book.checkables(app.store.today());
        return today.isEmpty() ? "비어 있음" : today.stream().filter(t -> t.done).count() + "/" + today.size();
    }

    /** 홈 펫 카드의 이름, 레벨, 경험치(D-59): "나비 Lv. 3 경험치 80 / 110". */
    String level() {
        return main.pet.nameText() + " " + main.pet.levelText() + " " + main.pet.expText();
    }

    /** 홈 펫 카드의 오늘 끝낸 일과 막대: "오늘 끝낸 일 1 / 4, 25% 4칸"(06의 13번). */
    String petCard() {
        return main.pet.doneText() + ", " + main.pet.doneBarText();
    }

    /** 메인 목록을 줄마다 화면 순서대로 "[x] 제목 10:00~11:00 60분"처럼 읽는다(D-59). 시각 알약 버튼의 글도 읽고, 글 없는 아이콘 버튼은 건너뛴다. */
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
        } else if (c instanceof JButton b) {
            if (!b.getText().isBlank()) {
                parts.add(b.getText());
            }
        } else if (c instanceof Container k) {
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
        JPanel root = AutoPlaceDialog.content(app.store, sched.weekStart(), p, new JButton("취소"), new JButton("적용"));
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
        JPanel rest = findNamed(root, "놓지 못한 일");
        if (rest != null) {
            for (java.awt.Component c : rest.getComponents()) {
                out.add(((JLabel) c).getText().replaceAll("<br>", " ").replaceAll("<[^>]+>", "").strip());
            }
        }
        return head.getText() + " / " + String.join(" / ", out);
    }

    private static JPanel findNamed(java.awt.Component c, String name) {
        if (c instanceof JPanel p && name.equals(p.getName())) {
            return p;
        }
        if (c instanceof java.awt.Container k) {
            for (java.awt.Component child : k.getComponents()) {
                JPanel found = findNamed(child, name);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    /** 나의 방 상점: 아이템마다 카드를 눌러 미리보기의 값과 버튼을 읽는다. "쿠션 보유 중 방에서 치우기 켜짐", "소파 180P 모자라요 꺼짐". */
    String shop() {
        List<String> out = new ArrayList<>();
        var sp = pet.shop;
        for (String name : planningpet.shop.model.Shop.ITEMS.keySet()) {
            sp.selected = null;
            sp.select(name);
            String state = app.store.owned.contains(name) ? "보유 중 " + sp.action.getText()
                    : sp.lack.getText().isBlank() ? "살 수 있어요" : sp.lack.getText();
            out.add(name + " " + state + (sp.action.isEnabled() ? " 켜짐" : " 꺼짐"));
        }
        sp.select(null);
        return String.join(" / ", out);
    }

    /** 나의 방: 배치 아이템, 방 말풍선(글이 없으면 하트), 보유한 아이템 카드("쿠션 보유 중 ✓"는 방에 놓임). */
    String pet() {
        pet.shop.ownedMode.doClick();
        StringBuilder owned = new StringBuilder(pet.shop.cardsText());
        // 카드 줄이 넘치면 다음 쪽으로 넘기며 모두 읽는다.
        while (pet.shop.next.isEnabled()) {
            pet.shop.next.doClick();
            owned.append(owned.length() > 0 ? ", " : "").append(pet.shop.cardsText());
        }
        pet.shop.shopMode.doClick();
        return pet.placedStat.getText().replaceAll("<[^>]+>", "") + ", " + (pet.room.bubble.isEmpty() ? "하트" : pet.room.bubble)
                + ", " + owned;
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
