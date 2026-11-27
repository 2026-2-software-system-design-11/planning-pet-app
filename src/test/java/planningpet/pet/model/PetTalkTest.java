package planningpet.pet.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static planningpet.pet.model.Situation.BRIEFING;
import static planningpet.pet.model.Situation.COMPLETED;
import static planningpet.pet.model.Situation.FIRST_RUN;
import static planningpet.pet.model.Situation.OTHER_DATE;
import static planningpet.pet.model.Situation.UNCOMPLETED;
import static planningpet.pet.model.Situation.USUAL;
import static planningpet.pet.model.Situation.WELCOME_BACK;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import planningpet.app.model.AppData;
import planningpet.reward.model.Level;
import planningpet.reward.model.Rewards;
import planningpet.schedule.model.Category;
import planningpet.schedule.model.Kind;
import planningpet.schedule.model.Repeat;
import planningpet.schedule.model.Task;

/** 06 16번과 docs/porting/pet-talk.md 2절. at은 시계("11-16"이면 10:00)와 일정, 고른 고양이(이름 petName)를 정한다. 날 32는 12-02다. */
class PetTalkTest {
    private static final ZoneId ZONE = ZoneId.systemDefault();
    private AppData data;
    private long seq;
    private String petName = "나비";

    private void at(String now, Task... tasks) {
        LocalDateTime time = LocalDateTime.parse("2026-" + (now.contains("T") ? now : now + "T10:00"));
        data = new AppData(Clock.fixed(time.atZone(ZONE).toInstant(), ZONE), List.of(tasks), List.of());
        data.addPet(new PetState(PetKind.CAT, petName));
        data.setActiveKind(PetKind.CAT);
    }

    private static LocalDate d(int day) {
        return day > 30 ? LocalDate.of(2026, 12, day - 30) : LocalDate.of(2026, 11, day);
    }

    /** 일정 하나. hour가 있으면 date의 그 시각, due가 있으면 첫날 first부터의 기간 할 일. */
    private Task make(Kind kind, String title, int date, Integer hour, Integer first, Integer due) {
        return new Task(title + seq, seq++, kind, title, d(date), hour == null ? null : LocalTime.of(hour, 0), 30, "",
                false, Category.OTHER, null, null, due == null ? null : d(due), first == null ? null : d(first), false,
                null, 0, 0, null, null);
    }

    private Task task(String title, int day) {
        return make(Kind.TASK, title, day, null, null, null);
    }

    private Task fixed(String title, int day, int hour) {
        return make(Kind.FIXED, title, day, hour, null, null);
    }

    private Task period(String title, int first, int due) {
        return make(Kind.TASK, title, first, null, first, due);
    }

    /** first~due 기간 할 일을 day의 hour 시로 정해 둔 것. */
    private Task planned(String title, int first, int due, int day, int hour) {
        return make(Kind.TASK, title, day, hour, first, due);
    }

    private static Task done(Task task, int day) {
        task.finish(d(day), 0, 0, null);
        return task;
    }

    /** 완료하고 펫 말을 돌려준다. 끝내기는 Rewards.complete로 한다. */
    private String complete(Task task) {
        return new PetTalk(data).say(COMPLETED, new Rewards(data).complete(task, data.today()), task);
    }

    /** 완료를 취소하고 펫 말을 돌려준다. 빠질 포인트와 전후 레벨은 controller가 넘길 값이다. */
    private String uncomplete(Task task) {
        int points = task.getRewardPoints();
        Level before = Level.of(data.activePet().getExp());
        new Rewards(data).uncomplete(task, data.today());
        return new PetTalk(data).say(UNCOMPLETED, points, before, Level.of(data.activePet().getExp()));
    }

    private String say(Situation situation) {
        return new PetTalk(data).say(situation);
    }

    @Test
    void T_TALK_3_firstRunNames() {
        for (String name : List.of("나비", "콩", "Max")) {
            petName = name;
            at("11-16");
            assertEquals("처음 만났네! 나는 " + name + (name.equals("콩") ? "이야" : "야") + ". 수업부터 넣어 볼까?",
                    say(FIRST_RUN));
        }
    }

    @Test
    void E_TALK_1_firstRunWithSamples() {
        at("11-16");
        data.setSamples(Set.of("x"), Map.of());
        assertEquals("처음 만났네! 나는 나비야. 예시 일정을 넣어 뒀어.", say(FIRST_RUN));
    }

