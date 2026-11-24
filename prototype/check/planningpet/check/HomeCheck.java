package planningpet.check;

import static planningpet.check.ProtoCheck.eq;

import planningpet.app.Main;
import planningpet.app.model.MockData;
import planningpet.app.model.Store;
import planningpet.app.view.App;
import planningpet.home.view.MainScreen;
import planningpet.schedule.model.Task;
import planningpet.schedule.view.TaskDialog;
import planningpet.schedule.view.TaskForm;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** 홈 결정(D-74, D-86, D-88, D-90, D-91)의 06 검사 줄. */
final class HomeCheck {
    private HomeCheck() {
    }

    static void run() {
        added();
        doneGroup();
    }

    static LocalDate d(String monthDay) {
        return LocalDate.parse("2026-" + monthDay);
    }

    static Store at(String monthDayTime) {
        Store s = MockData.empty();
        s.now = LocalDateTime.parse("2026-" + monthDayTime);
        return s;
    }

    /** T-TALK-23. 홈에서 넣으면 펫이 이름과 오늘 남은 수를 말한다(D-90). */
    static void added() {
        Store s = at("11-16T09:00");
        s.book.add(Task.Kind.TASK, "과제", d("11-16"), null, 30, "", null);
        App app = Main.create(s);
        MainScreen main = ProtoCheck.find(app.root, MainScreen.class);
        main.quick.setText("통계 레포트");
        main.quick.postActionEvent();
        String today = s.talk;
        main.selectDate(d("11-20"));
        main.quick.setText("은행 서류");
        main.quick.postActionEvent();
        String other = s.talk;
        app.show("일정관리");
        TaskForm f = new TaskForm(null, d("11-17"), s.now);
        f.title.setText("운동");
        boolean saved = TaskDialog.trySave(app, f, null);
        eq("T-TALK-23", "'통계 레포트' 넣었구나! 오늘 남은 일 2개야. / '은행 서류' 11/20에 넣었어. / true '은행 서류' 11/20에 넣었어.",
                today + " / " + other + " / " + saved + " " + s.talk);
    }

    /** T-LIST-8. 끝낸 줄은 제자리에 머문 뒤 아래 "끝낸 일" 묶음으로 가고, 묶음은 접힌다(D-88). */
    static void doneGroup() {
        Store s = at("11-16T08:00");
        s.book.add(Task.Kind.TASK, "A", d("11-16"), java.time.LocalTime.of(9, 0), 30, "", null);
        Task b = s.book.add(Task.Kind.TASK, "B", d("11-16"), null, 30, "", null).get(0);
        s.book.add(Task.Kind.TASK, "C", d("11-16"), null, 30, "", null);
        App app = Main.create(s);
        MainScreen main = ProtoCheck.find(app.root, MainScreen.class);
        app.redraw();
        PeriodCheck.box(main.rows, b).doClick();
        String stay = rows(main.rows) + " " + main.rows.settling();
        main.rows.settle();
        String settled = rows(main.rows);
        main.rows.doneHead.doClick();
        String folded = rows(main.rows);
        main.rows.doneHead.doClick();
        PeriodCheck.box(main.rows, b).doClick();
        String back = rows(main.rows) + " " + main.rows.settling();
        eq("T-LIST-8", "[ ] A 09:00~09:30 30분 / 시각 없는 할 일 / [x] B 시각 정하기 30분 / [ ] C 시각 정하기 30분 true"
                + " | [ ] A 09:00~09:30 30분 / 시각 없는 할 일 / [ ] C 시각 정하기 30분 / 끝낸 일 1개 / [x] B 시각 정하기 30분"
                + " | [ ] A 09:00~09:30 30분 / 시각 없는 할 일 / [ ] C 시각 정하기 30분 / 끝낸 일 1개"
                + " | [ ] A 09:00~09:30 30분 / 시각 없는 할 일 / [ ] B 시각 정하기 30분 / [ ] C 시각 정하기 30분 false",
                stay + " | " + settled + " | " + folded + " | " + back);
    }

    /** 목록을 줄마다 "[x] 제목 시각 분"처럼 읽는다(QA의 rows와 같다). */
    static String rows(java.awt.Container list) {
        java.util.List<String> out = new java.util.ArrayList<>();
        for (java.awt.Component c : list.getComponents()) {
            java.util.List<String> parts = new java.util.ArrayList<>();
            collect(c, parts);
            out.add(String.join(" ", parts));
        }
        return String.join(" / ", out);
    }

    private static void collect(java.awt.Component c, java.util.List<String> parts) {
        if (c instanceof javax.swing.JCheckBox b) {
            parts.add(b.isSelected() ? "[x]" : "[ ]");
        } else if (c instanceof javax.swing.JLabel l && !plain(l.getText()).isEmpty()) {
            parts.add(plain(l.getText()));
        } else if (c instanceof javax.swing.JButton b) {
            if (!b.getText().isBlank()) {
                parts.add(b.getText());
            }
        } else if (c instanceof java.awt.Container k) {
            for (java.awt.Component child : k.getComponents()) {
                collect(child, parts);
            }
        }
    }

    static String plain(String html) {
        return html == null ? "" : html.replaceAll("(?i)<br>", " ").replaceAll("<[^>]+>", "").replace("&lt;", "<")
                .replace("&gt;", ">").replace("&amp;", "&").replaceAll("\\s+", " ").strip();
    }
}
