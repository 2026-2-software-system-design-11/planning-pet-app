package planningpet.pet.model;

import planningpet.app.model.Store;
import planningpet.common.model.Rules;
import planningpet.reward.model.*;
import planningpet.schedule.model.Task;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

/** 펫 대사 표(06의 15번, 16번). */
public final class Talk {
    private final Store s;

    public Talk(Store store) {
        this.s = store;
    }

    /** 그날 처음 창이 보일 때 하는 말. before는 그 전에 마지막으로 연 날(없으면 null)이다. 15번이 부른다. */
    public String opening(LocalDate before) {
        LocalDate today = s.today();
        if (before == null) {
            return "안녕, 나는 " + s.petName + Rules.iya(s.petName) + ". 일정관리에서 이번 학기 수업부터 넣어 볼까?";
        }
        if (ChronoUnit.DAYS.between(before, today) >= 8) {
            int n = s.checkables(today).size();
            return "오랜만이야! 다시 와서 반가워." + (n > 0 ? " 오늘 할 일은 " + n + "개야." : "");
        }
        return briefing();
    }

    public String briefing() {
        LocalDate today = s.today();
        StringBuilder b = new StringBuilder(usual());
        long yesterday = s.forDate(today.minusDays(1)).stream().filter(t -> !t.done).count();
        if (yesterday > 0) {
            b.append(" 어제 못 끝낸 일 ").append(yesterday).append("개는 달력에 남아 있어.");
        }
        Task near = s.tasks.stream()
                .filter(t -> t.kind == Task.Kind.TASK && !t.done)
                .filter(t -> t.date.isAfter(today) && !t.date.isAfter(today.plusDays(3)))
                .min(Comparator.comparing((Task t) -> t.date).thenComparingLong(t -> t.seq))
                .orElse(null);
        if (near != null) {
            String[] when = {"", "내일이야", "모레야", "사흘 뒤야"};
            b.append(" '").append(Rules.shortTitle(near.title)).append("' 마감이 ")
                    .append(when[(int) ChronoUnit.DAYS.between(today, near.date)]).append(".");
        }
        if (s.checkables(today).isEmpty() && near == null) {
            b.append(" 아래 칸에 하나 적어 볼까?");
        }
        return b.toString();
    }

    public String usual() {
        List<Task> list = s.checkables(s.today());
        long left = list.stream().filter(t -> !t.done).count();
        if (list.isEmpty()) {
            return "오늘은 할 일이 없어.";
        }
        return left > 0 ? "오늘 할 일 " + left + "개 남았어." : "오늘 할 일은 다 끝냈어!";
    }

    public String otherDate(LocalDate date) {
        int n = s.checkables(date).size();
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
        boolean allDone = t.date.equals(s.today()) && s.checkables(s.today()).stream().allMatch(x -> x.done);
        return allDone ? "오늘 할 일을 다 끝냈어! 푹 쉬어." : "잘했어! +10P";
    }
}
