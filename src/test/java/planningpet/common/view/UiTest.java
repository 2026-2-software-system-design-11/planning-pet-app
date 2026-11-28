package planningpet.common.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.awt.Font;
import java.awt.FontMetrics;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.text.BadLocationException;
import org.junit.jupiter.api.Test;

/** 입력칸과 칸 글의 도우미. 번호는 docs/porting/pick.md 2절, home.md S3d 2절(E-FIT). */
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

    @Test
    void eFit4EmojiAtCut() {
        assertEquals("가나\uD83D…", fit("가나" + SMILE + "다", 40));
        assertEquals("가나" + SMILE + "다", fit("가나" + SMILE + "다", 50));
        assertEquals(SMILE + "…", fit(SMILE.repeat(3), 30));
        assertEquals("\uD83D…", fit(SMILE, 5));
    }
}
