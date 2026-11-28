package planningpet.schedule.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.junit.jupiter.api.Test;
import planningpet.common.view.CircleButton;
import planningpet.common.view.Icons;
import planningpet.common.view.Snap;
import planningpet.common.view.Theme;
import planningpet.schedule.model.Category;
import planningpet.schedule.model.Kind;
import planningpet.schedule.model.Task;

/** 홈 목록의 할 일 한 줄. 번호는 docs/porting/task-rows.md S3v 2절. 기준 그림 proto/row-*.png는 글을 비우고 제목 칸을 숨긴 줄이다. */
class TaskRowTest {
    private static final LocalDate TODAY = LocalDate.of(2026, 11, 18);
    private final TaskRows rows = new TaskRows();

    private static Task task(Kind kind, String title, LocalTime start, int minutes, boolean done) {
        return new Task("a", 1, kind, title, TODAY, start, minutes, "", false, Category.OTHER, null, null, null, null, done,
                done ? TODAY : null, 0, 0, null, null);
    }

    /** 줄을 만들어 앱처럼 Theme.clear를 건 뒤 폭 w, 알맞은 높이로 배치한다. */
    private JPanel row(Task t, String time, LocalDate list, int w) {
        JPanel p = rows.row(t, time, list);
        Theme.clear(p);
        Snap.layOut(p, w, p.getPreferredSize().height);
        return p;
    }

    /** 자식의 종류와 자리. 칸(JPanel) 안의 자식은 [ ] 안에 잇는다. */
    private static String bounds(Container p) {
        List<String> parts = new ArrayList<>();
        for (Component c : p.getComponents()) {
            Class<?> k = c.getClass().isAnonymousClass() ? c.getClass().getSuperclass() : c.getClass();
            parts.add(k.getSimpleName() + " " + c.getX() + "," + c.getY() + " " + c.getWidth() + "x" + c.getHeight()
                    + (c instanceof JPanel in ? " [" + bounds(in) + "]" : ""));
        }
        return String.join(", ", parts);
    }

    @Test
    void eRow1MiddleCells() {
        JPanel left = TaskRows.middle(RoundCheck.style(new JCheckBox()));
        Snap.layOut(left, 24, 28);
        assertEquals("JCheckBox 0,2 24x24", bounds(left));
        assertTrue(left.isOpaque());
        JLabel minutes = new JLabel("30분");
        minutes.setPreferredSize(new Dimension(34, 20));
        JPanel east = TaskRows.middle(new TimePill("", false, false, null), minutes,
                new CircleButton(Icons.Kind.PENCIL, 28, "수정", true), new CircleButton(Icons.Kind.TRASH, 28, "삭제", true));
        assertEquals(new Dimension(217, 28), east.getPreferredSize());
        Snap.layOut(east, 217, 28);
        assertEquals("TimePill 0,0 115x28, JLabel 119,4 34x20, CircleButton 157,0 28x28, CircleButton 189,0 28x28", bounds(east));
    }

    /** 글을 비우고 제목 칸을 숨겨 그린 그림을 기준 그림과 견준다. */
    private static void looks(String name, JPanel p) throws IOException {
        Snap.blank(p);
        p.getComponent(1).setVisible(false);
        BufferedImage img = new BufferedImage(p.getWidth() + 4, p.getHeight() + 4, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        p.paint(g);
        g.dispose();
        Snap.assertLooksLikePrototype("row-" + name, img);
    }

    @Test
    void eRow1Open() throws IOException {
        JPanel p = row(task(Kind.TASK, "토익 공부", null, 30, false), "", TODAY, 480);
        assertEquals(new Dimension(303, 39), p.getPreferredSize());
        assertEquals(44, p.getMaximumSize().height);
        assertEquals(new Insets(5, 6, 6, 4), p.getInsets());
        assertEquals(0f, p.getAlignmentX());
        assertFalse(p.isOpaque());
        assertEquals("JPanel 6,5 24x28 [JCheckBox 0,2 24x24], TitleLabel 36,5 217x28, JPanel 259,5 217x28 [TimePill 0,0 115x28, "
                + "JLabel 119,4 34x20, CircleButton 157,0 28x28, CircleButton 189,0 28x28]", bounds(p));
        looks("open", p);
    }

    @Test
    void eRow5Widths() throws IOException {
        Task t = task(Kind.TASK, "토익 공부", null, 30, false);
        int[][] want = {{200, -63, -21}, {0, -263, -221}, {900, 637, 679}};
        for (int[] w : want) {
            JPanel p = row(t, "", TODAY, w[0]);
            assertEquals(w[1], p.getComponent(1).getWidth());
            assertEquals(w[2], p.getComponent(2).getX());
        }
        looks("narrow", row(t, "", TODAY, 200));
    }
}
