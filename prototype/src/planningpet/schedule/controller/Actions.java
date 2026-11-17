package planningpet.schedule.controller;

import planningpet.app.view.App;
import planningpet.common.model.Rules;
import planningpet.reward.model.RewardResult;
import planningpet.schedule.model.Task;
import planningpet.schedule.view.*;
import java.time.LocalDate;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

/** 여러 화면이 같이 쓰는 사용자 동작: 체크, 수정, 삭제, 추가. */
public final class Actions {
    private final App app;

    public Actions(App app) {
        this.app = app;
        this.opener = (t, date) -> TaskDialog.open(app, t, date);
    }

    /** 삭제 확인 창. 고른 버튼의 번호를 돌려준다. QA는 이것을 바꿔 끼운다. */
    public interface Chooser {
        int choose(String title, String message, String[] options);
    }

    public Chooser chooser = this::ask;

    /** 일정 입력 창을 연다. editing이 null이면 추가다. QA는 이것을 바꿔 끼운다. */
    public interface Opener {
        void open(Task editing, LocalDate date);
    }

    public Opener opener;

    private int ask(String title, String message, String[] options) {
        return JOptionPane.showOptionDialog(app.root, message, title,
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[options.length - 1]);
    }

    /** UC-07, UC-08. */
    public void check(Task t) {
        if (t.done) {
            int points = t.rewardPoints;
            int before = Rules.level(app.store.exp);
            app.game.uncomplete(t);
            app.store.say(app.talk.uncompleted(points, before, Rules.level(app.store.exp)));
        } else {
            RewardResult r = app.game.complete(t);
            app.store.say(app.talk.completed(r, t));
        }
        app.store.changed();
    }

    /** UC-06. 반복 사본이면 범위를 묻는다. 지웠으면 true. */
    public boolean delete(Task t) {
        boolean deleted = false;
        if (t.seriesId == null) {
            int answer = chooser.choose("일정 삭제", "'" + t.title + "'" + josa(t.title, "을", "를") + " 삭제할까요?",
                    new String[] {"삭제", "취소"});
            if (answer == 0) {
                app.store.delete(t, false);
                deleted = true;
            }
        } else {
            int answer = chooser.choose("일정 삭제", "'" + t.title + "'" + josa(t.title, "은", "는") + " 매주 반복 일정이에요.",
                    new String[] {"이 일정만", "이후 반복 모두", "취소"});
            if (answer == 0 || answer == 1) {
                app.store.delete(t, answer == 1);
                deleted = true;
            }
        }
        app.store.changed();
        return deleted;
    }

    public LocalDate today() {
        return app.store.today();
    }

    /** D-42. 반복 사본을 저장할 때 범위를 묻는다. 0 이 일정만, 1 이후 반복 모두, -1 취소. 반복이 아니면 묻지 않고 0. */
    public int editScope(Task t) {
        if (t.seriesId == null) {
            return 0;
        }
        int answer = chooser.choose("반복 일정 저장", "'" + t.title + "'" + josa(t.title, "은", "는") + " 매주 반복 일정이에요. 어디까지 바꿀까요?",
                new String[] {"이 일정만", "이후 반복 모두", "취소"});
        return answer == 0 || answer == 1 ? answer : -1;
    }

    /** UC-05. */
    public void edit(Task t) {
        opener.open(t, t.date);
    }

    /** UC-03. 날짜는 띄운 화면이 고른다(06의 14번). */
    public void add(LocalDate date) {
        opener.open(null, date);
    }

    /** D-32. 메인 시각 칸을 누르면 그 아래에 시각 고치기 창을 연다. */
    public void editTime(Task t, java.awt.Component anchor) {
        TimeEditor.open(app, t, anchor);
    }

    /** 오른쪽 버튼 메뉴: 수정, 삭제. */
    public JPopupMenu menu(Task t) {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem edit = new JMenuItem("수정");
        edit.addActionListener(e -> edit(t));
        JMenuItem delete = new JMenuItem("삭제");
        delete.addActionListener(e -> delete(t));
        menu.add(edit);
        menu.add(delete);
        return menu;
    }

    /** 마지막 글자에 받침이 있으면 withBatchim, 없으면 without. */
    public static String josa(String word, String withBatchim, String without) {
        return Rules.hasBatchim(word) ? withBatchim : without;
    }
}
