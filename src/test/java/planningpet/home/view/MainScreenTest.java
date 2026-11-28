package planningpet.home.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.awt.Component;
import java.awt.Container;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import planningpet.app.controller.AppController;
import planningpet.app.model.AppData;
import planningpet.common.view.Snap;
import planningpet.common.view.Theme;
import planningpet.home.controller.HomeController;
import planningpet.pet.model.PetKind;
import planningpet.pet.model.PetState;
import planningpet.reward.controller.RewardController;
import planningpet.schedule.model.Category;
import planningpet.schedule.model.Kind;
import planningpet.schedule.model.Task;
import planningpet.schedule.model.TaskDraft;
import planningpet.schedule.view.TaskRows;

/** 홈 화면. 번호는 docs/porting/home.md S3ab 2절. */
class MainScreenTest {
    private static final ZoneId ZONE = ZoneId.systemDefault();
    private final List<Task> tasks = new ArrayList<>();
    private AppData data;

    @BeforeEach
    void install() {
        Theme.install();
    }

    @AfterEach
    void undoInstall() {
        for (Object key : List.copyOf(UIManager.getLookAndFeelDefaults().keySet())) {
            UIManager.put(key, null);
        }
        UIManager.put("swing.boldMetal", null);
    }

    private void task(String title, int month, int day, String start, int minutes, boolean done) {
        LocalDate d = LocalDate.of(2026, month, day);
        tasks.add(new Task("id" + tasks.size(), tasks.size() + 1, Kind.TASK, title, d, start == null ? null : LocalTime.parse(start),
                minutes, "", false, Category.OTHER, null, null, null, null, done, done ? d : null, 0, 0, null, null));
    }

    /** 1절의 일정: 11-14에 보고서 10:00 60분, 시각 없는 토익 공부, 끝낸 빨래, 11-18에 가. */
    private void base() {
        task("보고서", 11, 14, "10:00", 60, false);
        task("토익 공부", 11, 14, null, 30, false);
        task("빨래", 11, 14, null, 30, true);
        task("가", 11, 18, null, 30, false);
    }

    /** 오늘 11-14 15:23, 고른 고양이 나비의 홈 화면. Main처럼 데이터 변경 알림에 refresh를 건다. */
    private MainScreen screen() {
        data = new AppData(Clock.fixed(LocalDateTime.of(2026, 11, 14, 15, 23).atZone(ZONE).toInstant(), ZONE), tasks, List.of());
        data.setNextSeq(tasks.size() + 1);
        data.addPet(new PetState(PetKind.CAT, "나비"));
        data.setActiveKind(PetKind.CAT);
        AppController app = new AppController(data, null, e -> { });
        MainScreen m = new MainScreen(data, app, new HomeController(data, app), new RewardController(data, app));
        data.onChange(m::refresh);
        return m;
    }

    private static String parts(MainScreen m, int w, int h) {
        m.setSize(w, h);
        m.doLayout();
        List<String> out = new ArrayList<>();
        for (Component c : m.getComponents()) {
            out.add(at(c.getBounds()));
        }
        return String.join(" | ", out);
    }

    private static String at(Rectangle r) {
        return r.x + "," + r.y + " " + r.width + "x" + r.height;
    }

    /** c의 자리를 할 일 카드 좌표로. */
    private static String inList(MainScreen m, Component c) {
        return at(SwingUtilities.convertRectangle(c.getParent(), c.getBounds(), m.getComponent(2)));
    }

    /** 목록의 줄마다 체크와 제목, 라벨과 단추는 글. */
    private static String rows(TaskRows rows) {
        List<String> out = new ArrayList<>();
        for (Component c : rows.getComponents()) {
            if (c instanceof JLabel l) {
                out.add(l.getText());
            } else if (c instanceof AbstractButton b) {
                out.add(b.getText());
            } else {
                JCheckBox b = (JCheckBox) ((JPanel) ((JPanel) c).getComponent(0)).getComponent(0);
                out.add((b.isSelected() ? "[x] " : "[ ] ") + ((Task) b.getClientProperty(Task.class)).getTitle());
            }
        }
        return String.join(" / ", out);
    }

    private static void press(MainScreen m, int day) {
        for (Component k : m.calendar.grid.getComponents()) {
            if (k instanceof DayCell d && d.getText().matches("<html><center>(<b>)?" + day + "(</b>)?(<br>.*)?</center></html>")) {
                for (MouseListener l : d.getMouseListeners()) {
                    l.mouseClicked(new MouseEvent(d, MouseEvent.MOUSE_CLICKED, 0, 0, 5, 5, 1, false));
                }
                return;
            }
        }
        throw new AssertionError(day);
    }

    @Test
    void eScreen1Cards() {
        MainScreen m = screen();
        assertEquals(3, m.getComponentCount());
        assertSame(m.pet, m.getComponent(0));
        assertSame(m.calendar, ((Container) m.getComponent(1)).getComponent(0));
        assertEquals(new Insets(14, 16, 14, 16), ((JComponent) m.getComponent(1)).getInsets());
        assertEquals(new Insets(14, 16, 18, 16), ((JComponent) m.getComponent(2)).getInsets());
    }

    @Test
    void eScreen1Layout() {
        assertEquals("0,0 377x236 | 0,248 377x306 | 389,0 415x554", parts(screen(), 804, 554));
    }

