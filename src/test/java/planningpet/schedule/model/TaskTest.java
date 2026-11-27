package planningpet.schedule.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import planningpet.pet.model.PetKind;

/** 06 데이터 절의 Task 규칙과 docs/porting/task.md 2절의 경계. 오늘은 11-18(수)이다. */
class TaskTest {
    private static final LocalDate TODAY = LocalDate.of(2026, 11, 18);
    private static final LocalTime TEN = LocalTime.of(10, 0);

    private static Task make(Kind kind, LocalDate date, LocalTime start, boolean nextDay, int minutes) {
        return new Task("id", 1, kind, "일", date, start, minutes, "", nextDay, Category.OTHER, null, null, null,
                null, false, null, 0, 0, null, null);
    }

    /** 30분짜리 할 일이나 고정 일정. seriesId가 있으면 매일 반복 사본, due가 있으면 기간 첫날이 date인 기간 할 일이다. */
    private static Task full(Kind kind, LocalDate date, LocalTime start, String seriesId, LocalDate due,
            boolean done, LocalDate doneDay) {
        return new Task("id", 1, kind, "일", date, start, 30, "", false, Category.OTHER, seriesId,
                seriesId == null ? null : Repeat.DAILY, due, due == null ? null : date, done, doneDay, 0, 0, null, null);
    }

    private static Task period(LocalDate date, LocalTime start, LocalDate due) {
        return full(Kind.TASK, date, start, null, due, false, null);
    }

    private static Task finished(LocalDate date, LocalTime start, LocalDate due, LocalDate doneDay) {
        return full(Kind.TASK, date, start, null, due, true, doneDay);
    }

    /** 반복 사본. 고정 일정이면 10:00에 시작한다. */
    private static Task copy(Kind kind, LocalDate date, boolean done) {
        return full(kind, date, kind == Kind.FIXED ? TEN : null, "s", null, done, done ? date : null);
    }

    private static LocalDate day(int d) {
        return LocalDate.of(2026, 11, d);
    }

    @Test
    void E_TASK_1_firstMinute() {
        assertEquals(0, make(Kind.TASK, day(18), LocalTime.MIDNIGHT, false, 30).startMinute());
    }

    @Test
    void E_TASK_2_nextDayMidnight() {
        assertEquals(1440, make(Kind.TASK, day(18), LocalTime.MIDNIGHT, true, 30).startMinute());
    }

    @Test
    void E_TASK_3_latestStart() {
        assertEquals(1675, make(Kind.TASK, day(18), LocalTime.of(3, 55), true, 10).startMinute());
    }

    @Test
    void E_TASK_4_noStart() {
        Task t = make(Kind.TASK, day(18), null, false, 30);
        assertFalse(t.timed());
        assertThrows(NullPointerException.class, t::startMinute);
    }

    @Test
    void E_TASK_5_latestEnd() {
        assertEquals(1680, make(Kind.TASK, day(18), LocalTime.of(16, 0), false, 720).endMinute());
    }

    @Test
    void E_TASK_6_plainDoneOtherDay() {
        assertEquals(day(16), finished(day(16), null, null, day(17)).countDay());
    }

    @Test
    void E_TASK_7_periodNotDone() {
        assertEquals(day(20), period(day(16), null, day(20)).countDay());
    }

    @Test
    void E_TASK_8_periodDone() {
        assertEquals(day(17), finished(day(16), null, day(20), day(17)).countDay());
    }

    @Test
    void E_TASK_9_periodDoneWithoutDoneDay() {
        assertEquals(day(16), finished(day(16), null, day(20), null).countDay());
    }

    @Test
    void E_TASK_10_plannedYesterday() {
        assertTrue(period(day(17), TEN, day(20)).missed(TODAY));
    }

    @Test
    void E_TASK_11_plannedToday() {
        assertFalse(period(day(18), TEN, day(20)).missed(TODAY));
    }

    @Test
    void E_TASK_12_dueToday() {
        assertTrue(period(day(17), TEN, day(18)).missed(TODAY));
    }

    @Test
    void E_TASK_13_dueYesterday() {
        Task t = period(day(16), TEN, day(17));
        assertFalse(t.missed(TODAY));
        assertTrue(t.pastDue(TODAY));
    }

    @Test
    void E_TASK_14_noTimeSet() {
        assertFalse(period(day(16), null, day(20)).missed(TODAY));
    }

    @Test
    void E_TASK_15_doneIsNotMissed() {
        assertFalse(finished(day(17), TEN, day(20), day(17)).missed(TODAY));
    }

    @Test
    void E_TASK_16_doneIsNotPastDue() {
        assertFalse(finished(day(16), null, day(17), day(16)).pastDue(TODAY));
    }

    @Test
    void E_TASK_17_taskCopyYesterday() {
        assertTrue(copy(Kind.TASK, day(17), false).pastCopy(TODAY));
    }

    @Test
    void E_TASK_18_taskCopyToday() {
        assertFalse(copy(Kind.TASK, day(18), false).pastCopy(TODAY));
    }

    @Test
    void E_TASK_19_fixedCopyYesterday() {
        assertFalse(copy(Kind.FIXED, day(17), false).pastCopy(TODAY));
    }

