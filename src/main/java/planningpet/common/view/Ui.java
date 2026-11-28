package planningpet.common.view;

import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;
import java.util.function.ToIntFunction;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DateFormatter;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;
import javax.swing.text.JTextComponent;

/**
 * 화면 여러 곳이 같이 쓰는 작은 부품. 입력칸과 칸 글이 쓰는 것, 툴팁의 HTML 글자 바꾸기, 일정 입력 창의 칸 도우미만 옮겼고, 나머지는 그것을
 * 쓰는 화면의 단위가 더한다(docs/porting/pick.md, home.md, task-dialog.md).
 */
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

    /** HTML 라벨과 툴팁에 넣을 글자. 할 일 제목이나 펫 말의 꺾쇠와 앰퍼샌드가 태그로 읽히지 않게 한다(docs/porting/home.md S3q). */
    public static String esc(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
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
     * 멈춘다(docs/porting/home.md S3d). 글자는 06 읽는 법대로 코드 포인트로 세어 이모지가 반쪽으로 잘리지 않는다. 프로토타입과 일부러
     * 다름(UTF-16 단위로 잘랐다, docs/porting.md 4절).
     */
    public static String fit(String s, FontMetrics fm, int max) {
        return fit(s, fm::stringWidth, max);
    }

    /**
     * fit과 같고 폭을 그리는 글꼴 자신의 글자로 잰다. JDK는 FontMetrics를 이름, 모양, 크기가 같은 글꼴끼리 나눠 써서, Linux에서 HTML 라벨의
     * 대체 글꼴 붙은 합성 글꼴이 먼저 재어지면 Pretendard에 없는 글자의 폭이 그리는 것과 달라진다(docs/porting.md 4절, #502).
     */
    public static String fit(String s, Font font, FontRenderContext frc, int max) {
        return fit(s, t -> width(t, font, frc), max);
    }

    /** font 자신의 글자로 잰 글 폭(FontMetrics.stringWidth처럼 반올림). */
    public static int width(String s, Font font, FontRenderContext frc) {
        return s.isEmpty() ? 0 : (int) (0.5 + new TextLayout(s, font, frc).getAdvance());
    }

    private static String fit(String s, ToIntFunction<String> width, int max) {
        if (width.applyAsInt(s) <= max) {
            return s;
        }
        String cut = s;
        while (cut.codePointCount(0, cut.length()) > 1 && width.applyAsInt(cut.strip() + "…") > max) {
            cut = cut.substring(0, cut.offsetByCodePoints(cut.length(), -1));
        }
        return cut.strip() + "…";
    }

    /** 한 줄에 왼쪽부터 사이 4로 놓는 판. 일정 입력 창의 칸 줄과 단추 줄이 쓴다. */
    public static JPanel flow(JComponent... parts) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        for (JComponent c : parts) {
            p.add(c);
        }
        return p;
    }

    /** 걸리는 시간 칸(D-38). 10분부터 720분, 5분 단위다. 5분 단위가 아닌 값은 받지 않아 쳐 넣으면 이전 값으로 돌아간다. */
    public static JSpinner minutes(int value) {
        return new JSpinner(new SpinnerNumberModel(value, 10, 720, 5) {
            @Override
            public void setValue(Object v) {
                if (v instanceof Number n && n.intValue() % 5 != 0) {
                    throw new IllegalArgumentException("5분 단위가 아님");
                }
                super.setValue(v);
            }
        });
    }

    /**
     * 날짜 칸(D-43). 화살표가 커서 자리(처음에는 연도) 대신 늘 하루씩 바꾸도록 DateEditor가 아닌 기본 편집기에 날짜 형식을 붙인다.
     * SpinnerDateModel의 기본 단위가 하루다. 형식에 맞지 않는 글은 칸을 벗어날 때 이전 값으로 돌아간다(05 일정 입력 창 3번).
     * 요일 글은 05 그림대로 운영체제 로캘과 관계없이 한국어다. 프로토타입과 일부러 다름(운영체제 로캘을 따랐다, docs/porting.md 4절).
     */
    public static void dayEditor(JSpinner s, String pattern) {
        JSpinner.DefaultEditor editor = new JSpinner.DefaultEditor(s);
        JFormattedTextField field = editor.getTextField();
        field.setEditable(true);
        field.setFormatterFactory(new DefaultFormatterFactory(new DateFormatter(new SimpleDateFormat(pattern, Locale.KOREAN))));
        s.setEditor(editor);
    }

    /** 날짜 칸의 날. 칸 값은 운영체제 시간대의 그날 00:00이다. */
    public static LocalDate dateOf(JSpinner s) {
        return ((Date) s.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    /** 날짜 칸에 날을 넣는다. */
    public static void setDate(JSpinner s, LocalDate d) {
        s.setValue(Date.from(d.atStartOfDay(ZoneId.systemDefault()).toInstant()));
    }
}
