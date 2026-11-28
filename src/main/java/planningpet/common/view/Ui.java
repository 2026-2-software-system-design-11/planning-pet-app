package planningpet.common.view;

import javax.swing.JLabel;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.JTextComponent;

/** 화면 여러 곳이 같이 쓰는 작은 부품. 입력칸이 쓰는 것만 옮겼고, 나머지는 그것을 쓰는 화면의 단위가 더한다(docs/porting/pick.md). */
public final class Ui {
    private Ui() {
    }

    /** 칸 아래에 두는 흐린 작은 안내 글. */
    public static JLabel hint(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(Theme.INK_DIM);
        l.setFont(Theme.font(12));
        return l;
    }

    /** 칸에 max자를 넘게 써지지 않게 한다(붙여 넣으면 max자에서 자른다). */
    public static void limit(JTextComponent c, int max) {
        ((AbstractDocument) c.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void replace(FilterBypass fb, int off, int len, String text, AttributeSet a) throws BadLocationException {
                int room = max - fb.getDocument().getLength() + len;
                String t = text == null ? "" : text;
                super.replace(fb, off, len, t.length() > room ? t.substring(0, Math.max(0, room)) : t, a);
            }
        });
    }
}