    @Test
    void T_TALK_9_otherDateCountsFixed() {
        at("11-14", fixed("F1", 16, 9), fixed("F2", 16, 13));
        assertEquals("11월 16일 할 일은 2개야.", new PetTalk(data).say(OTHER_DATE, d(16)));
    }

    @Test
    void E_TALK_12_welcomeBackCount() {
        at("11-24");
        assertEquals("오랜만이야! 다시 와서 반가워.", say(WELCOME_BACK));
        at("11-24", fixed("F1", 24, 9), task("가", 24), period("나", 24, 28));
        assertEquals("오랜만이야! 다시 와서 반가워. 오늘 할 일은 2개야.", say(WELCOME_BACK));
    }

    @Test
    void E_TALK_13_otherDateNextMonthPeriod() {
        at("11-30", period("가", 30, 33), period("나", 30, 35));
        assertEquals("12월 1일은 할 일이 없어. 기간 할 일 2개가 있어.", new PetTalk(data).say(OTHER_DATE, d(31)));
    }

    @Test
    void T_TALK_1_briefingOrder() {
        at("11-16", done(task("T0", 16), 16), task("T1", 16), task("T2", 16), task("T3", 16), task("Y1", 15),
                task("Y2", 15), task("자소서 초안", 17));
        assertEquals("오늘 할 일 3개 남았어. 어제 못 끝낸 일 2개는 달력에 남아 있어. 내일 '자소서 초안'이 있어.", say(BRIEFING));
    }

    @Test
    void T_TALK_2_briefingEmpty() {
        at("11-16");
        assertEquals("오늘은 할 일이 없어. 아래 칸에 하나 적어 볼까?", say(BRIEFING));
    }

    @Test
    void T_TALK_4_longTitleDue() {
        at("11-18", period("2026 하반기 공채 자기소개서 1번 문항", 18, 20));
        assertEquals("오늘은 할 일이 없어. '2026 하반기 공채 자기…' 마감이 모레(20일)야, D-2.", say(BRIEFING));
    }

    @Test
    void T_TALK_14_periodNotCounted() {
        at("11-17", period("E사 지원서", 16, 22), period("F사 서류", 16, 18), task("과제", 16));
        assertEquals("오늘은 할 일이 없어. 어제 못 끝낸 일 1개는 달력에 남아 있어. 'F사 서류' 마감이 내일(18일)이야, D-1.",
                say(BRIEFING));
    }

    @Test
    void T_TALK_15_missedPlanOnce() {
        at("11-18", planned("E사 지원서", 16, 22, 17, 9));
        assertEquals("오늘은 할 일이 없어. 어제 하기로 한 'E사 지원서'를 못 했어. 마감은 22일이야.", say(BRIEFING));
        at("11-19", data.getBook().getTasks().get(0));
        assertEquals("오늘은 할 일이 없어. 'E사 지원서' 마감이 3일 뒤(22일)야, D-3.", say(BRIEFING));
    }

    @Test
    void T_TALK_17_dueDay() {
        at("11-22", period("E사 지원서", 16, 22));
        assertEquals("오늘 할 일 1개 남았어. 오늘이 E사 지원서 마감이야! [E사 지원서]", say(BRIEFING) + " "
                + data.getBook().dayPlan(d(22)).dueToday().stream().map(Task::getTitle).toList());
    }

    @Test
    void T_TALK_18_dueThenTomorrow() {
        at("11-18", task("빨래", 18), task("과제", 19), period("E사 지원서", 16, 20));
        assertEquals("오늘 할 일 1개 남았어. 'E사 지원서' 마감이 모레(20일)야, D-2. 내일 '과제'가 있어.", say(BRIEFING));
    }

    @Test
    void T_TALK_19_periodOnly() {
        at("11-18", period("E사 지원서", 16, 22));
        assertEquals("오늘은 할 일이 없어. E사 지원서는 22일까지야, 4일 남았어.", say(BRIEFING));
    }

