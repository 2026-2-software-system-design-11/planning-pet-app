package planningpet.schedule.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/** 06 26번과 docs/porting/screen-calc.md 2절의 E-CAL 경계. */
class CalendarCellTest {
    private LocalDate today = day(14);
    private final List<Task> tasks = new ArrayList<>();

    private static LocalDate day(int d) {
        return LocalDate.of(2026, 11, d);
    }

    /** 11월 date일의 일정. start는 "10:00"이고 null이면 시각 없음, doneDay가 0보다 크면 그날 끝냈다. */
    private Task put(Kind kind, String title, int date, String start, int doneDay) {
        Task t = new Task("id" + tasks.size(), tasks.size() + 1, kind, title, day(date),
                start == null ? null : LocalTime.parse(start), 30, "", false, Category.OTHER, null, null, null, null,
                doneDay > 0, doneDay > 0 ? day(doneDay) : null, 0, 0, null, null);
        tasks.add(t);
        return t;
    }

    /** "4개 진함 마감 []"처럼 칸 글, 진한지, 마감 제목. 비어 있으면 "비어 있음". */
    private String cell(int date, boolean withFixed) {
        CellPreview p = CalendarCell.preview(new TaskBook(tasks, () -> tasks.size() + 1L, () -> today), day(date),
                withFixed);
        return p.isEmpty() ? "비어 있음" : p.text() + " " + (p.open() ? "진함" : "흐림") + " 마감 " + p.due();
    }

    @Test
    void T_CAL_1_doneMixed() {
        put(Kind.TASK, "알고리즘 과제 2번", 14, "10:00", 14);
        put(Kind.TASK, "자소서 초안", 14, "14:00", 0);
        put(Kind.TASK, "토익 단어", 14, null, 0);
        put(Kind.TASK, "운동", 14, null, 0);
        assertEquals("4개 진함 마감 []", cell(14, false));
    }
}
