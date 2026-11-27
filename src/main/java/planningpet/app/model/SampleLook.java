package planningpet.app.model;

import java.time.LocalDate;
import java.time.LocalTime;

/** 예시 일정을 넣을 때의 모습(06 데이터 절 sampleLooks, D-94). 하나라도 다르면 고친 예시다(06 31번). */
public record SampleLook(String title, LocalTime startTime, boolean nextDay, String memo, LocalDate date) {
}
