package planningpet.schedule.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static planningpet.schedule.model.Tasks.d;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.text.JTextComponent;
import org.junit.jupiter.api.Test;
import planningpet.common.view.Theme;
import planningpet.common.view.Ui;
import planningpet.schedule.model.Category;
import planningpet.schedule.model.Kind;
import planningpet.schedule.model.Repeat;
import planningpet.schedule.model.TaskDraft;
import planningpet.schedule.model.Tasks;

/** 일정 입력 창의 칸, 추가 창의 처음 값, 칸끼리 켜고 끄기. 번호는 docs/porting/task-dialog.md S4f, S4g 2절. */
class TaskFormTest {
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 18, 10, 7);
    private static final String SMILE = new String(Character.toChars(0x1F600));

    private static TaskForm form(LocalDate date) {
        return new TaskForm(date, NOW, DayOfWeek.MONDAY);
    }

    /** 칸 글. 날짜 칸은 보이는 글이다. */
    private static String text(JSpinner s) {
        return ((JSpinner.DefaultEditor) s.getEditor()).getTextField().getText();
    }

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
        TaskForm f = form(d(20));
        assertEquals(List.of("종류 = 할 일 고정 일정", "제목 = JTextField", " = ''!", "유형 = JComboBox", "날짜 = JSpinner",
                "기간 = 기간 안에 하기 '반복 일정은 기간을 정할 수 없어요' '마감일'! JSpinner!", " = ''!",
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
        TaskForm f = new TaskForm(d(20), NOW, DayOfWeek.SUNDAY);
        Container repeatLine = f.days[0].getParent();
        assertEquals("JComboBox 일 월 화 수 목 금 토", name(repeatLine));
        assertEquals("월", f.days[0].getText());
    }

    @Test
    void eForm2AddStartValues() {
        TaskForm f = form(d(20));
        assertEquals(List.of(true, false, "", "기타", "2026-11-20 (금)", false, "2026-11-22", false, "09:00", 30, "안 함"),
                List.of(f.task.isSelected(), f.fixed.isSelected(), f.title.getText(), f.category.getSelectedItem(), text(f.date),
                        f.period.isSelected(), text(f.due), f.timed.isSelected(), f.start.getValue(), f.minutes.getValue(),
                        f.repeat.getSelectedItem()));
        for (JToggleButton b : f.days) {
            assertFalse(b.isSelected());
        }
        assertEquals("2026-12-18", text(f.repeatEnd));
        assertEquals("", f.memo.getText());
        assertTrue(f.errors.values().stream().noneMatch(Component::isVisible));
    }

    @Test
    void eForm2DefaultStart() {
        LocalDateTime[] nows = {NOW, LocalDateTime.of(2026, 11, 18, 23, 58, 30), LocalDateTime.of(2026, 11, 19, 1, 12),
            LocalDateTime.of(2026, 11, 19, 3, 57)};
        List<Object> got = new ArrayList<>();
        for (LocalDateTime now : nows) {
            got.add(new TaskForm(d(18), now, DayOfWeek.MONDAY).start.getValue());
        }
        assertEquals(List.of("10:10", "00:00(다음 날)", "01:15(다음 날)", "03:55(다음 날)"), got);
    }

    @Test
    void eForm2DueAndRepeatEnd() {
        TaskForm sunMon = form(d(22));
        TaskForm sunSun = new TaskForm(d(22), NOW, DayOfWeek.SUNDAY);
        TaskForm satSun = new TaskForm(d(21), NOW, DayOfWeek.SUNDAY);
        TaskForm yearEnd = form(d(12, 31));
        assertEquals(List.of("2026-11-29", "2026-11-28", "2026-11-28", "2027-01-03"),
                List.of(text(sunMon.due), text(sunSun.due), text(satSun.due), text(yearEnd.due)));
        assertEquals(List.of("2026-12-20", "2027-01-28"), List.of(text(sunMon.repeatEnd), text(yearEnd.repeatEnd)));
        assertEquals("2026-12-31 (목)", text(yearEnd.date));
    }

    /** 붙여 넣은 글이 칸에 남는 글자 수(코드 포인트). */
    private static int pasted(JTextComponent c, String before, String text) {
        c.replaceSelection(before);
        c.replaceSelection(text);
        String s = c.getText();
        assertFalse(Character.isHighSurrogate(s.charAt(s.length() - 1)));
        return s.codePointCount(0, s.length());
    }

    @Test
    void eForm3TitleAndMemoLimits() {
        assertEquals(List.of(100, 51, 100, 100), List.of(pasted(form(d(20)).title, "", "가".repeat(101)),
                pasted(form(d(20)).title, "", SMILE.repeat(51)), pasted(form(d(20)).title, "", SMILE.repeat(101)),
                pasted(form(d(20)).title, "가".repeat(99), SMILE)));
        assertEquals(List.of(1000, 1000, 1000), List.of(pasted(form(d(20)).memo, "", "가".repeat(1001)),
                pasted(form(d(20)).memo, "", SMILE.repeat(1000)), pasted(form(d(20)).memo, "", SMILE.repeat(1001))));
    }

    @Test
    void eForm5StartDraft() {
        assertEquals(Tasks.task("", d(20)).today(d(18)).draft(), form(d(20)).draft());
    }

    @Test
    void eForm5FilledDraft() {
        TaskForm f = new TaskForm(d(20), LocalDateTime.of(2026, 11, 19, 1, 12), DayOfWeek.MONDAY);
        f.title.setText("  면접 준비 ");
        f.category.setSelectedIndex(2);
        f.timed.setSelected(true);
        f.start.setValue("01:30(다음 날)");
        f.minutes.setValue(45);
        f.memo.setText("준비물\n이력서");
        f.repeat.setSelectedIndex(2);
        f.days[0].setSelected(true);
        f.days[2].setSelected(true);
        planningpet.common.view.Ui.setDate(f.repeatEnd, d(12, 31));
        assertEquals(new TaskDraft(Kind.TASK, "  면접 준비 ", Category.PROJECT, d(20), null, LocalTime.of(1, 30), 45,
                "준비물\n이력서", Repeat.weekly(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY), d(12, 31), true, null, d(18), false),
                f.draft());
    }

    @Test
    void eForm5KindsRepeatsAndPeriod() {
        TaskForm f = form(d(20));
        f.fixed.setSelected(true);
        f.timed.setSelected(true);
        f.start.setValue("23:55");
        f.repeat.setSelectedIndex(1);
        planningpet.common.view.Ui.setDate(f.repeatEnd, LocalDate.of(2027, 3, 5));
        TaskDraft x = f.draft();
        assertEquals(List.of(Kind.FIXED, LocalTime.of(23, 55), false, Repeat.DAILY, LocalDate.of(2027, 3, 5)),
                List.of(x.kind(), x.startTime(), x.nextDay(), x.repeat(), x.repeatEnd()));
        TaskForm p = form(d(20));
        p.period.setSelected(true);
        planningpet.common.view.Ui.setDate(p.due, d(25));
        assertEquals(List.of(d(25), "null null"), List.of(p.draft().dueDate(), p.draft().repeat() + " " + p.draft().repeatEnd()));
        TaskForm q = form(d(20));
        q.repeat.setSelectedIndex(2);
        q.start.setValue("00:00(다음 날)");
        assertEquals(List.of(Repeat.weekly(), "null false"),
                List.of(q.draft().repeat(), q.draft().startTime() + " " + q.draft().nextDay()));
        q.timed.setSelected(true);
        assertEquals(List.of(LocalTime.MIDNIGHT, true), List.of(q.draft().startTime(), q.draft().nextDay()));
    }

    @Test
    void eForm6FixedKeepsTimeOnAndPeriodOff() {
        TaskForm f = form(d(20));
        assertTrue(f.task.isEnabled() && f.fixed.isEnabled());
        f.period.doClick();
        f.fixed.doClick();
        assertEquals(List.of(true, false, true, false, false, "기간은 할 일만 정할 수 있어요", "날짜", false),
                List.of(f.timed.isSelected(), f.timed.isEnabled(), f.start.isEnabled(), f.period.isSelected(),
                        f.period.isEnabled(), f.period.getToolTipText(), f.dateLabel.getText(), f.due.isVisible()));
        f.task.doClick();
        assertEquals(List.of(true, true, true, true), List.of(f.task.isEnabled(), f.fixed.isEnabled(), f.timed.isEnabled(),
                f.period.isEnabled()));
        assertNull(f.period.getToolTipText());
    }

    @Test
    void tAdd8PeriodTurnsTimeOff() {
        TaskForm f = form(d(18));
        f.timed.doClick();
        f.start.setValue("10:00");
        f.period.doClick();
        Ui.setDate(f.due, d(22));
        assertEquals(List.of(false, false, false, "기간 할 일의 시각은 목록에서 정해요"), List.of(f.timed.isSelected(),
                f.timed.isEnabled(), f.start.isEnabled(), f.timed.getToolTipText()));
        TaskDraft x = f.draft();
        assertNull(x.startTime());
        assertEquals(d(22), x.dueDate());
    }
}
