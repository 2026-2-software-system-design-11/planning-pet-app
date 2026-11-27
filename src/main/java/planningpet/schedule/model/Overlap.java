package planningpet.schedule.model;

import java.time.LocalDate;
import planningpet.common.model.Words;

/** 겹침 묻기(06 3번, D-87)가 찾은 일정과 겹친 분. sameDay가 아니면 그 일정은 들어갈 날의 앞날이나 다음 날 일이다(D-94). */
public record Overlap(Task other, int minutes, boolean sameDay) {
    /** "14:00~15:00 '자소서 초안'과 30분 겹쳐요. 그래도 넣을까요?" 다른 날 일이면 앞에 "11/20(금) "을 붙인다(D-94). */
    public String question() {
        LocalDate d = other.getDate();
        String day = sameDay ? "" : d.getMonthValue() + "/" + d.getDayOfMonth() + "("
                + "월화수목금토일".charAt(d.getDayOfWeek().getValue() - 1) + ") ";
        return day + TaskBook.span(other.startMinute(), other.endMinute()) + " '" + other.getTitle() + "'"
                + Words.josa(other.getTitle(), "과", "와") + " " + minutes + "분 겹쳐요. 그래도 넣을까요?";
    }
}