    @Test
    void E_TASK_20_doneCopyYesterday() {
        assertFalse(copy(Kind.TASK, day(17), true).pastCopy(TODAY));
    }

    @Test
    void E_TASK_21_notCopyYesterday() {
        assertFalse(full(Kind.TASK, day(17), null, null, null, false, null).pastCopy(TODAY));
    }

    @Test
    void E_TASK_22_plainPastTimed() {
        assertTrue(full(Kind.TASK, day(17), TEN, null, null, false, null).onTimetable(TODAY));
    }

    @Test
    void E_TASK_23_untimed() {
        assertFalse(full(Kind.TASK, day(18), null, null, null, false, null).onTimetable(TODAY));
    }

    @Test
    void E_TASK_24_plannedLater() {
        assertTrue(period(day(19), TEN, day(20)).onTimetable(TODAY));
    }

    @Test
    void E_TASK_25_missedPlan() {
        assertFalse(period(day(17), TEN, day(20)).onTimetable(TODAY));
    }

    @Test
    void E_TASK_26_donePeriod() {
        assertFalse(finished(day(19), TEN, day(20), day(18)).onTimetable(TODAY));
    }

    @Test
    void E_TASK_27_oldPlanPastDue() {
        assertFalse(period(day(16), TEN, day(17)).onTimetable(TODAY));
    }

    @Test
    void E_TASK_28_plannedOnPastDueDay() {
        assertTrue(period(day(17), TEN, day(17)).onTimetable(TODAY));
    }

    /** 11-14부터 11-22까지 가운데 task가 보이는 날(일). */
    private static List<Integer> shown(Task task) {
        return IntStream.rangeClosed(14, 22).filter(d -> task.shownOn(day(d), TODAY)).boxed().toList();
    }

    @Test
    void E_TASK_29_plainTask() {
        assertEquals(List.of(18), shown(make(Kind.TASK, day(18), null, false, 30)));
    }

    @Test
    void E_TASK_30_periodEveryDay() {
        assertEquals(List.of(16, 17, 18, 19, 20), shown(period(day(16), null, day(20))));
    }

    @Test
    void E_TASK_31_dueBeforeDate() {
        assertEquals(List.of(20), shown(period(day(20), null, day(19))));
    }

    @Test
    void E_TASK_32_donePeriod() {
        assertEquals(List.of(17), shown(finished(day(16), null, day(20), day(17))));
    }

    @Test
    void E_TASK_33_pastDue() {
        assertEquals(List.of(17), shown(period(day(15), null, day(17))));
    }

    @Test
    void E_TASK_34_missedPlan() {
        assertEquals(List.of(18, 19, 20), shown(period(day(17), TEN, day(20))));
    }

    @Test
    void E_TASK_35_laterPlan() {
        assertEquals(List.of(19), shown(period(day(19), TEN, day(20))));
    }

    @Test
    void E_TASK_36_oldPlanPastDue() {
        assertEquals(List.of(17), shown(period(day(15), TEN, day(17))));
    }

    @Test
    void E_TASK_37_dueToday() {
        assertEquals(List.of(16, 17, 18), shown(period(day(16), null, day(18))));
    }

    /** 06 8번이 부르는 끝내기(docs/porting/rewards.md T-RWD-1, T-RWD-11). 기간 할 일의 날짜와 기간은 그대로다(D-66). */
    @Test
    void finishWritesDoneDayAndReward() {
        Task p = period(day(16), null, day(22));
        p.finish(day(17), 10, 10, PetKind.DOG);
        assertEquals("true 2026-11-17 10 10 2026-11-17 DOG 2026-11-16 2026-11-16 2026-11-22", p.isDone() + " "
                + p.getDoneDay() + " " + p.getRewardPoints() + " " + p.getRewardExp() + " " + p.getRewardDay() + " "
                + p.getRewardPet() + " " + p.getDate() + " " + p.getStartDay() + " " + p.getDueDate());
    }

    /** 하루 상한에 걸린 끝내기는 받은 날을 적지 않는다(docs/porting/rewards.md T-RWD-5). */
    @Test
    void finishWithoutRewardHasNoRewardDay() {
        Task t = make(Kind.TASK, TODAY, null, false, 30);
        t.finish(TODAY, 0, 0, null);
        assertEquals("true 2026-11-18 null", t.isDone() + " " + t.getDoneDay() + " " + t.getRewardDay());
    }

    /** 06 9번이 부르는 풀기(docs/porting/rewards.md T-UNDO-7). 기간 할 일의 날짜와 기간은 그대로다(D-66). */
    @Test
    void unfinishClearsDoneAndReward() {
        Task p = period(day(16), null, day(22));
        p.finish(day(17), 10, 10, PetKind.DOG);
        p.unfinish();
        assertEquals("false null 0 0 null null 2026-11-16 2026-11-16 2026-11-22", p.isDone() + " " + p.getDoneDay()
                + " " + p.getRewardPoints() + " " + p.getRewardExp() + " " + p.getRewardDay() + " " + p.getRewardPet()
                + " " + p.getDate() + " " + p.getStartDay() + " " + p.getDueDate());
    }
}
