package planningpet.check;

import static planningpet.check.ProtoCheck.eq;
import static planningpet.check.PeriodCheck.at;
import static planningpet.check.PeriodCheck.d;

import planningpet.app.Main;
import planningpet.app.model.Store;
import planningpet.app.view.App;
import planningpet.reward.model.DailyProgress;
import planningpet.reward.model.Game;
import planningpet.schedule.model.*;
import planningpet.schedule.view.*;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** 06 검사 표 가운데 앱 하루의 시간표(D-77), 겹침 묻기(D-87), 시간표 스크롤(D-89)의 줄. ProtoCheck가 부른다. */
final class DayCheck {
    private DayCheck() {
    }

    static void run() {
        nightDay();
    }

    /** 앱 하루의 분으로 시각을 준 할 일 하나를 넣는다. */
    static Task task(Store s, String title, String date, Integer minute, int minutes) {
        return s.book.add(TaskDraft.at(Task.Kind.TASK, title, d(date), minute, minutes)).get(0);
    }

    static int hm(int h, int m) {
        return h * 60 + m;
    }

    /** D-77: 하루 칸은 다음 날 04:00(28:00)까지다. */
    private static void nightDay() {
        Store s = at("11-16T09:00");
        String texts = List.of(task(s, "a", "11-16", hm(9, 0), 30), task(s, "b", "11-16", hm(23, 30), 30),
                task(s, "c", "11-16", hm(23, 30), 90), task(s, "d", "11-16", hm(25, 30), 30)).stream()
                .map(TaskBook::timeText).collect(Collectors.joining(", "));
        eq("T-LIST-2", "09:00~09:30, 23:30~24:00, 23:30~01:00(다음 날), 01:30~02:00(다음 날)", texts);

        Store g = at("11-21T01:45");
        Task late = task(g, "복습", "11-20", hm(25, 30), 30);
        new Game(g).complete(late);
        eq("T-PRG-7", "2026-11-20 1/1 2026-11-20", late.rewardDay + " " + DailyProgress.of(g.book, g.today()).done() + "/"
                + DailyProgress.of(g.book, g.today()).total() + " " + g.rewardCountDay);

        Store c = at("11-16T09:00");
        Task ca = task(c, "A", "11-16", hm(23, 30), 90);
        Task cb = task(c, "B", "11-16", hm(24, 30), 30);
        var cols = Planner.columns(List.of(ca, cb));
        eq("T-TT-6", "0/2 1/2", cols.get(ca)[0] + "/" + cols.get(ca)[1] + " " + cols.get(cb)[0] + "/" + cols.get(cb)[1]);

        Store v = at("11-16T09:00");
        String vals = PeriodCheck.msgs(TaskCheck.check(TaskDraft.at(Task.Kind.TASK, "밤", d("11-16"), hm(23, 30), 40), false)) + " / "
                + PeriodCheck.msgs(TaskCheck.check(TaskDraft.at(Task.Kind.TASK, "밤", d("11-16"), hm(27, 30), 30), false)) + " / "
                + PeriodCheck.msgs(TaskCheck.check(TaskDraft.at(Task.Kind.TASK, "밤", d("11-16"), hm(27, 30), 40), false));
        eq("T-VAL-8, T-VAL-20", "오류 없음 / 오류 없음 / 시작 시각: 다음 날 04:00까지 끝나야 해요", vals);

        // 자동 배치는 자정 앞까지만 놓는다(잠자는 시간에 놓지 않는다).
        Store p = at("11-16T23:40");
        task(p, "E", "11-16", null, 30);
        eq("D-77 자동 배치는 24:00까지", "0 E", Planner.place(p, d("11-16")).placed().size() + " "
                + Planner.place(p, d("11-16")).unplaced().get(0).title);
    }
}
