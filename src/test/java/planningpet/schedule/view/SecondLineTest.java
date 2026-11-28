package planningpet.schedule.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.junit.jupiter.api.Test;
import planningpet.common.view.CircleButton;
import planningpet.common.view.Icons;
import planningpet.common.view.Snap;

/** 일정관리 목록 줄의 둘째 줄. 번호는 docs/porting/schedule.md S5a 2절. 줄 폭 200에서 둘째 줄 폭은 여백 6, 4를 뺀 190이다. */
class SecondLineTest {
    /** 줄이 놓을 단추 칸: 시계, 연필, 휴지통 26x26을 틈 없이 오른쪽에 붙인다. */
    private static JPanel buttons() {
        JPanel b = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        b.add(TimePill.icon(false, false, null));
        b.add(new CircleButton(Icons.Kind.PENCIL, 26, "수정", true));
        b.add(new CircleButton(Icons.Kind.TRASH, 26, "삭제", true));
        return b;
    }

    /** 표시 marks와 단추 칸으로 둘째 줄을 만들어 폭 190, 알맞은 높이로 배치한다. */
    private static JPanel line(JLabel marks, JPanel buttons) {
        JPanel line = SecondLine.of(marks, buttons);
        Snap.layOut(line, 190, line.getPreferredSize().height);
        return line;
    }

    @Test
    void eWrow6OneOrTwoLines() {
        JPanel b = buttons();
        assertEquals(new Dimension(78, 26), b.getPreferredSize());
        // 표시 86과 단추 78의 합 164는 170 이하라 한 줄이고 둘 다 높이의 가운데다.
        JLabel due = TaskRows.marks("~11/22까지", null, null, false);
        JPanel one = line(due, b);
        assertEquals(new Dimension(164, 26), one.getPreferredSize());
        assertEquals(new Rectangle(0, 5, 86, 15), due.getBounds());
        assertEquals(new Rectangle(112, 0, 78, 26), b.getBounds());
        // 표시 167과 단추의 합은 170을 넘어 표시가 위 줄, 단추가 그 아래 오른쪽이다. 표시는 줄을 바꾸지 않는다.
        JLabel both = TaskRows.marks("~11/22까지", "17일에 하려던 일", null, false);
        String html = both.getText();
        JPanel two = line(both, b = buttons());
        assertEquals(html, both.getText());
        assertEquals(new Dimension(167, 15 + 26), two.getPreferredSize());
        assertEquals(new Rectangle(0, 0, 167, 15), both.getBounds());
        assertEquals(new Rectangle(112, 15, 78, 26), b.getBounds());
        // 표시가 없으면 단추 칸만 오른쪽이다.
        JPanel none = line(null, b = buttons());
        assertEquals(1, none.getComponentCount());
        assertSame(b, none.getComponent(0));
        assertEquals(new Dimension(78, 26), none.getPreferredSize());
        assertEquals(new Rectangle(112, 0, 78, 26), b.getBounds());
    }

    @Test
    void eWrow7LongMarksWrap() {
        JLabel wide = TaskRows.marks("~11/22까지", "17일에 하려던 일", "오늘 마감", false);
        assertEquals(213, wide.getPreferredSize().width);
        String html = wide.getText();
        JPanel b = buttons();
        JPanel line = line(wide, b);
        assertEquals(html.replace("<html>", "<html><body style='width:140px'>"), wide.getText());
        // 140 폭에서 두 줄로 꺾여 높이가 30이 되지만 알맞은 폭은 줄지 않는다. 배치는 둘째 줄 폭으로 자른다.
        assertEquals(new Dimension(212, 30), wide.getPreferredSize());
        assertEquals(new Dimension(212, 30 + 26), line.getPreferredSize());
        assertEquals(new Rectangle(0, 0, 190, 30), wide.getBounds());
        assertEquals(new Rectangle(112, 30, 78, 26), b.getBounds());
    }
}
