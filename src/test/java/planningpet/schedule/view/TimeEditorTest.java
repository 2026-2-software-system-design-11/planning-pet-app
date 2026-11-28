package planningpet.schedule.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static planningpet.schedule.model.Tasks.d;
import static planningpet.schedule.model.Tasks.task;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import planningpet.schedule.model.Task;

/** 시각 고치기 창 내용. 번호는 docs/porting/schedule.md S5e 2절. 오늘은 11-18(수)이고 창은 띄우지 않는다. */
class TimeEditorTest {
    private static final LocalDateTime NOW = LocalDateTime.parse("2026-11-18T15:23");

    /** 11-16부터 11-22까지의 기간 할 일 "보고서". at이 null이면 시각이 없다. */
    private static Task report(LocalDate placedOn, String at) {
        return (at == null ? task("보고서", placedOn) : task("보고서", placedOn).at(at)).period(d(16), d(22)).task();
    }

    private static TimeEditor open(Task t, LocalDate day, LocalDateTime now) {
        return new TimeEditor(t, day, now);
    }

    private static TimeEditor open(Task t, LocalDate day) {
        return open(t, day, NOW);
    }

    /** 시작 칸의 글, 걸리는 시간, 실제 시각 글. */
    private static List<Object> values(TimeEditor e) {
        return List.of(e.start.getValue(), e.minutes.getValue(), e.realTime.getText());
    }

    @Test
    void eTed1FirstValues() {
        Task run = task("운동", d(20)).at("10:00").minutes(45).task();
        assertEquals(List.of("10:00", 45, ""), values(open(run, d(20))));
        // 시각 없는 일은 defaultStart(창을 연 날, 지금): 오늘 15:23은 15:25, 다음 날 01:12는 앱 날 11-18의 01:15(다음 날)이다.
        Task laundry = task("빨래", d(18)).task();
        assertEquals(List.of("15:25", 30, ""), values(open(laundry, d(18))));
        TimeEditor dawn = open(laundry, d(18), LocalDateTime.parse("2026-11-19T01:12"));
        assertEquals(List.of("01:15(다음 날)", 30, "(목 01:15)"), values(dawn));
        // D-67: 11-17에 정해 두고 못 끝낸 계획은 시각 없는 할 일처럼 연다.
        assertEquals(List.of("15:25", 30, ""), values(open(report(d(17), "10:00"), d(18))));
    }
}
