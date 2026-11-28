package planningpet.common.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.text.BadLocationException;
import org.junit.jupiter.api.Test;

/** 입력칸과 칸 글, HTML 글자, 입력 창 칸의 도우미. 번호는 docs/porting/pick.md 2절, home.md S3d 2절(E-FIT), S3q 2절(E-ESC), task-dialog.md S4b 2절(E-UI). */
class UiTest {
    private static final String TEN = "가나다라마바사아자차";
    private static final String SMILE = "😀";

    /** max로 막은 칸. before가 있으면 막기 전에 넣고, 글 끝에 커서를 둔다. */
    private static JTextField field(int max, String before) {
        JTextField f = new JTextField(before);
        Ui.limit(f, max);
        f.setCaretPosition(f.getText().length());
        return f;
    }

    @Test
    void eHint1() {
        for (String text : new String[] {"비워 두면 나비, 바둑이로 불러요", "", "<html><b>x</b></html>"}) {
            JLabel l = Ui.hint(text);
            assertEquals(text, l.getText());
            assertEquals(Theme.INK_DIM, l.getForeground());
            assertEquals(102, l.getForeground().getAlpha());
            assertEquals(Theme.font(12), l.getFont());
            assertFalse(l.isOpaque());
        }
        assertNull(Ui.hint(null).getText());
    }

    @Test
    void eLimit1TypeOneByOne() {
        JTextField f = field(10, null);
        for (int i = 1; i <= 11; i++) {
            f.replaceSelection("가");
            assertEquals(Math.min(i, 10), f.getText().length(), i + "번째");
        }
    }

    @Test
    void eLimit2PasteAndReplace() {
        JTextField f = field(10, null);
        f.replaceSelection(TEN + "카타파");
        assertEquals(TEN, f.getText());
        f = field(10, "가나다라마바사아");
        f.replaceSelection("12345");
        assertEquals("가나다라마바사아12", f.getText());
        f = field(10, TEN);
        f.select(0, 3);
        f.replaceSelection("abcde");
        assertEquals("abc라마바사아자차", f.getText());
        f = field(10, null);
        f.setText(TEN + "카타");
        assertEquals(TEN, f.getText());
        f = field(10, null);
        f.replaceSelection(TEN);
        assertEquals(TEN, f.getText());
    }

    @Test
    void eLimit3ZeroNegativeEmptyNull() {
        for (int max : new int[] {0, -1}) {
            JTextField f = field(max, null);
            f.replaceSelection("a");
            f.setText("abc");
            assertEquals("", f.getText(), "max " + max);
        }
        JTextField f = field(10, "가나다");
        f.replaceSelection("");
        f.replaceSelection(null);
        assertEquals("가나다", f.getText());
        f.setText(null);
        assertEquals("", f.getText());
    }

    @Test
    void eLimit4AlreadyLonger() throws BadLocationException {
        JTextField f = field(10, TEN + "카타");
        f.replaceSelection("a");
        assertEquals(TEN + "카타", f.getText());
        f.getDocument().remove(11, 1);
        assertEquals(TEN + "카", f.getText());
        f.setText(TEN + "카타파하");
        assertEquals(TEN, f.getText());
    }

    @Test
    void eLimit5InsertStringIsNotFiltered() throws BadLocationException {
        JTextField f = field(10, TEN);
        f.getDocument().insertString(0, "xyz", null);
        assertEquals("xyz" + TEN, f.getText());
    }

    /** 프로토타입과 일부러 다름: 06 읽는 법대로 코드 포인트로 센다(프로토타입은 UTF-16 단위라 반쪽이 들어갔다). */
    @Test
    void eLimit6Emoji() {
        JTextField f = field(10, "가나다라마바사아자");
        f.replaceSelection(SMILE);
        assertEquals("가나다라마바사아자" + SMILE, f.getText());
        f = field(10, "가나다라마바사아자");
        f.replaceSelection(SMILE + SMILE);
        assertEquals("가나다라마바사아자" + SMILE, f.getText());
        f = field(10, null);
        f.replaceSelection(SMILE.repeat(6));
        assertEquals(SMILE.repeat(6), f.getText());
        f = field(10, null);
        f.replaceSelection(SMILE.repeat(11));
        assertEquals(SMILE.repeat(10), f.getText());
        f.replaceSelection("가");
        assertEquals(SMILE.repeat(10), f.getText());
        f = field(10, SMILE.repeat(5));
        f.select(0, 2);
        f.replaceSelection("가나다");
        assertEquals("가나다" + SMILE.repeat(4), f.getText());
    }

    /** 글꼴에 기대지 않게 UTF-16 한 단위를 폭 10으로 잰다("…"도 10, 이모지는 20). fit은 stringWidth만 쓴다. */
    private static final FontMetrics TEN_EACH = new FontMetrics(new Font(Font.DIALOG, Font.PLAIN, 10)) {
        @Override
        public int stringWidth(String s) {
            return 10 * s.length();
        }
    };

