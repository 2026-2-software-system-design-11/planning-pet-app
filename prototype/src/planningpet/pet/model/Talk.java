package planningpet.pet.model;

import planningpet.app.model.Store;
import planningpet.common.model.Rules;
import planningpet.reward.model.*;
import planningpet.schedule.model.Task;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

/** 펫 대사 표(06의 16번). 창이 보일 때 무엇을 말할지는 15번(AppController)이 고른다. */
public final class Talk {
    private final Store s;

    public Talk(Store store) {
        this.s = store;
    }

    /** 그날 처음 창이 보일 때 하는 말. before는 그 전에 마지막으로 연 날(없으면 null)이다. 15번이 부른다. */
    public String opening(LocalDate before) {
        LocalDate today = s.today();
        if (before == null) {
            return "안녕, 나는 " + s.pet().name + Rules.iya(s.pet().name) + ". 일정관리에서 이번 학기 수업부터 넣어 볼까?";
        }
        if (ChronoUnit.DAYS.between(before, today) >= 8) {
            int n = s.book.checkables(today).size();
            return "오랜만이야! 다시 와서 반가워." + (n > 0 ? " 오늘 할 일은 " + n + "개야." : "");
        }
        return briefing();
    }

    public String briefing() {
        LocalDate today = s.today();
        StringBuilder b = new StringBuilder(usual());
        // 세는 날(06 데이터 절)로 센다. 끝내지 않은 기간 할 일은 마감 날의 일이다(D-65).
        long yesterday = s.tasks.stream()
                .filter(t -> t.kind == Task.Kind.TASK && !t.done && t.countDay().equals(today.minusDays(1)))
                .filter(t -> !t.pastCopy(today)).count(); // D-69: 놓친 반복 사본은 밀린 일이 아니다.
        if (yesterday > 0) {
            b.append(" 어제 못 끝낸 일 ").append(yesterday).append("개는 달력에 남아 있어.");
        }
        // D-67: 어제로 시각을 정한 기간 할 일을 못 끝냈으면 그다음 날 아침 한 번 말한다.
        s.tasks.stream().filter(t -> t.missed(today) && t.date.equals(today.minusDays(1)))
                .min(Comparator.comparing((Task t) -> t.start).thenComparingLong(t -> t.seq))
                .ifPresent(t -> b.append(" 어제 하기로 한 '").append(Rules.shortTitle(t.title)).append("'")
                        .append(Rules.josa(t.title, "을", "를")).append(" 못 했어. 마감은 ").append(t.dueDate.getDayOfMonth())
                        .append("일이야."));
        Task dueToday = s.book.dayPlan(today).dueToday().stream().findFirst().orElse(null);
        if (dueToday != null) {
            // D-70: 마감 날까지 못 한 기간 할 일.
            b.append(" 오늘이 ").append(Rules.shortTitle(dueToday.title)).append(" 마감이야!");
        }
        // D-71: "마감"은 기간 할 일에만 쓰고 날짜와 남은 날 수를 붙인다.
        Task near = dueToday != null ? null : s.tasks.stream()
                .filter(t -> t.period() && !t.done)
                .filter(t -> t.dueDate.isAfter(today) && !t.dueDate.isAfter(today.plusDays(3)))
                .min(Comparator.comparing((Task t) -> t.dueDate).thenComparingLong(t -> t.seq))
                .orElse(null);
        if (near != null) {
            int n = (int) ChronoUnit.DAYS.between(today, near.dueDate);
            String when = n == 1 ? "내일" : n == 2 ? "모레" : n + "일 뒤";
            b.append(" '").append(Rules.shortTitle(near.title)).append("' 마감이 ").append(when).append("(")
                    .append(near.dueDate.getDayOfMonth()).append("일)").append(Rules.josa(when, "이야", "야"))
                    .append(", D-").append(n).append(".");
        }
        // D-71: 내일 날짜의 보통 할 일은 "내일 '{제목}'이 있어."
        Task tomorrow = s.book.dayPlan(today.plusDays(1)).timed().stream()
                .filter(t -> t.kind == Task.Kind.TASK && !t.done).findFirst()
                .or(() -> s.book.dayPlan(today.plusDays(1)).untimed().stream().filter(t -> !t.done && !t.period()).findFirst())
                .orElse(null);
        if (tomorrow != null) {
            b.append(" 내일 '").append(Rules.shortTitle(tomorrow.title)).append("'")
                    .append(Rules.josa(tomorrow.title, "이", "가")).append(" 있어.");
        }
        if (s.book.checkables(today).isEmpty() && near == null && dueToday == null && tomorrow == null) {
            // 기간 안에 할 일이 있으면 새로 적으라고 하지 않고 가장 가까운 마감을 알린다.
            String period = periodLine();
            b.append(period != null ? " " + period : " 아래 칸에 하나 적어 볼까?");
        }
        return b.toString();
    }

