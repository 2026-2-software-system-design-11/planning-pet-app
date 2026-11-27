package planningpet.pet.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import planningpet.app.model.AppData;
import planningpet.common.model.Words;
import planningpet.schedule.model.DayPlan;
import planningpet.schedule.model.Kind;
import planningpet.schedule.model.Task;

/** 펫 말풍선의 문장(06 16번). 무엇을 말할지는 부르는 controller가 고르고, 이 클래스는 그 상황의 문장을 만든다. */
public final class PetTalk {
    private final AppData data;

    public PetTalk(AppData data) {
        this.data = data;
    }

    /** 인자가 없는 상황의 말. 그 밖의 상황이면 IllegalArgumentException. */
    public String say(Situation situation) {
        return switch (situation) {
            case FIRST_RUN -> firstRun();
            case WELCOME_BACK -> welcomeBack();
            case BRIEFING -> briefing();
            case USUAL -> usual();
            default -> throw new IllegalArgumentException("인자가 더 필요한 상황: " + situation);
        };
    }

    /** OTHER_DATE: 그날 할 일 수. 그 밖의 상황이면 IllegalArgumentException. */
    public String say(Situation situation, LocalDate day) {
        if (situation != Situation.OTHER_DATE) {
            throw new IllegalArgumentException("날을 받지 않는 상황: " + situation);
        }
        int n = checkables(day).size();
        String date = day.getMonthValue() + "월 " + day.getDayOfMonth() + "일";
        if (n > 0) {
            return date + " 할 일은 " + n + "개야.";
        }
        // D-94: 기간 할 일만 있는 날은 목록에 보이는 기간 할 일 수를 덧붙인다.
        int period = data.getBook().dayPlan(day).period().size();
        return date + "은 할 일이 없어." + (period > 0 ? " 기간 할 일 " + period + "개가 있어." : "");
    }

    /** D-74: 처음 만난 인사. 바로 시작이면 수업부터 넣자고 이끌고, 예시로 둘러보기면 예시를 넣어 뒀다고 말한다. */
    private String firstRun() {
        String name = data.activePet().getName();
        return "처음 만났네! 나는 " + name + Words.iya(name) + ". "
                + (data.getSampleIds().isEmpty() ? "수업부터 넣어 볼까?" : "예시 일정을 넣어 뒀어.");
    }

    private String welcomeBack() {
        int n = checkables(data.today()).size();
        return "오랜만이야! 다시 와서 반가워." + (n > 0 ? " 오늘 할 일은 " + n + "개야." : "");
    }

