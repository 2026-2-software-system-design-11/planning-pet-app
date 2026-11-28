package planningpet.schedule.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.awt.Dimension;
import java.awt.Insets;
import javax.swing.JLabel;
import org.junit.jupiter.api.Test;
import planningpet.common.view.Theme;

/** 목록 줄의 둘째 줄 표시와 "고정" 꼬리표. 번호는 docs/porting/task-rows.md S3u 2절. */
class RowMarksTest {
    private static final String GRAY = "<font color='#8A96A0'>";
    private static final String RED = "<font color='#C0392B'>";

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
        assertNull(TaskRows.marks(null, null, null));
    }

    @Test
    void eMark2DueOnly() {
        mark(TaskRows.marks("~11/22까지", null, null), GRAY + "~11/22까지</font>", 86, 15);
    }

    @Test
    void eMark3DueAndNote() {
        mark(TaskRows.marks("~11/22까지", "17일에 하려던 일", null), GRAY + "~11/22까지</font> " + GRAY + "17일에 하려던 일</font>", 167, 15);
    }

    @Test
    void eMark4DueAndTag() {
        mark(TaskRows.marks("~11/22까지", null, "오늘 마감"), GRAY + "~11/22까지</font> " + RED + "오늘 마감</font>", 132, 15);
        mark(TaskRows.marks("~11/22까지", null, "마감"), GRAY + "~11/22까지</font> " + RED + "마감</font>", 109, 15);
    }

    @Test
    void eMark5All() {
        mark(TaskRows.marks("~11/22까지", "17일에 하려던 일", "오늘 마감"),
                GRAY + "~11/22까지</font> " + GRAY + "17일에 하려던 일</font> " + RED + "오늘 마감</font>", 213, 15);
    }

    @Test
    void eMark6EmptyFront() {
        mark(TaskRows.marks(null, null, "마감"), RED + "마감</font>", 50, 15);
        mark(TaskRows.marks(null, "17일에 하려던 일", null), GRAY + "17일에 하려던 일</font>", 108, 15);
        mark(TaskRows.marks("", null, null), GRAY + "</font>", 30, 0);
    }
}
