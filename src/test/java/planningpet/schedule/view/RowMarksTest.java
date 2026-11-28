package planningpet.schedule.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.awt.Dimension;
import java.awt.Insets;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import javax.swing.JLabel;
import org.junit.jupiter.api.Test;
import planningpet.common.view.Snap;
import planningpet.common.view.Theme;
import planningpet.schedule.model.Category;
import planningpet.schedule.model.Kind;
import planningpet.schedule.model.Task;

/** 목록 줄의 둘째 줄 표시와 "고정" 꼬리표. 번호는 docs/porting/task-rows.md S3u 2절. 꼬리표의 기준 그림은 proto/fixtag-*.png이고 글이 없다. */
class RowMarksTest {
    private static final String GRAY = "<font color='#8A96A0'>";
    private static final String RED = "<font color='#C0392B'>";

    private static LocalDate d(int month, int day) {
        return LocalDate.of(2026, month, day);
    }

    /** date에 start(null이면 시각 없음)로 둔 할 일. due가 있으면 11-16부터 due까지의 기간 할 일이다. */
    private static Task task(Kind kind, LocalDate date, LocalTime start, LocalDate due, boolean done) {
        return new Task("a", 1, kind, "지원서", date, start, 60, "", false, Category.OTHER, null, null, due, due == null ? null : d(11, 16),
                done, done ? d(11, 18) : null, 0, 0, null, null);
    }

    /** 표시의 글과 알맞은 크기를 보고, 글꼴과 왼쪽 여백 30, 투명은 늘 같다. */
    private static void mark(JLabel l, String html, int w, int h) {
        assertEquals("<html>" + html + "</html>", l.getText());
        assertEquals(new Dimension(w, h), l.getPreferredSize());
        assertEquals(Theme.font(12), l.getFont());
        assertEquals(new Insets(0, 30, 0, 0), l.getInsets());
        assertFalse(l.isOpaque());
    }

    @Test
    void eMark1None() {
        assertNull(TaskRows.marks(null, null, null, false));
    }

    @Test
    void eMark2DueOnly() {
        mark(TaskRows.marks("~11/22까지", null, null, false), GRAY + "~11/22까지</font>", 86, 15);
    }

    @Test
    void eMark3DueAndNote() {
        mark(TaskRows.marks("~11/22까지", "17일에 하려던 일", null, false), GRAY + "~11/22까지</font> " + GRAY + "17일에 하려던 일</font>", 167, 15);
    }

    @Test
    void eMark4DueAndTag() {
        mark(TaskRows.marks("~11/22까지", null, "오늘 마감", false), GRAY + "~11/22까지</font> " + RED + "오늘 마감</font>", 132, 15);
        mark(TaskRows.marks("~11/22까지", null, "마감", false), GRAY + "~11/22까지</font> " + RED + "마감</font>", 109, 15);
    }

    @Test
    void eMark5All() {
        mark(TaskRows.marks("~11/22까지", "17일에 하려던 일", "오늘 마감", false),
                GRAY + "~11/22까지</font> " + GRAY + "17일에 하려던 일</font> " + RED + "오늘 마감</font>", 213, 15);
    }

    @Test
    void eMark6EmptyFront() {
        mark(TaskRows.marks(null, null, "마감", false), RED + "마감</font>", 50, 15);
        mark(TaskRows.marks(null, "17일에 하려던 일", null, false), GRAY + "17일에 하려던 일</font>", 108, 15);
        mark(TaskRows.marks("", null, null, false), GRAY + "</font>", 30, 0);
    }

    /** 일정관리 목록의 빨간 "지난 날"은 다른 표시 뒤다(docs/porting/schedule.md S5a). */
    @Test
    void eWrow3PastMark() {
        assertEquals("<html>" + RED + "지난 날</font></html>", TaskRows.marks(null, null, null, true).getText());
        assertEquals("<html>" + GRAY + "~11/16까지</font> " + RED + "지난 날</font></html>",
                TaskRows.marks("~11/16까지", null, null, true).getText());
        assertNull(TaskRows.marks(null, null, null, false));
    }