    /** 06 16번 BRIEFING의 1~6번 문장. 해당하지 않는 문장은 뺀다. */
    private String briefing() {
        LocalDate today = data.today();
        List<Task> tasks = data.getBook().getTasks();
        StringBuilder b = new StringBuilder(left());
        // 세는 날(06 데이터 절)이 어제인 할 일. 놓친 반복 사본은 밀린 일이 아니다(D-69).
        long yesterday = tasks.stream().filter(t -> t.getKind() == Kind.TASK && !t.isDone()
                && t.countDay().equals(today.minusDays(1)) && !t.pastCopy(today)).count();
        if (yesterday > 0) {
            b.append(" 어제 못 끝낸 일 ").append(yesterday).append("개는 달력에 남아 있어.");
        }
        // D-67: 어제로 시각을 정한 기간 할 일을 못 끝냈으면 그다음 날 한 번 말한다.
        Task told = tasks.stream().filter(t -> t.missed(today) && t.getDate().equals(today.minusDays(1)))
                .min(Comparator.comparingInt(Task::startMinute).thenComparingLong(Task::getSeq)).orElse(null);
        if (told != null) {
            b.append(" 어제 하기로 한 '").append(Words.shortTitle(told.getTitle())).append("'")
                    .append(Words.josa(told.getTitle(), "을", "를")).append(" 못 했어. 마감은 ")
                    .append(told.getDueDate().getDayOfMonth()).append("일이야.");
        }
        // D-70, D-94: 마감 문장. 3번 문장이 말한 일은 빼고, 오늘로 시각을 정한 것도 말한다.
        List<Task> open = tasks.stream().filter(t -> t != told && t.period() && !t.isDone()).toList();
        Task dueToday = open.stream().filter(t -> t.getDueDate().equals(today))
                .min(Comparator.comparingLong(Task::getSeq)).orElse(null);
        Task near = dueToday != null ? null : open.stream()
                .filter(t -> t.getDueDate().isAfter(today) && !t.getDueDate().isAfter(today.plusDays(3)))
                .min(Comparator.comparing(Task::getDueDate).thenComparingLong(Task::getSeq)).orElse(null);
        if (dueToday != null) {
            b.append(" 오늘이 ").append(Words.shortTitle(dueToday.getTitle())).append(" 마감이야!");
        } else if (near != null) {
            int n = (int) ChronoUnit.DAYS.between(today, near.getDueDate());
            String when = n == 1 ? "내일" : n == 2 ? "모레" : n + "일 뒤";
            b.append(" '").append(Words.shortTitle(near.getTitle())).append("' 마감이 ").append(when).append("(")
                    .append(near.getDueDate().getDayOfMonth()).append("일)").append(Words.josa(when, "이야", "야"))
                    .append(", D-").append(n).append(".");
        }
        // D-71: 내일의 보통 할 일. dayPlan(내일)의 시각 있는 것, 그다음 시각 없는 것 순이다.
        DayPlan next = data.getBook().dayPlan(today.plusDays(1));
        Task tomorrow = Stream.concat(next.timed().stream().filter(t -> t.getKind() == Kind.TASK),
                next.untimed().stream().filter(t -> !t.period())).filter(t -> !t.isDone()).findFirst().orElse(null);
        if (tomorrow != null) {
            b.append(" 내일 '").append(Words.shortTitle(tomorrow.getTitle())).append("'")
                    .append(Words.josa(tomorrow.getTitle(), "이", "가")).append(" 있어.");
        }
        // 6번 문장: 오늘 할 일이 없고 마감, 내일 문장이 없을 때. 3번 문장이 말한 일은 빼고 본다(D-94).
        if (checkables(today).isEmpty() && dueToday == null && near == null && tomorrow == null) {
            String period = periodLine(told);
            b.append(period != null ? " " + period : told != null ? "" : " 아래 칸에 하나 적어 볼까?");
        }
        return b.toString();
    }

    /** 오늘 할 일이 없고 기간 안에 할 일이 있으면 기간 할 일 문장을 붙인다(D-94). */
    private String usual() {
        String period = checkables(data.today()).isEmpty() ? periodLine(null) : null;
        return left() + (period == null ? "" : " " + period);
    }

    /** BRIEFING 1번 문장: 오늘 남은 수. */
    private String left() {
        List<Task> list = checkables(data.today());
        long left = list.stream().filter(t -> !t.isDone()).count();
        if (list.isEmpty()) {
            return "오늘은 할 일이 없어.";
        }
        return left > 0 ? "오늘 할 일 " + left + "개 남았어." : "오늘 할 일은 다 끝냈어!";
    }

    /** 오늘의 기간 안에 할 일 가운데(skip은 빼고) 마감, 넣은 순 첫 번째의 "{제목}은 {일}일까지야, {n}일 남았어."(D-70). */
    private String periodLine(Task skip) {
        LocalDate today = data.today();
        return data.getBook().dayPlan(today).period().stream().filter(p -> p != skip)
                .min(Comparator.comparing(Task::getDueDate).thenComparingLong(Task::getSeq))
                .map(p -> Words.shortTitle(p.getTitle()) + Words.josa(p.getTitle(), "은", "는") + " "
                        + p.getDueDate().getDayOfMonth() + "일까지야, " + ChronoUnit.DAYS.between(today, p.getDueDate())
                        + "일 남았어.")
                .orElse(null);
    }

    /** 그날의 "오늘 할 일": 06 6번 dayPlan의 dueToday, timed, untimed. 기간 안에 할 일은 세지 않는다(D-70). */
    private List<Task> checkables(LocalDate day) {
        DayPlan plan = data.getBook().dayPlan(day);
        return Stream.of(plan.dueToday(), plan.timed(), plan.untimed()).flatMap(List::stream).toList();
    }
}
