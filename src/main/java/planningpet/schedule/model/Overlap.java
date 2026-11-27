package planningpet.schedule.model;

/** 겹침 묻기(06 3번, D-87)가 찾은 일정과 겹친 분. sameDay가 아니면 그 일정은 들어갈 날의 앞날이나 다음 날 일이다(D-94). */
public record Overlap(Task other, int minutes, boolean sameDay) {
}
