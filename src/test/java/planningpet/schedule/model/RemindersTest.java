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

    private Task stats() {
        Task t = new Task("id" + tasks.size(), tasks.size() + 1, Kind.TASK, "통계 과제", D16, LocalTime.of(15, 0), 30, "",
                false, Category.OTHER, null, null, null, null, false, null, 0, 0, null, null);
        tasks.add(t);
        return t;
    }

    /** date의 time에 확인해 고른 알림. sent는 테스트 안에서 이어진다. */
    private List<Reminder> run(LocalDate date, String time) {
        LocalDateTime now = date.atTime(LocalTime.parse(time));
        return Reminders.due(new TaskBook(tasks, () -> tasks.size() + 1L, () -> AppDay.of(now)), now, sent);
    }

    @Test
    void T_NTF_1_tenMinutesBefore() {
        Task t = stats();
        List<Reminder> due = run(D16, "14:50:00");
        assertEquals(List.of("통계 과제 10분 뒤 시작"), due.stream().map(Reminder::text).toList());
        assertEquals("플래닝펫", due.get(0).title());
        assertEquals(Set.of(new ReminderKey(t.getId(), D16.atTime(15, 0))), sent);
    }
}
