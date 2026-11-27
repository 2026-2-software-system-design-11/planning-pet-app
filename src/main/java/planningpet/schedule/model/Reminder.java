package planningpet.schedule.model;

import java.time.LocalDateTime;

/** 06 17번 출력. minutes는 시작까지 남은 분을 올린 값, title()과 text()는 운영체제 알림의 제목과 내용이다. */
public record Reminder(Task task, LocalDateTime start, int minutes) {
    public String title() {
        return "플래닝펫";
    }

    public String text() {
        return task.getTitle() + " " + minutes + "분 뒤 시작";
    }
}
