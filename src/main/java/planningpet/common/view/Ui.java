package planningpet.common.view;

import javax.swing.JLabel;

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
}