    @Test
    void eScreen2Edges() {
        MainScreen m = screen();
        assertEquals("0,0 360x236 | 0,248 360x313 | 372,0 393x561", parts(m, 765, 561));
        assertEquals("0,0 360x236 | 0,248 360x314 | 372,0 394x562", parts(m, 766, 562));
        assertEquals("0,0 360x236 | 0,248 360x315 | 372,0 395x563", parts(m, 767, 563));
        assertEquals("0,0 759x399 | 0,411 759x541 | 771,0 846x952", parts(m, 1617, 952));
        assertEquals("0,0 760x400 | 0,412 760x541 | 772,0 846x953", parts(m, 1618, 953));
        assertEquals("0,0 760x400 | 0,412 760x788 | 772,0 1228x1200", parts(m, 2000, 1200));
    }

    @Test
    void eScreen3TinyPane() {
        MainScreen m = screen();
        assertEquals("0,0 360x236 | 0,248 360x-48 | 372,0 -72x200", parts(m, 300, 200));
        assertEquals("0,0 360x236 | 0,248 360x-248 | 372,0 -372x0", parts(m, 0, 0));
    }

    @Test
    void eScreen4ListCard() {
        base();
        MainScreen m = screen();
        data.changed();
        Snap.layOut(m, 804, 554);
        assertEquals("16,14 383x45", inList(m, m.listTitle.getParent()));
        assertEquals(new Insets(0, 0, 9, 0), ((JComponent) m.listTitle.getParent()).getInsets());
        assertEquals("16,14 286x36 " + Theme.heavy(21) + " 24 8", inList(m, m.listTitle) + " " + m.listTitle.getFont() + " "
                + m.listTitle.getIcon().getIconWidth() + " " + m.listTitle.getIconTextGap());
        assertEquals("302,14 97x36 16 6", inList(m, m.addButton) + " " + m.addButton.getIcon().getIconWidth() + " "
                + m.addButton.getIconTextGap());
        JScrollPane scroll = (JScrollPane) m.rows.getParent().getParent();
        assertEquals("16,67 383x392 20 31 " + new Insets(0, 0, 0, 0), inList(m, scroll) + " " + scroll.getVerticalScrollBarPolicy() + " "
                + scroll.getHorizontalScrollBarPolicy() + " " + scroll.getInsets());
        assertEquals("16,475 315x40 / 339,475 60x40 false " + new Insets(9, 18, 11, 18) + " / 16,521 383x15",
                inList(m, m.quick) + " / " + inList(m, m.quickButton) + " " + m.quickButton.isEnabled() + " "
                        + m.quickButton.getInsets() + " / " + inList(m, m.quickButton.getParent().getComponent(2)));
        JLabel hint = (JLabel) m.quickButton.getParent().getComponent(2);
        assertEquals("제목을 쓰고 Enter나 추가를 누르면 30분짜리 할 일로 들어가요 " + Theme.font(12) + " " + Theme.INK_SUB + " 8",
                hint.getText() + " " + hint.getFont() + " " + hint.getForeground() + " " + hint.getInsets().left);
    }

    @Test
    void eScreen5BeforeNotice() {
        base();
        MainScreen m = screen();
        assertEquals("", m.listTitle.getText());
        assertEquals(0, m.rows.getComponentCount());
        assertFalse(m.quickButton.isEnabled());
    }

    @Test
    void eScreen6Titles() {
        MainScreen m = screen();
        data.changed();
        List<String> got = new ArrayList<>(List.of(m.listTitle.getText()));
        for (int day : new int[] {15, 16, 1, 30, 18}) {
            press(m, day);
            got.add(m.listTitle.getText());
        }
        m.calendar.next.doClick();
        press(m, 1);
        got.add(m.listTitle.getText());
        m.calendar.next.doClick();
        press(m, 3);
        got.add(m.listTitle.getText());
        assertEquals(List.of("11월 14일 (토) 할 일", "11월 15일 (일) 할 일", "11월 16일 (월) 할 일", "11월 1일 (일) 할 일", "11월 30일 (월) 할 일",
                "11월 18일 (수) 할 일", "12월 1일 (화) 할 일", "1월 3일 (일) 할 일"), got);
    }

    @Test
    void eScreen7PickRedraws() {
        base();
        MainScreen m = screen();
        data.changed();
        String first = rows(m.rows);
        assertEquals("[ ] 보고서 / 시각 없는 할 일 / [ ] 토익 공부 / 끝낸 일 1개 / [x] 빨래", first);
        press(m, 18);
        assertEquals("시각 없는 할 일 / [ ] 가", rows(m.rows));
        press(m, 15);
        assertEquals("아직 일정이 없어요. 작은 계획을 추가해 보세요.", rows(m.rows));
        m.calendar.next.doClick();
        assertEquals("11월 15일 (일) 할 일 아직 일정이 없어요. 작은 계획을 추가해 보세요.", m.listTitle.getText() + " " + rows(m.rows));
        m.calendar.prev.doClick();
        press(m, 14);
        assertEquals("11월 14일 (토) 할 일 " + first, m.listTitle.getText() + " " + rows(m.rows));
    }

    @Test
    void eScreen8DataChange() {
        base();
        MainScreen m = screen();
        data.changed();
        data.getBook().add(new TaskDraft(Kind.TASK, "새 일", Category.OTHER, LocalDate.of(2026, 11, 14), null, null, 30, "", null,
                null, false, null, null, false));
        data.changed();
        assertEquals("[ ] 보고서 / 시각 없는 할 일 / [ ] 토익 공부 / [ ] 새 일 / 끝낸 일 1개 / [x] 빨래", rows(m.rows));
        DayCell cell = (DayCell) m.calendar.grid.getComponent(19);
        assertEquals("<html><center><b>14</b><br><font size='2' color='#28977F'>4개</font></center></html>", cell.getText());
    }
}
