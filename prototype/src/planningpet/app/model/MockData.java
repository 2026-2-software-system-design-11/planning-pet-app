package planningpet.app.model;

import planningpet.reward.model.Game;
import planningpet.schedule.model.Task;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;

/** 05 와이어프레임과 같은 목 데이터. 시계는 2026-11-14(토) 15:23에 멈춰 있다. */
public final class MockData {
    public static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 14, 15, 23);
    private static final LocalDate TERM_START = LocalDate.of(2026, 8, 31);
    private static final LocalDate TERM_END = LocalDate.of(2026, 12, 18);

    private MockData() {
    }

    /** 첫 실행: 데이터 없음. 반려동물 고르기 창부터 뜬다. */
    public static Store empty() {
        Store s = new Store();
        s.now = NOW;
        return s;
    }

    /** 반려동물 고르기 창의 미리보기. 그 동물만 정한 빈 데이터다. */
    public static Store look(String petKind) {
        Store s = empty();
        s.petKind = petKind;
        return s;
    }

    public static Store sample() {
        Store s = empty();
        s.points = 100;
        s.exp = 210;
        s.streak = 4;
        s.lastOpenDay = LocalDate.of(2026, 11, 13);
        s.rewardCountDay = LocalDate.of(2026, 11, 14);
        s.rewardCount = 1;
        s.owned.add("쿠션");
        s.owned.add("화분");
        s.owned.add("러그");
        s.placed.add("쿠션");
        s.placed.add("화분");

        weekly(s, "자료구조", DayOfWeek.MONDAY, 9, 0);
        weekly(s, "자료구조", DayOfWeek.WEDNESDAY, 9, 0);
        weekly(s, "운영체제", DayOfWeek.TUESDAY, 10, 0);
        weekly(s, "운영체제", DayOfWeek.THURSDAY, 10, 0);
        weekly(s, "소프트웨어설계", DayOfWeek.MONDAY, 13, 0);

        task(s, 10, "빨래", null, 30);
        task(s, 12, "도서관 책 반납", null, 30);
        done(task(s, 13, "토익 스터디", LocalTime.of(13, 0), 90), 13);
        done(task(s, 14, "알고리즘 과제 2번", LocalTime.of(10, 0), 60), 14);
        task(s, 14, "자소서 초안", LocalTime.of(14, 0), 90).memo = "1번 문항 먼저 쓰기\n지난번 초안 참고";
        task(s, 14, "토익 단어", null, 30);
        task(s, 14, "운동", null, 30);
        task(s, 15, "은행 서류", null, 30);
        task(s, 18, "통계 레포트", null, 120);
        return s;
    }

    private static void weekly(Store s, String title, DayOfWeek day, int hour, int minute) {
        LocalDate first = TERM_START.with(TemporalAdjusters.nextOrSame(day));
        s.book.add(Task.Kind.FIXED, title, first, LocalTime.of(hour, minute), 75, "", TERM_END);
    }

    private static Task task(Store s, int day, String title, LocalTime start, int minutes) {
        return s.book.add(Task.Kind.TASK, title, LocalDate.of(2026, 11, day), start, minutes, "", null).get(0);
    }

    private static void done(Task t, int rewardDay) {
        t.done = true;
        t.rewardPoints = Game.TASK_POINTS;
        t.rewardExp = Game.TASK_EXP;
        t.rewardDay = LocalDate.of(2026, 11, rewardDay);
    }
}