    @Test
    void eMiss1MissedPlan() {
        Task p = task(Kind.TASK, d(11, 17), LocalTime.of(9, 0), d(11, 22), false);
        assertNull(TaskRows.missedNote(p, d(11, 16)));
        assertNull(TaskRows.missedNote(p, d(11, 17)));
        assertEquals("17일에 하려던 일", TaskRows.missedNote(p, d(11, 18)));
        assertEquals("17일에 하려던 일", TaskRows.missedNote(p, d(11, 22)));
        assertNull(TaskRows.missedNote(p, d(11, 23)));
    }

    @Test
    void eMiss2NotMissed() {
        assertNull(TaskRows.missedNote(task(Kind.TASK, d(11, 17), LocalTime.of(9, 0), d(11, 22), true), d(11, 18)));
        assertNull(TaskRows.missedNote(task(Kind.TASK, d(11, 17), null, d(11, 22), false), d(11, 18)));
        assertNull(TaskRows.missedNote(task(Kind.TASK, d(11, 17), LocalTime.of(9, 0), null, false), d(11, 18)));
    }

    @Test
    void eMiss3MonthEdges() {
        assertEquals("30일에 하려던 일", TaskRows.missedNote(task(Kind.TASK, d(11, 30), LocalTime.of(9, 0), d(12, 3), false), d(12, 1)));
        assertEquals("1일에 하려던 일", TaskRows.missedNote(task(Kind.TASK, d(12, 1), LocalTime.of(9, 0), d(12, 3), false), d(12, 2)));
        assertNull(TaskRows.missedNote(task(Kind.TASK, d(11, 22), LocalTime.of(9, 0), d(11, 22), false), d(11, 23)));
    }

    @Test
    void eDue1DueDayList() {
        Task p = task(Kind.TASK, d(11, 16), null, d(11, 22), false);
        assertEquals("마감", TaskRows.dueTag(p, d(11, 22), d(11, 18)));
        assertEquals("오늘 마감", TaskRows.dueTag(p, d(11, 22), d(11, 22)));
    }

    @Test
    void eDue2OtherListOrPastDue() {
        Task p = task(Kind.TASK, d(11, 16), null, d(11, 22), false);
        assertNull(TaskRows.dueTag(p, d(11, 21), d(11, 18)));
        assertNull(TaskRows.dueTag(p, d(11, 18), d(11, 18)));
        assertNull(TaskRows.dueTag(p, d(11, 22), d(11, 23)));
    }

    @Test
    void eDue3DoneOrNotPeriod() {
        assertNull(TaskRows.dueTag(task(Kind.TASK, d(11, 16), null, d(11, 22), true), d(11, 22), d(11, 22)));
        assertNull(TaskRows.dueTag(task(Kind.TASK, d(11, 22), null, null, false), d(11, 22), d(11, 22)));
        assertNull(TaskRows.dueTag(task(Kind.FIXED, d(11, 22), LocalTime.of(9, 0), null, false), d(11, 22), d(11, 22)));
    }

    @Test
    void eFix1FixedTag() throws IOException {
        JLabel tag = TaskRows.fixedTag();
        assertEquals("고정", tag.getText());
        assertEquals(Theme.bold(11), tag.getFont());
        assertEquals(Theme.INK_SUB, tag.getForeground());
        assertEquals(new Insets(2, 7, 2, 7), tag.getInsets());
        assertEquals(new Dimension(34, 18), tag.getPreferredSize());
        assertFalse(tag.isOpaque());
        assertNotSame(tag, TaskRows.fixedTag());
        Snap.blank(tag);
        for (int[] s : new int[][] {{34, 18}, {60, 30}, {10, 6}, {0, 0}}) {
            Snap.assertLaidOutLooksLikePrototype("fixtag-" + s[0] + "x" + s[1], tag, s[0], s[1]);
        }
    }
}
