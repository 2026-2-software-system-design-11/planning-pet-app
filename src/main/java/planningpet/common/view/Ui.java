package planningpet.common.view;

import java.awt.FontMetrics;
import javax.swing.JLabel;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;
import javax.swing.text.JTextComponent;

/** 화면 여러 곳이 같이 쓰는 작은 부품. 입력칸과 칸 글이 쓰는 것만 옮겼고, 나머지는 그것을 쓰는 화면의 단위가 더한다(docs/porting/pick.md, home.md). */
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

    /**
     * 칸에 max자를 넘게 써지지 않게 한다(붙여 넣으면 max자에서 자른다). 글자는 06 읽는 법대로 코드 포인트로 세어
     * 이모지 하나가 한 자이고 반쪽만 들어가지 않는다. 프로토타입과 일부러 다름(UTF-16 단위로 셌다, docs/porting.md 4절).
     */
    public static void limit(JTextComponent c, int max) {
        ((AbstractDocument) c.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void replace(FilterBypass fb, int off, int len, String text, AttributeSet a) throws BadLocationException {
                Document d = fb.getDocument();
                String all = d.getText(0, d.getLength());
                int room = max - all.codePointCount(0, all.length()) + all.codePointCount(off, off + len);
                String t = text == null ? "" : text;
                boolean over = t.codePointCount(0, t.length()) > room;
                super.replace(fb, off, len, over ? t.substring(0, t.offsetByCodePoints(0, Math.max(0, room))) : t, a);
            }
        });
    }

    /**
     * max 폭에 들어가게 끝을 "…"로 줄인다. 끝에서 한 자씩 빼며 앞뒤 공백을 지운 글과 "…"가 들어가면 멈추고, 한 자가 남으면 폭을 넘어도
     * 멈춘다(docs/porting/home.md S3d).
     */
    public static String fit(String s, FontMetrics fm, int max) {
        if (fm.stringWidth(s) <= max) {
            return s;
        }
        String cut = s;
        while (cut.length() > 1 && fm.stringWidth(cut.strip() + "…") > max) {
            cut = cut.substring(0, cut.length() - 1);
        }
        return cut.strip() + "…";
    }
}
