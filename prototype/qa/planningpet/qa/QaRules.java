package planningpet.qa;

import planningpet.app.*;
import planningpet.app.model.*;
import planningpet.app.view.App;
import planningpet.common.view.Card;
import planningpet.pet.view.PickDialog;
import planningpet.schedule.model.*;
import planningpet.schedule.view.*;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.regex.Pattern;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 * 사용성 자동 규칙(#79). 명세가 아니라 화면이 지켜야 할 규칙을 본다. 어긋남은 보고서에 적고 qa.sh를 실패시키지 않는다.
 * 1 입력 보존, 2 Enter와 옆 버튼, 3 용어, 4 같은 이름 버튼의 자리.
 */
final class QaRules {
    private final List<String[]> found = new ArrayList<>();
    private final List<String> glossary = new ArrayList<>();
    /** 규칙 4에서 까닭이 있어 받아들인 것(qa/places-ok.tsv). */
    private final Map<String, String> allowed = new java.util.HashMap<>();
    private final List<String[]> accepted = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        QaRules r = new QaRules();
        r.inputs();
        Map<String, JComponent> screens = r.screens();
        r.terms(screens, new File(args[1]));
        File ok = new File(new File(args[1]).getParentFile(), "places-ok.tsv");
        for (String line : Files.readAllLines(ok.toPath(), StandardCharsets.UTF_8)) {
            if (!line.isBlank() && !line.startsWith("#")) {
                String[] p = line.split("\t");
                r.allowed.put(p[0], p[1]);
            }
        }
        r.places(screens);
        r.write(new File(args[0]));
        System.out.println("규칙 어긋남 " + r.found.size() + "개");
        System.exit(0);
    }

    /** 앱 창 네 화면과 창 열 가지. 창은 버튼에 동작이 붙기 전의 내용 판이다. */
    private Map<String, JComponent> screens() {
        Map<String, JComponent> m = new LinkedHashMap<>();
        for (String name : new String[] {"홈", "일정관리", "캐릭터", "나의 방", "통계", "설정"}) {
            App app = fresh();
            app.show(name);
            app.store.changed();
            lay(app.root, new Dimension(1000, 680));
            m.put(name, app.root);
        }
        App app = fresh();
        LocalDate today = app.store.today();
        Task essay = app.store.book.dayPlan(today).timed().get(1);
        LocalDate monday = TaskBook.mondayOf(today);
        m.put("일정 추가 창", TaskDialog.content(new TaskForm(null, today, app.store.now), new JButton("취소"), new JButton("저장"), null));
        m.put("일정 수정 창", TaskDialog.content(new TaskForm(essay, today, app.store.now), new JButton("취소"), new JButton("저장"),
                new JButton("삭제")));
        m.put("시각 고치기 창", new TimeEditor(essay, app.store.now).panel);
        m.put("자동 배치 결과 창", AutoPlaceDialog.content(app.store, monday, Planner.place(app.store, monday), new JButton("취소"),
                new JButton("적용")));
        m.put("반려동물 고르기 창", new PickDialog().panel);
        for (JComponent c : m.values()) {
            if (c.getWidth() == 0) {
                lay(c, c.getPreferredSize());
            }
        }
        return m;
    }

    private static App fresh() {
        App app = Main.create(MockData.sample());
        app.control.windowShown();
        app.show("홈");
        app.store.changed();
        return app;
    }

    /** 규칙 1, 2: 앱 창의 글 칸마다 같은 묶음의 버튼을 누르고, Enter와 결과를 견준다. */
    private void inputs() {
        App probe = fresh();
        List<JTextField> fields = all(probe.root, JTextField.class).stream()
                .filter(f -> f.isEditable() && SwingUtilities.getAncestorOfClass(JSpinner.class, f) == null).toList();
        for (int i = 0; i < fields.size(); i++) {
            int fi = i;
            String enter = effect(app -> {
                JTextField f = editable(app).get(fi);
                f.setText("규칙 검사");
                f.postActionEvent();
                return f;
            });
            List<JButton> near = all(fields.get(i).getParent(), JButton.class);
            for (int b = 0; b < near.size(); b++) {
                int bi = b;
                String label = near.get(b).getText();
                String click = effect(app -> {
                    JTextField f = editable(app).get(fi);
                    f.setText("규칙 검사");
                    all(f.getParent(), JButton.class).get(bi).doClick();
                    return f;
                });
                if (click.contains("글 남음") && !click.contains("할 일 +")) {
                    found.add(new String[] {"1 입력 보존", "홈 글 칸 옆 \"" + label + "\"", "글을 쓰고 누르면 " + click
                            + ". 쓴 글이 쓰이지도 지워지지도 않는다"});
                }
                if (!click.equals(enter)) {
                    found.add(new String[] {"2 Enter와 옆 버튼", "홈 글 칸과 \"" + label + "\"", "Enter: " + enter + ". 버튼: " + click});
                }
            }
        }
    }

    private static List<JTextField> editable(App app) {
        return all(app.root, JTextField.class).stream()
                .filter(f -> f.isEditable() && SwingUtilities.getAncestorOfClass(JSpinner.class, f) == null).toList();
    }

    /** 새 앱에서 동작을 하고, 할 일 수와 칸 글과 창이 떴는지를 한 줄로 돌려준다. */
    private static String effect(Function<App, JTextField> act) {
        App app = fresh();
        int before = app.store.tasks.size();
        String window = "";
        JTextField f = null;
        try {
            f = act.apply(app);
        } catch (java.awt.HeadlessException e) {
            window = ", 창을 연다";
            f = editable(app).get(0);
        }
        int delta = app.store.tasks.size() - before;
        String text = f.getText().isEmpty() ? "글 비움" : "글 남음";
        return (delta != 0 ? "할 일 +" + delta : "할 일 그대로") + ", " + text + window;
    }

    /** 규칙 3: 화면 글자를 모으고, qa/terms.tsv에 적은 같은 뜻 묶음에서 둘 이상의 말이 보이면 어긋남이다. */
    private void terms(Map<String, JComponent> screens, File termsFile) throws Exception {
        Map<String, List<String>> where = new TreeMap<>();
        screens.forEach((name, root) -> {
            for (Component c : all(root, JComponent.class)) {
                String t = c instanceof JLabel l ? Qa.plain(l.getText()) : c instanceof AbstractButton b ? b.getText() : null;
                if (t != null && !t.isBlank() && shown(c, root)) {
                    where.computeIfAbsent(t.strip(), k -> new ArrayList<>()).add(name);
                }
            }
        });
        // 용어집에는 화면이 정한 말만 둔다. 숫자로 시작하는 글(날짜, 시각, 포인트)과 일정 제목이 든 글은 데이터라 뺀다.
        java.util.Set<String> titles = new java.util.HashSet<>();
        MockData.sample().tasks.forEach(x -> titles.add(x.title));
        where.keySet().removeIf(t -> t.matches("^[0-9<>~].*") || titles.stream().anyMatch(t::contains));
        where.forEach((t, s) -> glossary.add("| " + t.replace("|", "/") + " | " + String.join(", ", s.stream().distinct().toList()) + " |"));
        for (String line : Files.readAllLines(termsFile.toPath(), StandardCharsets.UTF_8)) {
            if (line.isBlank() || line.startsWith("#")) {
                continue;
            }
            String[] p = line.split("\t");
            List<String> seen = new ArrayList<>();
            for (String variant : p[1].split("\\|")) {
                Pattern rx = Pattern.compile(Pattern.quote(variant));
                where.forEach((t, s) -> {
                    if (rx.matcher(t).find()) {
                        seen.add("\"" + variant + "\"(" + String.join(", ", s.stream().distinct().toList()) + ")");
                    }
                });
            }
            List<String> distinct = seen.stream().distinct().toList();
            if (distinct.stream().map(x -> x.substring(0, x.indexOf('('))).distinct().count() > 1) {
                found.add(new String[] {"3 용어", p[0], String.join(", ", distinct)});
            }
        }
    }

    /** 규칙 4: 같은 글의 버튼이 화면마다 같은 자리(묶음 판 안의 위, 아래, 왼쪽, 오른쪽)에 있는지 본다. */
    private void places(Map<String, JComponent> screens) {
        Map<String, List<String>> at = new TreeMap<>();
        screens.forEach((name, root) -> {
            for (JButton b : all(root, JButton.class)) {
                if (b.getText().isBlank() || !shown(b, root)) {
                    continue;
                }
                // 목록 줄 안의 버튼은 그 줄 안의 자리, 나머지는 묶음 판(Card) 안의 자리를 본다.
                Container rows = (Container) SwingUtilities.getAncestorOfClass(TaskRows.class, b);
                Container box = (Container) SwingUtilities.getAncestorOfClass(Card.class, b);
                Container frame = rows != null ? rowOf(rows, b) : box != null ? box : root;
                Point p = SwingUtilities.convertPoint(b, b.getWidth() / 2, b.getHeight() / 2, frame);
                String v = p.y < frame.getHeight() / 3 ? "위" : p.y > frame.getHeight() * 2 / 3 ? "아래" : "가운데";
                String h = p.x < frame.getWidth() / 3 ? "왼쪽" : p.x > frame.getWidth() * 2 / 3 ? "오른쪽" : "가운데";
                at.computeIfAbsent(b.getText(), k -> new ArrayList<>()).add(name + "|" + v + " " + h);
            }
        });
        at.forEach((text, places) -> {
            List<String> spots = places.stream().map(s -> s.substring(s.indexOf('|') + 1)).distinct().toList();
            List<String> names = places.stream().map(s -> s.substring(0, s.indexOf('|'))).distinct().toList();
            if (names.size() > 1 && spots.size() > 1) {
                String where = String.join(", ", places.stream().distinct().map(s -> s.replace('|', ' ')).toList());
                String why = allowed.get(text);
                if (why == null) {
                    found.add(new String[] {"4 같은 이름 버튼의 자리", "\"" + text + "\"", where});
                } else {
                    accepted.add(new String[] {"\"" + text + "\"", where, why});
                }
            }
        });
    }

    private void write(File out) throws Exception {
        List<String> lines = new ArrayList<>();
        lines.add("# 사용성 자동 규칙");
        lines.add("");
        lines.add("`prototype/qa.sh`가 `QaRules`로 만든다. 손으로 고치지 않는다. 규칙과 까닭은 [blind/README.md](blind/README.md) 7절에 있다.");
        lines.add("");
        lines.add("## 어긋남");
        lines.add("");
        lines.add("| 규칙 | 어디 | 무엇 |");
        lines.add("|---|---|---|");
        found.forEach(f -> lines.add("| " + f[0] + " | " + f[1] + " | " + f[2] + " |"));
        lines.add("");
        lines.add("## 받아들인 자리 차이");
        lines.add("");
        lines.add("규칙 4에서 까닭이 있어 받아들인 것이다(`prototype/qa/places-ok.tsv`).");
        lines.add("");
        lines.add("| 버튼 | 자리 | 까닭 |");
        lines.add("|---|---|---|");
        accepted.forEach(a -> lines.add("| " + a[0] + " | " + a[1] + " | " + a[2] + " |"));
        lines.add("");
        lines.add("## 화면 글자(용어집)");
        lines.add("");
        lines.add("| 글자 | 화면 |");
        lines.add("|---|---|");
        lines.addAll(glossary);
        Files.write(out.toPath(), lines, StandardCharsets.UTF_8);
    }

    /** 그 부품과 위의 모든 판이 보이는가. 카드 레이아웃이 숨긴 화면의 부품은 빼야 한다. */
    private static boolean shown(Component c, Component root) {
        for (Component x = c; x != null && x != root; x = x.getParent()) {
            if (!x.isVisible()) {
                return false;
            }
        }
        return true;
    }

    private static Container rowOf(Container rows, Component b) {
        Component x = b;
        while (x.getParent() != rows) {
            x = x.getParent();
        }
        return (Container) x;
    }

    private static void lay(Component c, Dimension size) {
        c.setSize(size);
        for (int i = 0; i < 2; i++) {
            for (Component x : all(c, Component.class)) {
                x.doLayout();
            }
        }
    }

    static <T> List<T> all(Component root, Class<T> type) {
        List<T> out = new ArrayList<>();
        if (type.isInstance(root)) {
            out.add(type.cast(root));
        }
        if (root instanceof Container k) {
            for (Component c : k.getComponents()) {
                out.addAll(all(c, type));
            }
        }
        return out;
    }
}