    private static String fit(String s, int max) {
        return Ui.fit(s, TEN_EACH, max);
    }

    @Test
    void eFit1Fits() {
        assertEquals("가나다", fit("가나다", 30));
        assertEquals("가나다", fit("가나다", 31));
        assertEquals("", fit("", 0));
        assertEquals(" 가 ", fit(" 가 ", 30));
    }

    @Test
    void eFit2TooWide() {
        assertEquals("가나다…", fit("가나다라마", 40));
        assertEquals("가나다…", fit("가나다라마", 49));
        assertEquals("가나…", fit("가나 다라마", 40));
        assertEquals("가나다…", fit("  가나다라마", 40));
    }

    @Test
    void eFit3VeryNarrow() {
        assertEquals("가…", fit("가나다", 5));
        assertEquals("가…", fit("가나다", 0));
        assertEquals("가…", fit("가", 5));
        assertEquals("…", fit("", -1));
        assertEquals("…", fit("   가나", 10));
    }

    /** 프로토타입과 일부러 다름: 06 읽는 법대로 코드 포인트로 자른다(프로토타입은 이모지의 앞 반쪽이 남았다). */
    @Test
    void eFit4EmojiAtCut() {
        assertEquals("가나…", fit("가나" + SMILE + "다", 40));
        assertEquals("가나" + SMILE + "다", fit("가나" + SMILE + "다", 50));
        assertEquals(SMILE + "…", fit(SMILE.repeat(3), 30));
        assertEquals(SMILE + "…", fit(SMILE, 5));
    }

    @Test
    void eEsc1() {
        assertEquals("a&lt;b&gt;&amp;c", Ui.esc("a<b>&c"));
        assertEquals("&amp;lt;", Ui.esc("&lt;"));
        assertEquals("", Ui.esc(""));
        assertEquals("'\"", Ui.esc("'\""));
        assertEquals(SMILE + "&lt;" + SMILE, Ui.esc(SMILE + "<" + SMILE));
        assertEquals("&lt;html&gt;&lt;b&gt;x&lt;/b&gt;&lt;/html&gt;", Ui.esc("<html><b>x</b></html>"));
        assertEquals("a&amp;&amp;b&gt;&gt;", Ui.esc("a&&b>>"));
        assertEquals("plain 글", Ui.esc("plain 글"));
        assertThrows(NullPointerException.class, () -> Ui.esc(null));
    }

    private static final LocalDate NOV16 = LocalDate.of(2026, 11, 16);
    private static final String WEEKDAY = "yyyy-MM-dd (E)";

    /** pattern 형식의 날짜 칸에 d를 넣는다. 요일 글은 만들 때의 기본 로캘을 따른다. */
    private static JSpinner day(String pattern, LocalDate d) {
        JSpinner s = new JSpinner(new SpinnerDateModel());
        Ui.dayEditor(s, pattern);
        Ui.setDate(s, d);
        return s;
    }

    private static JFormattedTextField editor(JSpinner s) {
        return ((JSpinner.DefaultEditor) s.getEditor()).getTextField();
    }

    /** 칸의 화살표 동작(increment, decrement). */
    private static void arrow(JSpinner s, String name) {
        s.getActionMap().get(name).actionPerformed(new ActionEvent(s, ActionEvent.ACTION_PERFORMED, name));
    }

    /** 칸 글을 typed로 바꾸고 넣어 본다. 형식 오류면 값이 그대로다. */
    private static void type(JSpinner s, String typed) {
        editor(s).setText(typed);
        try {
            editor(s).commitEdit();
        } catch (ParseException e) {
            // 칸을 벗어날 때 이전 값으로 돌아간다(05 일정 입력 창 3번).
        }
    }

    /** 기본 로캘을 locale로 두고 run을 부른 뒤 되돌린다. */
    private static void inLocale(Locale locale, Runnable run) {
        Locale before = Locale.getDefault();
        Locale.setDefault(locale);
        try {
            run.run();
        } finally {
            Locale.setDefault(before);
        }
    }

    @Test
    void eUi1Flow() {
        JLabel label = new JLabel("가");
        label.setPreferredSize(new Dimension(30, 20));
        JButton button = new JButton("x");
        button.setPreferredSize(new Dimension(50, 26));
        JPanel p = Ui.flow(label, button);
        FlowLayout f = (FlowLayout) p.getLayout();
        assertEquals(List.of(FlowLayout.LEFT, 4, 0), List.of(f.getAlignment(), f.getHgap(), f.getVgap()));
        assertEquals(List.of(label, button), List.of(p.getComponents()));
        assertTrue(p.isOpaque());
        assertEquals(new Dimension(92, 26), p.getPreferredSize());
        p.setSize(200, 30);
        p.doLayout();
        assertEquals(List.of(new Rectangle(4, 3, 30, 20), new Rectangle(38, 0, 50, 26)), List.of(label.getBounds(), button.getBounds()));
        assertEquals(new Dimension(8, 0), Ui.flow().getPreferredSize());
    }

