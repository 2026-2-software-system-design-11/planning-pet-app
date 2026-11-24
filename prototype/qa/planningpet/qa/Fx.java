package planningpet.qa;

import planningpet.app.model.Store;
import planningpet.reward.model.Game;
import planningpet.schedule.model.Task;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/** 페르소나 고정 데이터를 만드는 도구(docs/personas.md). */
final class Fx {
    private Fx() {
    }

    /** owned, placed는 쉼표로 나눈 아이템 이름. attendance가 null이면 출석 기록 없음. */
    static Store store(String now, int points, int exp, int streak, String lastOpen, String attendance, int rewardsToday,
                       String kind, String name, String owned, String placed) {
        Store s = new Store();
        s.now = LocalDateTime.parse(now);
        s.points = points;
        s.streak = streak;
        s.lastOpenDay = lastOpen == null ? null : LocalDate.parse(lastOpen);
        s.attendanceDay = attendance == null ? null : LocalDate.parse(attendance);
        s.rewardCountDay = s.today();
        s.rewardCount = rewardsToday;
        s.pets.clear();
        s.pets.add(new planningpet.pet.model.PetState(kind, name));
        s.activeKind = kind;
        s.pet().exp = exp;
        if (attendance != null) {
            s.attendDays.add(s.attendanceDay);
        }
        if (!owned.isEmpty()) {
            s.owned.addAll(List.of(owned.split(",")));
        }
        if (!placed.isEmpty()) {
            s.placed.addAll(List.of(placed.split(",")));
        }
        return s;
    }

    static void weekly(Store s, String title, String first, String time, int minutes, String end) {
        s.book.add(Task.Kind.FIXED, title, LocalDate.parse(first), LocalTime.parse(time), minutes, "", LocalDate.parse(end));
    }

    static Task task(Store s, String date, String title, String time, int minutes) {
        return s.book.add(Task.Kind.TASK, title, LocalDate.parse(date), time == null ? null : LocalTime.parse(time),
                minutes, "", null).get(0);
    }

    /** 끝냄. rewardDay 앱 날짜에 10P와 경험치 10을 받았다. */
    static Task done(Task t, String rewardDay) {
        t.done = true;
        t.rewardPoints = Game.TASK_POINTS;
        t.rewardExp = Game.TASK_EXP;
        t.rewardDay = LocalDate.parse(rewardDay);
        return t;
    }
}
