package planningpet.schedule.model;

import java.time.LocalDateTime;

/** 06 17번의 보낸 알림: 일정 id와 시작 일시의 짝. 시각을 고치면 새 짝이 된다(D-24). 저장하지 않는다. */
public record ReminderKey(String id, LocalDateTime start) {
}
