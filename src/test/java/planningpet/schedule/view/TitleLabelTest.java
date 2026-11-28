package planningpet.schedule.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.awt.Dimension;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import planningpet.common.view.Theme;
import planningpet.schedule.model.Category;
import planningpet.schedule.model.Kind;
import planningpet.schedule.model.Task;

/** 목록 줄의 제목 칸. 번호는 docs/porting/task-rows.md S3t 2절. */
class TitleLabelTest {
    static final String LONG = "졸업 작품 기획서 마무리하고 교수님께 메일 보내기";
    private static final LocalDate DAY = LocalDate.of(2026, 11, 16);

    static TaskRows.TitleLabel label(String title, String memo, boolean done, int w, int h) {
        Task t = new Task("a", 1, Kind.TASK, title, DAY, null, 30, memo, false, Category.OTHER, null, null, null, null, done,
                done ? DAY : null, 0, 0, null, null);
        TaskRows.TitleLabel l = new TaskRows.TitleLabel(t);
        l.setSize(w, h);
        return l;
    }

    @Test
    void eTitle1Fits() {
        TaskRows.TitleLabel l = label("토익 공부", "", false, 200, 24);
        assertEquals("토익 공부", l.shown());
        assertEquals("<html>토익 공부</html>", l.getText());
        assertEquals(new Dimension(40, 24), l.getPreferredSize());
        assertEquals(new Dimension(0, 0), l.getMinimumSize());
        assertEquals(Theme.font(14), l.getFont());
        assertFalse(l.isOpaque());
    }

    @Test
    void eTitle2Long() {
        assertEquals("졸업 작품 기획서 마…", label(LONG, "", false, 120, 24).shown());
        TaskRows.TitleLabel wide = label(LONG, "", false, 600, 24);
        assertEquals(LONG, wide.shown());
        assertEquals("<html>" + LONG + "</html>", wide.getText());
    }

    @Test
    void eTitle6Html() {
        TaskRows.TitleLabel l = label("<b>A&B</b>", "", false, 200, 24);
        assertEquals("<b>A&B</b>", l.shown());
        assertEquals("<html>&lt;b&gt;A&amp;B&lt;/b&gt;</html>", l.getText());
    }

    @Test
    void eTitle7Narrow() {
        assertEquals("졸…", label(LONG, "", false, 20, 24).shown());
        assertEquals("졸…", label(LONG, "", false, 0, 24).shown());
        assertEquals("가…", label("가", "", false, 5, 24).shown());
        Task t = new Task("a", 1, Kind.TASK, "토익 공부", DAY, null, 30, "", false, Category.OTHER, null, null, null, null, false, null,
                0, 0, null, null);
        assertEquals("토…", new TaskRows.TitleLabel(t).shown());
    }
}
