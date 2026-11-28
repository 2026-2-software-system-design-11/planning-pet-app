package planningpet.schedule.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.DefaultKeyboardFocusManager;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.KeyboardFocusManager;
import java.awt.image.BufferedImage;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import planningpet.common.view.Snap;
import planningpet.common.view.Theme;

/** 목록 줄의 시각 알약. 번호는 docs/porting/task-rows.md S3s 2절. 기준 그림은 proto/pill-*.png이고 글 띠를 자홍색으로 덮었다. */
class TimePillTest {
    private static final String T = "10:00~11:00";
    private static final String WHY = "지난 날에는 시각을 정할 수 없어요";

    private static TimePill pill(String time) {
        return new TimePill(time, false, false, null);
    }

    /** w x h 알약을 그리고 글 띠(글 왼쪽 1 앞부터 폭+2, 글 위 1 위부터 높이+2)를 덮어 기준 그림과 견준다. 음수 w는 알맞은 크기다. */
    private static void looks(String name, TimePill p, int w, int h) throws IOException {
        Dimension d = w < 0 ? p.getPreferredSize() : new Dimension(w, h);
        Snap.layOut(p, d.width, d.height);
        BufferedImage img = new BufferedImage(d.width + 4, d.height + 4, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        p.paint(g);
        FontMetrics fm = p.getFontMetrics(p.getFont());
        int tw = fm.stringWidth(p.getText());
        int x = (d.width - 1 - (14 + 5 + tw)) / 2 + 19;
        int top = (d.height - 1 + fm.getAscent() - fm.getDescent()) / 2 + 1 - fm.getAscent();
        g.setColor(new Color(0xFF00FF));
        g.fillRect(x - 1, top - 1, tw + 2, fm.getAscent() + fm.getDescent() + 2);
        g.dispose();
        Snap.assertLooksLikePrototype("pill-" + name, img);
    }

    private static void looks(String name, TimePill p) throws IOException {
        looks(name, p, -1, -1);
    }

    @Test
    void eTpill1Time() throws IOException {
        TimePill p = pill(T);
        looks("time", p);
        assertEquals(T, p.getText());
        assertTrue(p.isEnabled());
        assertEquals("눌러서 시각을 바꿔요", p.getToolTipText());
        assertEquals(Cursor.HAND_CURSOR, p.getCursor().getType());
        assertEquals(T, p.getAccessibleContext().getAccessibleName());
    }

    @Test
    void eTpill2Empty() throws IOException {
        TimePill p = pill("");
        looks("empty", p);
        assertEquals("시각 정하기", p.getText());
    }

    @Test
    void eTpill3Done() throws IOException {
        for (String time : new String[] {T, ""}) {
            TimePill p = new TimePill(time, true, true, null);
            looks(time.isEmpty() ? "locked" : "done", p);
            assertFalse(p.isEnabled());
            assertNull(p.getToolTipText());
            assertEquals(Cursor.DEFAULT_CURSOR, p.getCursor().getType());
        }
    }

    @Test
    void eTpill4Locked() throws IOException {
        for (String time : new String[] {"", T}) {
            TimePill p = new TimePill(time, false, true, WHY);
            looks(time.isEmpty() ? "locked" : "done", p);
            assertFalse(p.isEnabled());
            assertEquals(WHY, p.getToolTipText());
        }
    }

    @Test
    void eTpill5Width() throws IOException {
        looks("short", pill("9:00"));
        looks("midnight", pill("23:30~24:00"));
        looks("long", pill("23:30~01:00(다음 날)"));
        assertEquals(new Dimension(115, 28), pill("9:00").getPreferredSize());
        assertEquals(new Dimension(115, 28), pill("23:30~24:00").getPreferredSize());
        assertEquals(new Dimension(115, 28), pill("").getMaximumSize());
        assertEquals(new Dimension(155, 28), pill("23:30~01:00(다음 날)").getMaximumSize());
    }

    private static TimePill over(TimePill p) {
        p.getModel().setRollover(true);
        return p;
    }

    @Test
    void eTpill6Over() throws IOException {
        looks("over", over(pill(T)));
        looks("over-empty", over(pill("")));
        TimePill done = over(new TimePill(T, true, true, null));
        assertFalse(done.getModel().isRollover());
        looks("done", done);
    }

    private static TimePill down(TimePill p, boolean armed) {
        p.getModel().setArmed(armed);
        p.getModel().setPressed(true);
        return p;
    }

    @Test
    void eTpill7Down() throws IOException {
        looks("down", down(pill(T), true));
        looks("down-empty", down(pill(""), true));
        looks("time", down(pill(T), false));
    }

    @Test
    void eTpill8KeyboardFocus() throws IOException {
        class Focus extends DefaultKeyboardFocusManager {
            void own(Component c) {
                setGlobalFocusOwner(c);
            }
        }
        Focus focus = new Focus();
        KeyboardFocusManager.setCurrentKeyboardFocusManager(focus);
        try {
            String[] names = {"focus", "focus-empty", "focus-locked"};
            TimePill[] pills = {pill(T), pill(""), new TimePill("", false, true, WHY)};
            for (int i = 0; i < pills.length; i++) {
                focus.own(pills[i]);
                looks(names[i], pills[i]);
            }
        } finally {
            KeyboardFocusManager.setCurrentKeyboardFocusManager(null);
        }
    }

    @Test
    void eTpill9OtherSizes() throws IOException {
        for (int[] s : new int[][] {{200, 40}, {60, 20}, {10, 10}, {0, 0}}) {
            looks(s[0] + "x" + s[1], pill(T), s[0], s[1]);
        }
    }

    @Test
    void eTpill10Properties() {
        TimePill p = pill(T);
        assertFalse(p.isOpaque());
        assertFalse(p.isContentAreaFilled());
        assertFalse(p.isBorderPainted());
        assertFalse(p.isFocusPainted());
        assertTrue(p.isRolloverEnabled());
        assertEquals(Theme.bold(12), p.getFont());
        assertNull(p.getIcon());
        int[] fired = {0};
        p.addActionListener(e -> fired[0]++);
        p.doClick(0);
        TimePill locked = new TimePill("", false, true, WHY);
        locked.addActionListener(e -> fired[0] += 10);
        locked.doClick(0);
        assertEquals(1, fired[0]);
    }

    /** 일정관리 목록의 글 없는 작은 시계 단추(docs/porting/schedule.md S5a). */
    @Test
    void eTpill11Icon() {
        TimePill p = TimePill.icon(false, false, null);
        assertEquals(new Dimension(26, 26), p.getPreferredSize());
        assertEquals(new Dimension(26, 26), p.getMaximumSize());
        assertEquals("", p.getText());
        assertEquals("시각 정하기", p.getAccessibleContext().getAccessibleName());
        assertEquals("시각 정하기", p.getToolTipText());
        assertEquals(Cursor.HAND_CURSOR, p.getCursor().getType());
        assertTrue(p.isEnabled());
        TimePill done = TimePill.icon(true, true, null);
        assertFalse(done.isEnabled());
        assertNull(done.getToolTipText());
        assertEquals(Cursor.DEFAULT_CURSOR, done.getCursor().getType());
        String why = "마감이 지난 일은 시각을 정할 수 없어요";
        assertEquals(why, TimePill.icon(false, true, why).getToolTipText());
    }
}
