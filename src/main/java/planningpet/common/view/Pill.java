package planningpet.common.view;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import javax.swing.JToggleButton;

/**
 * 알약 모양 탭(캐릭터 창의 펫 탭과 상태, 꾸미기, 정보 탭, 나의 방의 분류 탭과 펫 칩). 고르면 민트로 채우고 흰 글자, 아니면 흰 바탕에 옅은
 * 테두리다. 그림(펫 얼굴)이나 아이콘을 글자 앞에 둘 수 있고, 글자 옆 작은 글(펫 종류)과 둘째 줄(레벨)을 둘 수 있다. docs/porting/character.md S8d.
 */
public class Pill extends JToggleButton {
    private final Icons.Kind kind;
    private final int size;
    /** 좁은 자리에서 아이콘을 빼고 글자만 보인다. */
    private boolean showIcon = true;

    public Pill(String text, Icons.Kind kind, int size) {
        super(text);
        this.kind = kind;
        this.size = size;
        setFont(Theme.bold(size));
        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setRolloverEnabled(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    /** 첫 줄의 폭. */
    private int firstLine(FontMetrics fm) {
        return fm.stringWidth(getText());
    }

    /** 글자 앞 표시(아이콘)의 폭. */
    private int mark() {
        return kind != null && showIcon ? size + 4 : 0;
    }

    public void showIcon(boolean on) {
        if (showIcon != on) {
            showIcon = on;
            revalidate();
        }
    }

    @Override
    public Dimension getPreferredSize() {
        FontMetrics fm = getFontMetrics(getFont());
        int text = firstLine(fm);
        int h = size + 18;
        int gap = mark() > 0 ? 8 : 0;
        return new Dimension(text + mark() + gap + size * 2 + 6, h);
    }
}
