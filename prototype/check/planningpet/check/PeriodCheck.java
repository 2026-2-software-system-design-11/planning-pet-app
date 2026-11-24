package planningpet.check;

import static planningpet.check.ProtoCheck.eq;

import planningpet.app.Main;
import planningpet.app.model.MockData;
import planningpet.app.model.Store;
import planningpet.app.view.App;
import planningpet.common.view.Ui;
import planningpet.home.view.MainScreen;
import planningpet.pet.model.Talk;
import planningpet.reward.model.DailyProgress;
import planningpet.reward.model.Game;
import planningpet.schedule.model.*;
import planningpet.schedule.view.*;
import planningpet.stats.model.Stats;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

/** 06 검사 표 가운데 D-64(반복)와 D-65(기간 할 일)의 줄. ProtoCheck가 부른다. */
final class PeriodCheck {
    private PeriodCheck() {
    }

    /** "11-16"은 2026년, "2027-03-15"는 그대로. */
    static LocalDate d(String s) {
        return LocalDate.parse(s.length() == 5 ? "2026-" + s : s);
    }

    static TaskDraft draft(Task.Kind kind, String date, String start) {
        return new TaskDraft(kind, "일", d(date), start == null ? null : LocalTime.parse(start), 30, "");
    }

    /** 검사 문장을 "칸: 문장"으로 잇는다. 통과면 "오류 없음". */
    static String msgs(List<String[]> p) {
        return p.isEmpty() ? "오류 없음" : p.stream().map(x -> x[0] + ": " + x[1]).collect(Collectors.joining(" / "));
    }

    static Task period(Store s, String title, String date, String due, int minutes) {
        return s.book.add(new TaskDraft(Task.Kind.TASK, title, d(date), null, minutes, "").withDue(d(due))).get(0);
    }

    static Store at(String dateTime) {
        Store s = MockData.empty();
        s.now = java.time.LocalDateTime.parse("2026-" + dateTime);
        return s;
    }

    static void run() {
        validate();
        add();
        edit();
        lists();
        rewards();
        talkAndCalendar();
        autoPlace();
    }

    static void validate() {
        TaskDraft task = draft(Task.Kind.TASK, "11-16", null);
        eq("T-VAL-11", "오류 없음", msgs(TaskCheck.check(task.withRepeat(Repeat.DAILY, d("12-13")), false)));
        eq("T-VAL-13", "오류 없음 / 반복: 반복이 너무 많아요. 끝 날짜를 앞당겨 주세요",
                msgs(TaskCheck.check(task.withRepeat(Repeat.DAILY, d("2027-03-15")), false)) + " / "
                + msgs(TaskCheck.check(task.withRepeat(Repeat.DAILY, d("2027-03-16")), false)));
        eq("D-64 요일은 하나 이상", "반복: 요일을 하나 이상 골라 주세요",
                msgs(TaskCheck.check(task.withRepeat(Repeat.weekly(List.of()), d("11-30")), false)));
        Repeat wed = Repeat.weekly(DayOfWeek.WEDNESDAY);
        eq("D-64 반복하는 날 없음", "반복: 끝 날짜 안에 반복하는 날이 없어요 / 오류 없음",
                msgs(TaskCheck.check(task.withRepeat(wed, d("11-17")), false)) + " / "
                + msgs(TaskCheck.check(task.withRepeat(wed, d("11-18")), false)));
        TaskDraft p18 = draft(Task.Kind.TASK, "11-18", null);
        eq("D-65 마감은 날짜와 같거나 뒤", "마감: 마감이 날짜보다 앞이에요 / 오류 없음 / 마감: 마감이 날짜보다 앞이에요 / 오류 없음",
                msgs(TaskCheck.check(p18.withDue(d("11-17")), false)) + " / "
                + msgs(TaskCheck.check(p18.withDue(d("11-18")), false)) + " / "
                + msgs(TaskCheck.check(p18.withDue(d("11-17")), true)) + " / "
                + msgs(TaskCheck.check(p18.withDue(d("11-17")), true, true)));
        eq("D-65 기간 할 일은 반복하지 않는다", "반복: 기간 안에 하는 할 일은 반복할 수 없어요",
                msgs(TaskCheck.check(task.withDue(d("11-22")).withRepeat(Repeat.DAILY, d("11-22")), false)));
        eq("D-65 기간은 할 일만", "마감: 기간은 할 일만 정할 수 있어요",
                msgs(TaskCheck.check(draft(Task.Kind.FIXED, "11-16", "10:00").withDue(d("11-22")), false)));
        eq("T-DEF-5", "2027-03-01", DefaultDates.repeatEnd(Task.Kind.FIXED, d("11-16")));
        eq("D-64 할 일 반복 끝 4주", "2026-12-14", DefaultDates.repeatEnd(Task.Kind.TASK, d("11-16")));
        eq("D-65 마감 기본은 그 주 일요일", "2026-11-22 2026-11-22", DefaultDates.due(d("11-18")) + " " + DefaultDates.due(d("11-22")));
    }

    /** 사본들을 "날짜 시작 seq"로 잇고, 같은 반복인지와 반복 방식을 붙인다. */
    static String copies(List<Task> made) {
