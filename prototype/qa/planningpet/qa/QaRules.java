package planningpet.qa;

import planningpet.app.*;
import planningpet.app.model.*;
import planningpet.app.view.App;
import planningpet.common.view.Card;
import planningpet.pet.view.CharacterDialog;
import planningpet.pet.view.PetScreen;
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
        r.clipped(screens);
        r.reacts();
        r.write(new File(args[0]));
        System.out.println("규칙 어긋남 " + r.found.size() + "개");
        System.exit(0);
    }

    /** 앱 창 네 화면과 창 열 가지. 창은 버튼에 동작이 붙기 전의 내용 판이다. */
    private Map<String, JComponent> screens() {
        Map<String, JComponent> m = new LinkedHashMap<>();
        for (String name : new String[] {"홈", "일정관리", "나의 방", "통계", "설정"}) {
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
        PetScreen room = QaDrive.find(app.root, PetScreen.class);
        room.character.opened(false);
        JComponent character = CharacterDialog.content(room.character);
        lay(character, new Dimension(880, 585));
        m.put("캐릭터 창", character);
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
                String t = c instanceof JLabel l ? Qa.plain(l.getText()) : c instanceof AbstractButton b ? label(b) : null;
                if (t != null && !t.isBlank() && shown(c, root)) {
                    where.computeIfAbsent(t.strip(), k -> new ArrayList<>()).add(name);
                }
                // 홈 펫 카드(D-59)는 글을 손수 그린다. 그린 글을 부품 글과 같이 센다.
                if (c instanceof planningpet.home.view.PetCard card && shown(c, root)) {
                    for (String w : new String[] {card.nameText(), card.levelText(), card.expText(), "오늘 끝낸 일"}) {
                        where.computeIfAbsent(w, k -> new ArrayList<>()).add(name);
                    }
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

    /** 버튼의 이름: 글이 있으면 글, 아이콘만 있으면 툴팁(사용자가 마우스를 올려 읽는 이름, 예: 연필 "수정"). */
    private static String label(AbstractButton b) {
        String t = b.getText();
        if (t != null && !t.isBlank()) {
            return t;
        }
        return b.getToolTipText() == null ? "" : b.getToolTipText();
    }

    /** 규칙 4: 같은 글의 버튼이 화면마다 같은 자리(묶음 판 안의 위, 아래, 왼쪽, 오른쪽)에 있는지 본다. */
    private void places(Map<String, JComponent> screens) {
        Map<String, List<String>> at = new TreeMap<>();
        screens.forEach((name, root) -> {
            for (JButton b : all(root, JButton.class)) {
                if (label(b).isBlank() || !shown(b, root)) {
                    continue;
                }
                // 목록 줄 안의 버튼은 그 줄 안의 자리, 나머지는 묶음 판(Card) 안의 자리를 본다.
                Container rows = (Container) SwingUtilities.getAncestorOfClass(TaskRows.class, b);
                Container box = (Container) SwingUtilities.getAncestorOfClass(Card.class, b);
                Container frame = rows != null ? rowOf(rows, b) : box != null ? box : root;
                Point p = SwingUtilities.convertPoint(b, b.getWidth() / 2, b.getHeight() / 2, frame);
                String v = p.y < frame.getHeight() / 3 ? "위" : p.y > frame.getHeight() * 2 / 3 ? "아래" : "가운데";
                String h = p.x < frame.getWidth() / 3 ? "왼쪽" : p.x > frame.getWidth() * 2 / 3 ? "오른쪽" : "가운데";
                at.computeIfAbsent(label(b), k -> new ArrayList<>()).add(name + "|" + v + " " + h);
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

    /** 규칙 6: 기본 창(1000x680)과 창들에서 글 부품의 글이 잘리는가. HTML 글은 줄을 바꾸므로 뺀다. */
    private void clipped(Map<String, JComponent> screens) {
        screens.forEach((name, root) -> {
            // 스스로 그리는 부품(알약 탭, 장신구 카드 등)은 선호 크기에 여백이 들어 있어 견줄 수 없다.
            // 글을 Swing이 그리는 라벨과 기본 버튼만 본다.
            List<JComponent> parts = new ArrayList<>(all(root, JLabel.class));
            for (AbstractButton bt : all(root, AbstractButton.class)) {
                Class<?> k = bt.getClass();
                if (k == javax.swing.JButton.class || k == javax.swing.JToggleButton.class || k == javax.swing.JCheckBox.class
                        || k == javax.swing.JRadioButton.class) {
                    parts.add(bt);
                }
            }
            for (JComponent c : parts) {
                String text = c instanceof JLabel l ? l.getText() : ((AbstractButton) c).getText();
                if (text == null || text.isBlank() || text.startsWith("<html") || !shown(c, root) || c.getWidth() <= 0) {
                    continue;
                }
                int need = c.getPreferredSize().width;
                if (need > c.getWidth()) {
                    found.add(new String[] {"6 글 잘림", name, "\"" + text + "\" 필요 폭 " + need + ", 실제 폭 " + c.getWidth()});
                }
            }
        });
    }

    /**
     * 규칙 5: 화면의 보이는 활성 버튼을 하나씩 누르고 누르기 전과 뒤의 그림을 견준다. 그대로면 반응이 없는 버튼이다.
     * 창을 여는 버튼은 창 없이 돌 때 예외가 나므로 반응한 것으로 친다. 묶음에서 이미 눌린 탭은 관례라 뺀다.
     * 버튼마다 앱을 새로 만들어 앞 버튼이 바꾼 상태가 남지 않게 한다.
     */
    private void reacts() {
        Dimension size = new Dimension(1000, 680);
        for (String name : new String[] {"홈", "일정관리", "나의 방", "통계", "설정"}) {
            int count = pressable(open(name, size)).size();
            for (int i = 0; i < count; i++) {
                App app = open(name, size);
                List<AbstractButton> bs = pressable(app);
                if (i >= bs.size()) {
                    break;
                }
                AbstractButton b = bs.get(i);
                int[] before = picture(app.root, size);
                boolean opened = false;
                try {
                    b.doClick(0);
                } catch (RuntimeException e) {
                    opened = true;
                }
                lay(app.root, size);
                if (!opened && java.util.Arrays.equals(before, picture(app.root, size))) {
                    found.add(new String[] {"5 누르면 그림이 바뀐다", name, "\"" + name(b) + "\"을 눌러도 화면이 그대로다"});
                }
            }
        }
    }

    private static App open(String name, Dimension size) {
        App app = fresh();
        app.show(name);
        app.store.changed();
        lay(app.root, size);
        return app;
    }

    private static List<AbstractButton> pressable(App app) {
        List<AbstractButton> out = new ArrayList<>();
        for (AbstractButton b : all(app.root, AbstractButton.class)) {
            boolean tab = b instanceof javax.swing.JToggleButton && b.isSelected()
                    && b.getModel() instanceof javax.swing.DefaultButtonModel m && m.getGroup() != null;
            // 스크롤 막대의 화살표는 누름이 아니라 마우스를 누르고 있는 동안 움직여 이 규칙 밖이다.
            boolean scroll = javax.swing.SwingUtilities.getAncestorOfClass(javax.swing.JScrollBar.class, b) != null;
            if (b.isEnabled() && shown(b, app.root) && b.getWidth() > 0 && !tab && !scroll) {
                out.add(b);
            }
        }
        return out;
    }

    private static int[] picture(JComponent root, Dimension size) {
        java.awt.image.BufferedImage im = new java.awt.image.BufferedImage(size.width, size.height, java.awt.image.BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D g = im.createGraphics();
        root.paint(g);
        g.dispose();
        return im.getRGB(0, 0, size.width, size.height, null, 0, size.width);
    }

    private static String name(AbstractButton b) {
        String t = b.getText();
        if (t != null && !t.isBlank()) {
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
