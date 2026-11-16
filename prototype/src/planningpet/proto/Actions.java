package planningpet.proto;

import java.time.LocalDate;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

/** 여러 화면이 같이 쓰는 사용자 동작: 체크, 수정, 삭제, 추가. */
public final class Actions {
    private final App app;

    Actions(App app) {
        this.app = app;
    }

    /** UC-07, UC-08. */
    public void check(Task t) {
        if (t.done) {
            int points = t.rewardPoints;
            int before = Rules.level(app.store.exp);
            app.game.uncomplete(t);
            app.store.talk = app.talk.uncompleted(points, before, Rules.level(app.store.exp));
        } else {
            RewardResult r = app.game.complete(t);
            app.store.talk = app.talk.completed(r, t);
        }
        app.store.changed();
    }

    /** UC-06. 반복 사본이면 범위를 묻는다. 지웠으면 true. */
    public boolean delete(Task t) {
        boolean deleted = false;
        if (t.seriesId == null) {
            int answer = JOptionPane.showOptionDialog(app.root, "'" + t.title + "'" + josa(t.title, "을", "를") + " 지울까요?",
                    "일정 삭제", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                    new String[] {"지우기", "취소"}, "취소");
            if (answer == 0) {
                app.store.delete(t, false);
                deleted = true;
            }
        } else {
            int answer = JOptionPane.showOptionDialog(app.root, "'" + t.title + "'" + josa(t.title, "은", "는") + " 매주 반복 일정이에요.",
                    "일정 삭제", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                    new String[] {"이 일정만", "이후 반복 모두", "취소"}, "취소");
            if (answer == 0 || answer == 1) {
                app.store.delete(t, answer == 1);
                deleted = true;
            }
        }
        app.store.changed();
        return deleted;
    }

    /** UC-05. */
    public void edit(Task t) {
        TaskDialog.open(app, t, t.date);
    }

    /** UC-03. 날짜는 띄운 화면이 고른다(06의 14번). */
    public void add(LocalDate date) {
        TaskDialog.open(app, null, date);
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
    static String josa(String word, String withBatchim, String without) {
        return Rules.hasBatchim(word) ? withBatchim : without;
    }
}
