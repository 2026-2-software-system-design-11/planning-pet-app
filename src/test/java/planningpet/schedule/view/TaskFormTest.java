package planningpet.schedule.view;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.junit.jupiter.api.Test;
import planningpet.common.view.Theme;

/** 일정 입력 창의 칸과 추가 창의 처음 값. 번호는 docs/porting/task-dialog.md S4f 2절. */
class TaskFormTest {
    /** 부품을 "이름" 꼴로. 줄 판은 안의 부품을 차례로, 단추는 글을, 숨은 것은 "!"를 붙인다. */
    private static String name(Component c) {
        String n = c instanceof JLabel l ? "'" + l.getText() + "'"
                : c instanceof AbstractButton b ? b.getText() : c.getClass().getSimpleName();
        if (c instanceof JPanel p) {
            List<String> parts = new ArrayList<>();
            for (Component k : p.getComponents()) {
                parts.add(name(k));
            }
            n = String.join(" ", parts);
        }
        return c.isVisible() ? n : n + "!";
    }

    private static List<String> lines(TaskForm f) {
        List<String> out = new ArrayList<>();
        Component[] cs = f.panel.getComponents();
        for (int i = 0; i < cs.length; i += 2) {
            out.add(((JLabel) cs[i]).getText() + " = " + name(cs[i + 1]));
        }
        return out;
    }

    @Test
    void eForm1LinesInPrototypeOrder() {
        TaskForm f = new TaskForm(DayOfWeek.MONDAY);
        assertEquals(List.of("종류 = 할 일 고정 일정", "제목 = JTextField", " = ''!", "유형 = JComboBox", "날짜 = JSpinner",
                "기간 = 기간 안에 하기 '반복 일정은 기간을 정할 수 없어요' '마감일' JSpinner", " = ''!",
                "시작 시각 = 정하기 JSpinner ''", " = ''!", "걸리는 시간 (분) = JSpinner ''", " = ''!", " = '안 바꾸면 30분으로 들어가요'",
                "반복 = JComboBox 월 화 수 목 금 토 일", " = '반복 끝나는 날' JSpinner", " = ''!",
                " = '기간 안에 하는 할 일은 반복할 수 없어요'", "메모 = JScrollPane", " = ''!"), lines(f));
        assertEquals(List.of("제목", "마감", "시작 시각", "걸리는 시간", "메모", "반복"), List.copyOf(f.errors.keySet()));
        assertEquals(Theme.SUNDAY, f.errors.get("메모").getForeground());
        assertEquals("날짜", f.dateLabel.getText());
        GridBagLayout g = (GridBagLayout) f.panel.getLayout();
        GridBagConstraints name = g.getConstraints(f.panel.getComponent(2));
        GridBagConstraints field = g.getConstraints(f.title);
        assertEquals(List.of(1, new Insets(3, 8, 3, 8), GridBagConstraints.WEST, GridBagConstraints.NONE),
                List.of(name.gridy, name.insets, name.anchor, name.fill));
        assertEquals(List.of(1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL),
                List.of(field.gridy, field.gridx, field.anchor, field.fill));
        assertEquals(List.of(22, 3, 22, new Insets(1, 4, 1, 4)),
                List.of(f.title.getColumns(), f.memo.getRows(), f.memo.getColumns(), f.days[6].getMargin()));
    }

    @Test
    void eForm1DaysFollowWeekStart() {
        TaskForm f = new TaskForm(DayOfWeek.SUNDAY);
        Container repeatLine = f.days[0].getParent();
        assertEquals("JComboBox 일 월 화 수 목 금 토", name(repeatLine));
        assertEquals("월", f.days[0].getText());
    }
}