    @Test
    void eUi2Minutes() {
        JSpinner s = Ui.minutes(30);
        SpinnerNumberModel m = (SpinnerNumberModel) s.getModel();
        assertEquals(List.of(30, 10, 720, 5), List.of(s.getValue(), m.getMinimum(), m.getMaximum(), m.getStepSize()));
        assertInstanceOf(JSpinner.NumberEditor.class, s.getEditor());
        s.setValue(10);
        assertNull(s.getPreviousValue());
        s.setValue(720);
        assertNull(s.getNextValue());
        assertEquals(715, s.getPreviousValue());
        assertEquals("5분 단위가 아님", assertThrows(IllegalArgumentException.class, () -> s.setValue(27)).getMessage());
        s.setValue(730); // 범위는 보지 않는다
        assertEquals(730, s.getValue());
        for (String typed : List.of("27", "730", "5", "45")) {
            JSpinner t = Ui.minutes(30);
            type(t, typed);
            assertEquals(typed.equals("45") ? 45 : 30, t.getValue(), typed);
        }
        assertThrows(IllegalArgumentException.class, () -> Ui.minutes(5));
        assertEquals(27, Ui.minutes(27).getValue());
    }

    @Test
    void eUi3DayEditor() {
        inLocale(Locale.US, () -> assertEquals("2026-11-16 (Mon)", editor(day(WEEKDAY, NOV16)).getText()));
        inLocale(Locale.KOREA, () -> {
            JSpinner s = day(WEEKDAY, NOV16);
            assertEquals("2026-11-16 (월)", editor(s).getText());
            assertEquals(JSpinner.DefaultEditor.class, s.getEditor().getClass());
            assertTrue(editor(s).isEditable());
            editor(s).setCaretPosition(0); // 연도 자리
            arrow(s, "increment");
            assertEquals(List.of(LocalDate.of(2026, 11, 17), "2026-11-17 (화)"), List.of(Ui.dateOf(s), editor(s).getText()));
            arrow(s, "decrement");
            arrow(s, "decrement");
            assertEquals(LocalDate.of(2026, 11, 15), Ui.dateOf(s));
            for (String typed : List.of("abc", "2026-11-20", "2026-11-20 (Fri)")) {
                Ui.setDate(s, NOV16);
                type(s, typed);
                assertEquals(NOV16, Ui.dateOf(s), typed);
            }
            type(s, "2026-11-20 (월)"); // 친 요일은 읽지 않는다
            assertEquals(List.of(LocalDate.of(2026, 11, 20), "2026-11-20 (금)"), List.of(Ui.dateOf(s), editor(s).getText()));
        });
    }

    @Test
    void eUi4DateRoundTrip() {
        JSpinner s = day("yyyy-MM-dd", NOV16);
        String[][] rows = {{"2026-11-30", "2026-12-01", "2026-11-29"}, {"2026-12-31", "2027-01-01", "2026-12-30"},
            {"2027-01-01", "2027-01-02", "2026-12-31"}, {"2028-02-28", "2028-02-29", "2028-02-27"}, {"2026-03-01", "2026-03-02", "2026-02-28"}};
        for (String[] row : rows) {
            LocalDate d = LocalDate.parse(row[0]);
            Ui.setDate(s, d);
            assertEquals(d, Ui.dateOf(s));
            arrow(s, "increment");
            assertEquals(LocalDate.parse(row[1]), Ui.dateOf(s));
            Ui.setDate(s, d);
            arrow(s, "decrement");
            assertEquals(LocalDate.parse(row[2]), Ui.dateOf(s));
        }
    }

    @Test
    void eUi5NonexistentDate() {
        inLocale(Locale.KOREA, () -> {
            JSpinner s = day(WEEKDAY, NOV16);
            type(s, "2026-11-31 (월)");
            assertEquals(LocalDate.of(2026, 12, 1), Ui.dateOf(s));
            type(s, "2026-13-01 (월)");
            assertEquals(LocalDate.of(2027, 1, 1), Ui.dateOf(s));
        });
        JSpinner plain = day("yyyy-MM-dd", NOV16);
        type(plain, "2026-02-30");
        assertEquals(LocalDate.of(2026, 3, 2), Ui.dateOf(plain));
    }

    @Test
    void eUi6TwoDigitYear() {
        JSpinner s = day("yyyy-MM-dd", NOV16);
        type(s, "26-1-5");
        assertEquals("0026-01-05", editor(s).getText());
        assertEquals(26, Ui.dateOf(s).getYear());
        assertNotEquals(LocalDate.of(26, 1, 5), Ui.dateOf(s)); // 1582년 전 달력 차이
    }
}
