package planningpet.qa;

import planningpet.schedule.model.Task;
import planningpet.schedule.view.TaskRows;
import planningpet.settings.view.SettingsScreen;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/** UF-1-6(D-102): 설정 "목록 누르기"를 켜고 홈과 일정관리 목록 줄을 한 번 눌러 시각 고치기 창 열기. */
final class FlowsSetting {
    private FlowsSetting() {
    }

    static void uf16() {
        Qa q = new Qa("UF-1-6");
        q.start(Personas.p1());
        q.open();
        List<String> opened = new ArrayList<>();
        q.app.schedule.timeOpener = (t, day, anchor) -> opened.add("시각 고치기 창 " + t.title + " " + day);
        q.app.schedule.opener = (t, day) -> opened.add("수정 창 " + t.title);
        q.step("1", "설정을 연다");
        q.app.show("설정");
        SettingsScreen s = QaDrive.find(q.app.root, SettingsScreen.class);
        q.eq("목록 누르기", "줄을 한 번 누르면 시각 고치기 창 열기 false", s.listClickTime.getText() + " " + s.listClickTime.isSelected());
        q.has("설명", "켜면 홈과 일정관리 목록에서 줄을 한 번 누를 때 시각 고치기 창이 떠요. 모든 칸은 연필 단추로 고쳐요."
                + " 끄면 두 번 누를 때 수정 창이 떠요.", Flows8910.text(s));
        q.step("2", "홈에서 토익 단어 줄의 제목을 한 번, 이어서 빨리 두 번 누른다");
        q.app.show("홈");
        Task toeic = q.task("토익 단어");
        click(q.main.rows, toeic, 1);
        String once = String.join(", ", opened);
        click(q.main.rows, toeic, 1);
        click(q.main.rows, toeic, 2);
        q.eq("창", " / 수정 창 토익 단어", once + " / " + String.join(", ", opened));
        opened.clear();
        q.step("3", "설정에서 목록 누르기를 켠다");
        q.app.show("설정");
        s.listClickTime.doClick();
        q.eq("켜짐과 저장", "true true", s.listClickTime.isSelected() + " " + q.app.store.listClickTime);
        q.step("4", "홈에서 토익 단어 줄의 제목을 빨리 두 번 누른다");
        q.app.show("홈");
        click(q.main.rows, toeic, 1);
        click(q.main.rows, toeic, 2);
        q.eq("창", "시각 고치기 창 토익 단어 2026-11-14", String.join(", ", opened));
        opened.clear();
        q.step("5", "일정관리를 열고 11/15 은행 서류 줄의 제목을 한 번 누른다");
        q.app.show("일정관리");
        click(q.sched.untimed, q.task("은행 서류"), 1);
        q.eq("창", "시각 고치기 창 은행 서류 2026-11-15", String.join(", ", opened));
    }

    /** 목록에서 t 줄의 제목 칸에 누르기를 보낸다. count 2는 빨리 두 번 누른 두 번째다. */
    private static void click(Container list, Task t, int count) {
        for (Component row : list.getComponents()) {
            if (row instanceof Container k && QaDrive.boxOf(k, t) != null) {
                TaskRows.TitleLabel l = QaDrive.find(k, TaskRows.TitleLabel.class);
                l.dispatchEvent(new MouseEvent(l, MouseEvent.MOUSE_CLICKED, 0, 0, 5, 5, count, false, MouseEvent.BUTTON1));
                return;
            }
        }
        throw new IllegalStateException("줄 없음: " + t.title);
    }
}