    @Test
    void T_TALK_20_missedCopiesNotYesterday() {
        List<Task> copies = java.util.stream.IntStream.rangeClosed(16, 22).mapToObj(day -> new Task("운동" + day, day,
                Kind.TASK, "운동", d(day), null, 30, "", false, Category.OTHER, "s", Repeat.DAILY, null, null, false, null,
                0, 0, null, null)).toList();
        at("11-18", java.util.stream.Stream.concat(copies.stream(), java.util.stream.Stream.of(task("과제", 17)))
                .toArray(Task[]::new));
        assertEquals("오늘 할 일 1개 남았어. 어제 못 끝낸 일 1개는 달력에 남아 있어. 내일 '운동'이 있어.", say(BRIEFING));
    }

    @Test
    void T_TALK_24_plannedOnDueDay() {
        at("11-22", planned("E사 지원서", 16, 22, 22, 14));
        assertEquals("오늘 할 일 1개 남았어. 오늘이 E사 지원서 마감이야!", say(BRIEFING));
    }

    @Test
    void T_TALK_26_periodOnlyDay() {
        at("11-18", period("E사 지원서", 18, 22));
        assertEquals("11월 19일은 할 일이 없어. 기간 할 일 1개가 있어.", new PetTalk(data).say(OTHER_DATE, d(19)));
        assertEquals("오늘은 할 일이 없어. E사 지원서는 22일까지야, 4일 남았어.", say(USUAL));
    }

    @Test
    void T_TALK_27_toldNotDue() {
        at("11-21", planned("E사 지원서", 16, 22, 20, 9), period("F사 서류", 16, 23));
        assertEquals("오늘은 할 일이 없어. 어제 하기로 한 'E사 지원서'를 못 했어. 마감은 22일이야. 'F사 서류' 마감이 모레(23일)야, D-2.",
                say(BRIEFING));
    }

    @Test
    void E_TALK_2_dueInThreeDays() {
        at("11-18", period("E사 지원서", 16, 21));
        assertEquals("오늘은 할 일이 없어. 'E사 지원서' 마감이 3일 뒤(21일)야, D-3.", say(BRIEFING));
    }

    @Test
    void E_TALK_3_nearestDueFirst() {
        at("11-18", period("가", 16, 21), period("나", 16, 20), period("다", 16, 20));
        assertEquals("오늘은 할 일이 없어. '나' 마감이 모레(20일)야, D-2.", say(BRIEFING));
    }

    @Test
    void E_TALK_4_dueTodayBeforeTomorrow() {
        at("11-18", period("가", 16, 19), period("나", 16, 18));
        assertEquals("오늘 할 일 1개 남았어. 오늘이 나 마감이야!", say(BRIEFING));
    }

    @Test
    void E_TALK_5_donePeriodOnDueDay() {
        at("11-18", done(period("가", 16, 18), 18));
        assertEquals("오늘 할 일은 다 끝냈어!", say(BRIEFING));
    }

    @Test
    void E_TALK_6_dueNextMonth() {
        at("11-29", period("가", 29, 31));
        assertEquals("오늘은 할 일이 없어. '가' 마감이 모레(1일)야, D-2.", say(BRIEFING));
    }

    @Test
    void E_TALK_7_allDoneWithPeriod() {
        at("11-18", done(task("가", 18), 18), period("나", 16, 25));
        assertEquals("오늘 할 일은 다 끝냈어! 오늘 할 일은 다 끝냈어!", say(BRIEFING) + " " + say(USUAL));
    }

    @Test
    void E_TALK_8_yesterdayTasksOnly() {
        at("11-18", fixed("알바", 17, 18), period("가", 16, 17));
        assertEquals("오늘은 할 일이 없어. 어제 못 끝낸 일 1개는 달력에 남아 있어. 아래 칸에 하나 적어 볼까?", say(BRIEFING));
    }

    @Test
    void E_TALK_9_tomorrowOrder() {
        at("11-18", task("가", 19), make(Kind.TASK, "나", 19, 10, null, null), fixed("다", 19, 9),
                done(make(Kind.TASK, "라", 19, 8, null, null), 18), period("마", 19, 25));
        assertEquals("오늘은 할 일이 없어. 내일 '나'가 있어.", say(BRIEFING));
    }

    @Test
    void E_TALK_10_plannedPeriodTomorrow() {
        at("11-18", planned("E사 지원서", 16, 25, 19, 10));
        assertEquals("오늘은 할 일이 없어. 아래 칸에 하나 적어 볼까?", say(BRIEFING));
    }

