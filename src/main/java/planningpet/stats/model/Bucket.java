package planningpet.stats.model;

import java.time.LocalDate;

/** 추이의 한 칸(06 29번 출력): 첫날, 끝날, 그 칸의 Rate. 주간은 하루, 월간은 한 주다. */
public record Bucket(LocalDate from, LocalDate to, Rate rate) {
}
