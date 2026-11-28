package planningpet.common.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.text.BadLocationException;
import org.junit.jupiter.api.Test;

/** 입력칸의 도우미. 번호는 docs/porting/pick.md 2절. */
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

    @Test
    void eLimit6Emoji() {
        JTextField f = field(10, "가나다라마바사아자");
        f.replaceSelection(SMILE);
        assertEquals("가나다라마바사아자\uD83D", f.getText());
        f = field(10, "가나다라마바사아자");
        f.replaceSelection(SMILE + SMILE);
        assertEquals("가나다라마바사아자\uD83D", f.getText());
        for (int n : new int[] {6, 11}) {
            f = field(10, null);
            f.replaceSelection(SMILE.repeat(n));
            assertEquals(SMILE.repeat(5), f.getText(), n + "개");
        }
        f = field(10, SMILE.repeat(5));
        f.select(0, 2);
        f.replaceSelection("가나다");
        assertEquals("가나" + SMILE.repeat(4), f.getText());
    }
}
