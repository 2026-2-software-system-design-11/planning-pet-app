package planningpet.proto;

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

    public static void main(String[] args) throws Exception {
        QaRules r = new QaRules();
        r.inputs();
        Map<String, JComponent> screens = r.screens();
        r.terms(screens, new File(args[1]));
        r.places(screens);
        r.write(new File(args[0]));
        System.out.println("규칙 어긋남 " + r.found.size() + "개");
        System.exit(0);
    }

    /** 앱 창 네 화면과 창 열 가지. 창은 버튼에 동작이 붙기 전의 내용 판이다. */
    private Map<String, JComponent> screens() {
        Map<String, JComponent> m = new LinkedHashMap<>();
        for (String name : new String[] {"홈", "일정관리", "pet", "설정"}) {
            App app = fresh();
            app.show(name);
            app.store.changed();
            lay(app.root, new Dimension(1000, 680));
            m.put(name, app.root);
        }
        App app = fresh();
        LocalDate today = app.store.today();
        Task essay = app.store.dayPlan(today).timed().get(1);
        LocalDate monday = Store.mondayOf(today);
        m.put("일정 추가 창", TaskDialog.content(new TaskForm(null, today, app.store.now), new JButton("취소"), new JButton("저장"), null));
        m.put("일정 수정 창", TaskDialog.content(new TaskForm(essay, today, app.store.now), new JButton("취소"), new JButton("저장"),
                new JButton("삭제")));
        m.put("시각 고치기 창", new TimeEditor(essay, app.store.now).panel);
        m.put("자동 배치 결과 창", AutoPlaceDialog.content(app.store, monday, Planner.place(app.store, monday), new JButton("취소"),
                new JButton("적용")));
        m.put("상점 창", ShopDialog.content(app.store, x -> { }, new JButton("닫기")));
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
        app.store.say(app.talk.onWindowShown(app.game));
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
