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
}
