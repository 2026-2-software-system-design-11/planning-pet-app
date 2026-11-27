package planningpet.schedule.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import planningpet.common.model.AppDay;

/** 06 17번과 docs/porting/reminders.md 2절의 E-NTF 경계. 따로 정하지 않으면 할 일은 11-16 15:00 "통계 과제"다. */
class RemindersTest {
    private static final LocalDate D16 = LocalDate.of(2026, 11, 16);
    private final List<Task> tasks = new ArrayList<>();
    private final Set<ReminderKey> sent = new HashSet<>();
    private TaskBook book;

    /** start가 null이면 시각 없음, "+"로 시작하면 다음 날 새벽 시각이다. */
    private Task put(Kind kind, String title, LocalDate date, String start, boolean done) {
        boolean next = start != null && start.startsWith("+");
        Task t = new Task("id" + tasks.size(), tasks.size() + 1, kind, title, date,
                start == null ? null : LocalTime.parse(next ? start.substring(1) : start), 30, "", next,
                Category.OTHER, null, null, null, null, done, done ? date : null, 0, 0, null, null);
        tasks.add(t);
        return t;
    }

    private Task stats() {
        return put(Kind.TASK, "통계 과제", D16, "15:00", false);
    }

    /** date의 time에 확인해 고른 알림. sent는 테스트 안에서 이어진다. */
    private List<Reminder> run(LocalDate date, String time) {
        LocalDateTime now = date.atTime(LocalTime.parse(time));
        book = new TaskBook(tasks, () -> tasks.size() + 1L, () -> AppDay.of(now));
        return Reminders.due(book, now, sent);
    }

    /** 11-16 time에 확인해 고른 알림의 내용 글. */
    private List<String> texts(String time) {
        return run(D16, time).stream().map(Reminder::text).toList();
    }

    /** 수정 창으로 t의 시작을 11-16 start로 고친다. */
    private void move(Task t, String start) {
        book.update(t, new TaskDraft(Kind.TASK, t.getTitle(), Category.OTHER, D16, null, LocalTime.parse(start), 30, "",
                null, null, false, null, null, true), EditScope.ONLY_THIS);
    }

    @Test
    void T_NTF_1_tenMinutesBefore() {
        Task t = stats();
        List<Reminder> due = run(D16, "14:50:00");
        assertEquals(List.of("통계 과제 10분 뒤 시작"), due.stream().map(Reminder::text).toList());
        assertEquals("플래닝펫", due.get(0).title());
        assertEquals(Set.of(new ReminderKey(t.getId(), D16.atTime(15, 0))), sent);
    }

    @Test
    void T_NTF_2_oneSecondTooEarly() {
        stats();
        assertEquals(List.of(), texts("14:49:59"));
        assertEquals(Set.of(), sent);
    }

    @Test
    void T_NTF_3_justOpened() {
        stats();
        assertEquals(List.of("통계 과제 4분 뒤 시작"), texts("14:56:00"));
    }

    @Test
    void T_NTF_4_alreadyStarted() {
        stats();
        assertEquals(List.of(), texts("15:00:00"));
    }

    @Test
    void T_NTF_5_done() {
        put(Kind.TASK, "통계 과제", D16, "15:00", true);
        assertEquals(List.of(), texts("14:55"));
    }

    @Test
    void T_NTF_6_alreadySent() {
        stats();
        texts("14:50:00");
        assertEquals(List.of(), texts("14:50:30"));
    }

    @Test
    void T_NTF_7_timeChanged() {
        Task t = stats();
        texts("14:50:00");
        move(t, "16:00");
        assertEquals(List.of("통계 과제 10분 뒤 시작"), texts("15:50"));
    }

    @Test
    void T_NTF_8_fixed() {
        put(Kind.FIXED, "대학수학", D16, "10:30", false);
        assertEquals(List.of("대학수학 10분 뒤 시작"), texts("10:20"));
    }

    @Test
    void T_NTF_9_noStartTime() {
        put(Kind.TASK, "통계 과제", D16, null, false);
        assertEquals(List.of(), texts("14:50"));
        assertEquals(List.of(), texts("15:00"));
    }

    @Test
    void T_NTF_10_otherDate() {
        put(Kind.TASK, "통계 과제", D16.plusDays(1), "00:05", false);
        assertEquals(List.of("통계 과제 10분 뒤 시작"), texts("23:55"));
    }

    @Test
    void T_NTF_11_nextDayStart() {
        put(Kind.TASK, "통계 과제", D16, "+00:30", false);
        assertEquals(List.of(), texts("00:20"));
        assertEquals(List.of("통계 과제 10분 뒤 시작"),
                run(D16.plusDays(1), "00:20").stream().map(Reminder::text).toList());
    }

    @Test
    void E_NTF_7_yearChanges() {
        LocalDate last = LocalDate.of(2026, 12, 31);
        put(Kind.TASK, "통계 과제", last, "+00:05", false);
        List<Reminder> due = run(last, "23:58");
        assertEquals(List.of("통계 과제 7분 뒤 시작"), due.stream().map(Reminder::text).toList());
        assertEquals(LocalDateTime.of(2027, 1, 1, 0, 5), due.get(0).start());
    }

    @Test
    void E_NTF_1_roundsUp() {
        stats();
        assertEquals(List.of("통계 과제 5분 뒤 시작"), texts("14:55:30"));
    }

    @Test
    void E_NTF_2_oneSecondBefore() {
        stats();
        assertEquals(List.of("통계 과제 1분 뒤 시작"), texts("14:59:59"));
    }

    @Test
    void E_NTF_3_placedPeriodTask() {
        tasks.add(new Task("p", 1, Kind.TASK, "통계 과제", D16, LocalTime.of(15, 0), 30, "", false, Category.OTHER,
                null, null, D16.plusDays(4), D16, false, null, 0, 0, null, null));
        assertEquals(List.of("통계 과제 5분 뒤 시작"), texts("14:55"));
    }

    @Test
    void E_NTF_4_sameStartBySeq() {
        stats();
        put(Kind.TASK, "영어 단어", D16, "15:00", false);
        put(Kind.FIXED, "조회", D16, "14:58", false);
        assertEquals(List.of("조회 8분 뒤 시작", "통계 과제 10분 뒤 시작", "영어 단어 10분 뒤 시작"), texts("14:50"));
    }

    @Test
    void E_NTF_5_repeatCopies() {
        for (int d = 0; d < 2; d++) {
            tasks.add(new Task("s" + d, d + 1, Kind.TASK, "영어 단어", D16.plusDays(d), LocalTime.of(15, 0), 30, "",
                    false, Category.OTHER, "S", Repeat.DAILY, null, null, false, null, 0, 0, null, null));
        }
        List<Reminder> due = run(D16, "14:50");
        assertEquals(1, due.size());
        assertEquals("s0", due.get(0).task().getId());
    }

    @Test
    void E_NTF_6_movedBack() {
        Task t = stats();
        texts("14:50:00");
        move(t, "16:00");
        move(t, "15:00");
        assertEquals(List.of(), texts("14:55"));
    }
}