    @Test
    void E_TALK_11_earliestMissedPlan() {
        at("11-18", planned("가", 16, 25, 17, 14), planned("나", 16, 25, 17, 9));
        assertEquals("오늘은 할 일이 없어. 어제 하기로 한 '나'를 못 했어. 마감은 25일이야. 가는 25일까지야, 7일 남았어.", say(BRIEFING));
    }

    @Test
    void E_TALK_14_josaOfFullTitle() {
        String title = "2026 하반기 공채 자기소개서 1번 문항";
        at("11-18", planned(title, 16, 22, 17, 9), task(title, 19));
        assertEquals("오늘은 할 일이 없어. 어제 하기로 한 '2026 하반기 공채 자기…'을 못 했어. 마감은 22일이야. 내일 '2026 하반기 공채 자기…'이 있어.",
                say(BRIEFING));
    }

    @Test
    void T_TALK_5_stageUp() {
        at("11-16", task("가", 16));
        data.activePet().setExp(230);
        assertEquals("잘했어! 레벨이 올랐어. 이제 Lv.4, 나 좀 자란 것 같아.", complete(data.getBook().getTasks().get(0)));
    }

    @Test
    void T_TALK_6_allDone() {
        at("11-16", task("가", 16));
        assertEquals("오늘 할 일을 다 끝냈어! 푹 쉬어.", complete(data.getBook().getTasks().get(0)));
    }

    @Test
    void T_TALK_7_uncompleteLevelDown() {
        at("11-16", task("가", 16));
        data.activePet().setExp(45);
        complete(data.getBook().getTasks().get(0));
        assertEquals("체크를 풀었어. -10P, 이제 Lv.1", uncomplete(data.getBook().getTasks().get(0)));
    }

    @Test
    void T_TALK_8_uncompleteCapped() {
        at("11-16", task("가", 16), task("나", 16));
        data.setRewardCount(d(16), 20);
        complete(data.getBook().getTasks().get(0));
        assertEquals("체크를 풀었어.", uncomplete(data.getBook().getTasks().get(0)));
    }

    @Test
    void T_TALK_16_allDoneWithPeriod() {
        Task laundry = task("빨래", 18);
        at("11-18", done(task("과제", 18), 18), laundry, period("E사 지원서", 16, 22));
        assertEquals("오늘 할 일 다 끝냈어! E사 지원서는 22일까지야, 4일 남았어.", complete(laundry));
    }

    @Test
    void E_TALK_15_levelUp() {
        Task first = task("가", 16);
        at("11-16", first, task("나", 16));
        data.activePet().setExp(40);
        assertEquals("잘했어! 레벨이 올랐어. 이제 Lv.2", complete(first));
    }

    @Test
    void E_TALK_16_capped() {
        Task first = task("가", 16);
        at("11-16", first, task("나", 16));
        data.setRewardCount(d(16), 20);
        assertEquals("잘했어! 오늘 보상은 다 받았지만 체크는 계속 돼.", complete(first));
    }

    @Test
    void E_TALK_17_cappedBeforeAllDone() {
        Task only = task("가", 16);
        at("11-16", only);
        data.setRewardCount(d(16), 20);
        assertEquals("잘했어! 오늘 보상은 다 받았지만 체크는 계속 돼.", complete(only));
    }

    @Test
    void E_TALK_18_completed() {
        Task first = task("가", 16);
        at("11-16", first, task("나", 16));
        assertEquals("잘했어! +10P", complete(first));
    }

    @Test
    void E_TALK_19_yesterdayAfterAllDone() {
        Task yesterday = task("어제", 15);
        at("11-16", done(task("가", 16), 16), yesterday);
        assertEquals("잘했어! +10P", complete(yesterday));
    }

    @Test
    void E_TALK_20_fixedLast() {
        Task fixed = fixed("실습", 16, 9);
        at("11-16", fixed);
        assertEquals("오늘 할 일을 다 끝냈어! 푹 쉬어.", complete(fixed));
    }

    @Test
    void E_TALK_21_uncompleteSameLevel() {
        Task first = task("가", 16);
        at("11-16", first);
        complete(first);
        assertEquals("체크를 풀었어. -10P", uncomplete(first));
    }
}
