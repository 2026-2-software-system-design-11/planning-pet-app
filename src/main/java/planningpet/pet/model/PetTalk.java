package planningpet.pet.model;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;
import planningpet.app.model.AppData;
import planningpet.common.model.Words;
import planningpet.schedule.model.DayPlan;
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

    /** 그날의 "오늘 할 일": 06 6번 dayPlan의 dueToday, timed, untimed. 기간 안에 할 일은 세지 않는다(D-70). */
    private List<Task> checkables(LocalDate day) {
        DayPlan plan = data.getBook().dayPlan(day);
        return Stream.of(plan.dueToday(), plan.timed(), plan.untimed()).flatMap(List::stream).toList();
    }
}