    /** 오늘의 기간 안에 할 일 가운데 마감, 넣은 순으로 첫 번째의 "{제목}은 {일}일까지야, {n}일 남았어."(D-70). 없으면 null. */
    private String periodLine() {
        LocalDate today = s.today();
        return s.book.dayPlan(today).period().stream()
                .min(Comparator.comparing((Task x) -> x.dueDate).thenComparingLong(x -> x.seq))
                .map(p -> Rules.shortTitle(p.title) + Rules.josa(p.title, "은", "는") + " " + p.dueDate.getDayOfMonth()
                        + "일까지야, " + ChronoUnit.DAYS.between(today, p.dueDate) + "일 남았어.")
                .orElse(null);
    }

    public String usual() {
        List<Task> list = s.book.checkables(s.today());
        long left = list.stream().filter(t -> !t.done).count();
        if (list.isEmpty()) {
            return "오늘은 할 일이 없어.";
        }
        return left > 0 ? "오늘 할 일 " + left + "개 남았어." : "오늘 할 일은 다 끝냈어!";
    }

    public String otherDate(LocalDate date) {
        int n = s.book.checkables(date).size();
        String day = date.getMonthValue() + "월 " + date.getDayOfMonth() + "일";
        return n > 0 ? day + " 할 일은 " + n + "개야." : day + "은 할 일이 없어.";
    }

    /** D-36. 완료 취소 때 빠진 보상을 말한다. */
    public String uncompleted(int points, int levelBefore, int levelAfter) {
        if (points == 0) {
            return "체크를 풀었어.";
        }
        return "체크를 풀었어. -" + points + "P" + (levelAfter < levelBefore ? ", 이제 Lv." + levelAfter : "");
    }

    /** 완료 하나가 여러 상황이면 STAGE_UP, LEVEL_UP, CAPPED, ALL_DONE, COMPLETED 순으로 하나만. */
    public String completed(RewardResult r, Task t) {
        if (r.stageUp()) {
            return "잘했어! 레벨이 올랐어. 이제 Lv." + r.levelAfter() + ", 나 좀 자란 것 같아.";
        }
        if (r.levelUp()) {
            return "잘했어! 레벨이 올랐어. 이제 Lv." + r.levelAfter();
        }
        if (r.outcome() == RewardResult.Outcome.CAPPED) {
            return "잘했어! 오늘 보상은 다 받았지만 체크는 계속 돼.";
        }
        boolean allDone = t.countDay().equals(s.today()) && s.book.checkables(s.today()).stream().allMatch(x -> x.done);
        if (!allDone) {
            return "잘했어! +10P";
        }
        // D-70: 기간 안에 할 일이 남았으면 가장 가까운 마감을 함께 말한다.
        String period = periodLine();
        return period != null ? "오늘 할 일 다 끝냈어! " + period : "오늘 할 일을 다 끝냈어! 푹 쉬어.";
    }
}
