package planningpet.schedule.model;

/** 일정 입력 창에서 잘못된 칸 하나(06 2번 출력). field는 "제목"처럼 입력 창의 칸 이름이다. */
public record FieldError(String field, String message) {
}
