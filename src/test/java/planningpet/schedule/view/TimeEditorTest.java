package planningpet.schedule.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static planningpet.schedule.model.Tasks.d;
import static planningpet.schedule.model.Tasks.task;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JLabel;
import org.junit.jupiter.api.Test;
import planningpet.common.view.Theme;
import planningpet.schedule.model.Kind;
import planningpet.schedule.model.Task;
import planningpet.schedule.model.TaskDraft;

/** 시각 고치기 창 내용. 번호는 docs/porting/schedule.md S5e 2절. 오늘은 11-18(수)이고 창은 띄우지 않는다. */
class TimeEditorTest {
    private static final LocalDateTime NOW = LocalDateTime.parse("2026-11-18T15:23");

    /** 11-16부터 11-22까지의 기간 할 일 "보고서". at이 null이면 시각이 없다. */
    private static Task report(LocalDate placedOn, String at) {
        return (at == null ? task("보고서", placedOn) : task("보고서", placedOn).at(at)).period(d(16), d(22)).task();
    }

    /** 저장 함수가 받은 값과 "닫음"을 차례로 적는다. */
    private final List<Object> calls = new ArrayList<>();
    /** 저장 함수가 돌려줄 값. */
    private boolean answer = true;

    private TimeEditor open(Task t, LocalDate day, LocalDateTime now) {
        TimeEditor e = new TimeEditor(t, day, now, (x, v) -> calls.add(v) && answer);
        e.close = () -> calls.add("닫음");
        return e;
    }

    private TimeEditor open(Task t, LocalDate day) {
        return open(t, day, NOW);
    }

    private static BorderLayout layout(Container c) {
        return (BorderLayout) c.getLayout();
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

    @Test
    void eTed2RealTime() {
        TimeEditor e = open(task("독서", d(20)).at("01:30").nextDay().task(), d(20));
        assertEquals("(토 01:30)", e.realTime.getText());
        e.start.setValue("00:30(다음 날)");
        assertEquals("(토 00:30)", e.realTime.getText());
        e.start.setValue("23:55");
        assertEquals("", e.realTime.getText());
        // 시각 없는 기간 할 일은 창을 연 날로 옮겨지므로 그 날의 다음 날이다.
        TimeEditor p = open(report(d(16), null), d(18));
        p.start.setValue("01:00(다음 날)");
        assertEquals("(목 01:00)", p.realTime.getText());
    }

    @Test
    void eTed3ClearShown() {
        assertTrue(open(task("운동", d(18)).at("10:00").task(), d(18)).clear.isVisible());
        assertTrue(open(report(d(18), "10:00"), d(18)).clear.isVisible());
        assertFalse(open(task("빨래", d(18)).task(), d(18)).clear.isVisible());
        assertFalse(open(report(d(17), "10:00"), d(18)).clear.isVisible());
    }

    @Test
    void tEdit6FixedHasNoClear() {
        assertFalse(open(task("수업", d(18)).kind(Kind.FIXED).at("13:00").minutes(75).task(), d(18)).clear.isVisible());
    }

    @Test
    void eTed4Content() {
        TimeEditor e = open(task("<b>시험</b> & 발표", d(18)).at("10:00").task(), d(18));
        assertEquals(new Insets(12, 14, 12, 14), e.panel.getInsets());
        Container fields = (Container) layout(e.panel).getLayoutComponent(BorderLayout.NORTH);
        JLabel title = (JLabel) layout(fields).getLayoutComponent(BorderLayout.NORTH);
        assertEquals("<html><b>&lt;b&gt;시험&lt;/b&gt; &amp; 발표</b></html>", title.getText());
        assertSame(e.error, layout(e.panel).getLayoutComponent(BorderLayout.CENTER));
        assertEquals(List.of(" ", Theme.SUNDAY, 12), List.of(e.error.getText(), e.error.getForeground(), e.error.getFont().getSize()));
        Container buttons = (Container) layout(e.panel).getLayoutComponent(BorderLayout.SOUTH);
        assertSame(e.clear.getParent(), layout(buttons).getLayoutComponent(BorderLayout.WEST));
        assertSame(e.save.getParent(), layout(buttons).getLayoutComponent(BorderLayout.EAST));
        assertEquals(List.of(e.cancel, e.save), List.of(e.save.getParent().getComponents()));
        assertEquals(List.of("시각 빼기", "취소", "저장"), List.of(e.clear.getText(), e.cancel.getText(), e.save.getText()));
    }

    @Test
    void eTed5EndCheck() {
        TimeEditor e = open(task("독서", d(18)).at("03:00").nextDay().minutes(60).task(), d(18));
        assertNull(e.check());
        e.minutes.setValue(65);
        assertEquals("다음 날 04:00까지 끝나야 해요", e.check());
        e.start.setValue("23:30");
        assertNull(e.check());
    }

    @Test
    void eTed6SaveAndCancel() {
        TimeEditor e = open(task("독서", d(18)).at("03:30").nextDay().minutes(60).task(), d(18));
        e.save.doClick();
        assertEquals(List.of(TaskDraft.END_PROBLEM, List.of()), List.of(e.error.getText(), calls));
        e.start.setValue("03:00(다음 날)");
        answer = false;
        e.save.doClick();
        assertEquals(List.of(e.draft()), calls);
        answer = true;
        e.save.doClick();
        assertEquals(List.of(e.draft(), e.draft(), "닫음"), calls);
        calls.clear();
        e.cancel.doClick();
        assertEquals(List.of("닫음"), calls);
    }

    @Test
    void eTed7SavedDateAndDue() {
        TaskDraft run = open(task("운동", d(20)).at("10:00").task(), d(20)).draft();
        assertEquals(List.of(d(20), LocalTime.of(10, 0), false, true), List.of(run.date(), run.startTime(), run.nextDay(), run.timeOnly()));
        // 기간 할 일은 창을 연 날로 옮기고 마감 칸은 비운다. 지난 날 줄에서 연 창은 오늘 11-18로 연다.
        TimeEditor p = open(report(d(16), null), d(19));
        p.start.setValue("01:30(다음 날)");
        TaskDraft v = p.draft();
        assertEquals(List.of(d(19), LocalTime.of(1, 30), true, 30), List.of(v.date(), v.startTime(), v.nextDay(), v.durationMinutes()));
        assertNull(v.dueDate());
        assertEquals(d(18), open(report(d(16), null), d(18)).draft().date());
    }
}
