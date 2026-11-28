package planningpet.schedule.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import planningpet.common.view.Snap;
import planningpet.common.view.Theme;
import planningpet.schedule.model.Category;
import planningpet.schedule.model.Kind;
import planningpet.schedule.model.Task;

/** 할 일 줄의 오른쪽 버튼 메뉴. 번호는 docs/porting/task-rows.md S3y 2절. 앱처럼 Theme.install 뒤에 만든다. */
class TaskMenuTest {
    private final List<String> heard = new ArrayList<>();

    @BeforeAll
    static void theme() {
        Theme.install();
    }

    private static Task task(String title, boolean done) {
        LocalDate day = LocalDate.of(2026, 11, 18);
        return new Task(title, 1, Kind.TASK, title, day, LocalTime.of(10, 0), 60, "", false, Category.OTHER, null, null, null, null,
                done, done ? day : null, 0, 0, null, null);
    }

    private JPopupMenu menu(Task t) {
        return TaskMenu.of(e -> heard.add("수정 " + e.getTitle()), e -> heard.add("삭제 " + e.getTitle()), t);
    }

    @Test
    void eMenu1Items() {
        Task t = task("보고서", false);
        JPopupMenu m = menu(t);
        assertNotSame(m, menu(t));
        assertNull(m.getLabel());
        assertNull(m.getInvoker());
        assertEquals(false, m.isVisible());
        assertEquals(new Dimension(47, 51), m.getPreferredSize());
        Snap.layOut(m, 47, 51);
        List<String> items = new ArrayList<>();
        for (Component c : m.getComponents()) {
            JMenuItem i = (JMenuItem) c;
            items.add(i.getClass().getSimpleName() + " " + i.getText() + " " + i.getX() + "," + i.getY() + " " + i.getWidth() + "x"
                    + i.getHeight() + " " + i.getAccessibleContext().getAccessibleName());
            assertTrue(i.isEnabled() && i.getIcon() == null && i.getAccelerator() == null && i.getToolTipText() == null);
            assertEquals(Theme.font(14), i.getFont());
            assertEquals(Theme.INK, i.getForeground());
        }
        assertEquals(List.of("JMenuItem 수정 1,3 45x23 수정", "JMenuItem 삭제 1,26 45x23 삭제"), items);
    }

    @Test
    void eMenu2Click() {
        for (Task t : List.of(task("보고서", false), task("빨래", true))) {
            JPopupMenu m = menu(t);
            ((JMenuItem) m.getComponent(0)).doClick();
            ((JMenuItem) m.getComponent(1)).doClick();
        }
        assertEquals(List.of("수정 보고서", "삭제 보고서", "수정 빨래", "삭제 빨래"), heard);
    }

    @Test
    void eMenu3Looks() throws IOException {
        JPopupMenu m = menu(task("보고서", false));
        Snap.layOut(m, 47, 51);
        Snap.blank(m);
        BufferedImage img = new BufferedImage(51, 55, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        m.paint(g);
        g.dispose();
        Snap.assertLooksLikePrototype("menu", img);
    }
}
