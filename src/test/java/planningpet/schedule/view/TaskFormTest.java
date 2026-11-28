package planningpet.schedule.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static planningpet.schedule.model.Tasks.d;

import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.FontMetrics;
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
import planningpet.schedule.model.FieldError;
import planningpet.schedule.model.Kind;
import planningpet.schedule.model.Repeat;
import planningpet.schedule.model.TaskDraft;
import planningpet.schedule.model.Tasks;

/**
 * 일정 입력 창의 칸, 추가 창과 수정 창의 처음 값, 칸끼리 켜고 끄기, 손댄 값 지키기와 날짜 따라가기, 칸 아래 오류 글. 번호는
 * docs/porting/task-dialog.md S4f~S4j 2절.
 */
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
                "기간 = 기간 안에 하기 '반복 일정은 기간을 정할 수 없어요'! '마감일'! JSpinner!", " = ''!",
                "시작 시각 = 정하기 JSpinner ''", " = ''!", "걸리는 시간 (분) = JSpinner ''", " = ''!", " = '안 바꾸면 30분으로 들어가요'",
                "반복 = JComboBox 월 화 수 목 금 토 일", " = '반복 끝나는 날' JSpinner", " = ''!",
                " = '기간 안에 하는 할 일은 반복할 수 없어요'!", "메모 = JScrollPane", " = ''!"), lines(f));
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
        // 앱이 골라 준 날짜의 요일(금)을 끄고 월, 수를 고른다(D-93, S4h).
        f.days[4].doClick();
        f.days[0].doClick();
        f.days[2].doClick();
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
        q.days[4].doClick();
        q.start.setValue("00:00(다음 날)");
        assertEquals(List.of(Repeat.weekly(), "null false"),
                List.of(q.draft().repeat(), q.draft().startTime() + " " + q.draft().nextDay()));
        q.timed.setSelected(true);
        assertEquals(List.of(LocalTime.MIDNIGHT, true), List.of(q.draft().startTime(), q.draft().nextDay()));
    }

    @Test
    void eForm4SundayStartDaysPickTheirDay() {
        TaskForm f = new TaskForm(d(16), NOW, DayOfWeek.SUNDAY);
        f.repeat.setSelectedIndex(2);
        Container line = f.days[0].getParent();
        assertEquals("JComboBox 일 월 화 수 목 금 토", name(line));
        for (int k = 1; k < line.getComponentCount(); k++) {
            JToggleButton b = (JToggleButton) line.getComponent(k);
            assertTrue(b.isEnabled());
            if (b.isSelected() != (b.getText().equals("화") || b.getText().equals("일"))) {
                b.doClick();
            }
        }
        assertEquals(Repeat.weekly(DayOfWeek.TUESDAY, DayOfWeek.SUNDAY), f.draft().repeat());
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
        f.repeat.setSelectedIndex(1);
        assertEquals(List.of(false, "기간은 할 일만 정할 수 있어요"), List.of(f.periodHint.isVisible(), f.period.getToolTipText()));
        f.repeat.setSelectedIndex(0);
        f.task.doClick();
        // 고정 일정 앞의 기간 할 일로 돌아와 정하기는 꺼진 채 비활성이다(D-101, S4h).
        assertEquals(List.of(true, true, false, true), List.of(f.task.isEnabled(), f.fixed.isEnabled(), f.timed.isEnabled(),
                f.period.isEnabled()));
        assertNull(f.period.getToolTipText());
    }

    @Test
    void eForm7RepeatTurnsPeriodOff() {
        TaskForm f = form(d(20));
        assertEquals(List.of(false, false, true), List.of(f.repeatEnd.isEnabled(), f.days[0].isEnabled(), f.period.isEnabled()));
        f.repeat.setSelectedIndex(1);
        assertEquals(List.of(true, false, false, false, true), List.of(f.repeatEnd.isEnabled(), f.days[0].isEnabled(),
                f.period.isSelected(), f.period.isEnabled(), f.periodHint.isVisible()));
        f.repeat.setSelectedIndex(2);
        assertTrue(List.of(f.days).stream().allMatch(JToggleButton::isEnabled));
        f.repeat.setSelectedIndex(0);
        assertEquals(List.of(false, false, true, false), List.of(f.repeatEnd.isEnabled(), f.days[6].isEnabled(),
                f.period.isEnabled(), f.periodHint.isVisible()));
        f.period.doClick();
        assertEquals(List.of(false, true), List.of(f.repeat.isEnabled(), f.repeatHint.isVisible()));
        f.period.doClick();
        assertEquals(List.of(true, false), List.of(f.repeat.isEnabled(), f.repeatHint.isVisible()));
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

    @Test
    void eForm8ThirtyMinuteHintOnlyAtThirty() {
        TaskForm f = form(d(20));
        assertEquals(List.of(true, ""), List.of(f.minutesHint.isVisible(), f.endLabel.getText()));
        f.minutes.setValue(45);
        assertFalse(f.minutesHint.isVisible());
        f.minutes.setValue(30);
        assertTrue(f.minutesHint.isVisible());
    }

    @Test
    void tDef13NamesFollowPeriodAndRepeat() {
        TaskForm f = form(d(16));
        f.period.doClick();
        assertEquals(List.of("시작일", "마감일", true), List.of(f.dateLabel.getText(), f.dueLabel.getText(), f.due.isVisible()));
        f.period.doClick();
        f.repeat.setSelectedIndex(1);
        f.timed.doClick();
        f.start.setValue("10:00");
        f.minutes.setValue(45);
        JLabel endName = (JLabel) f.repeatEnd.getParent().getComponent(0);
        assertEquals(List.of("날짜", true, "반복 끝나는 날", "10:45에 끝나요", ""), List.of(f.dateLabel.getText(),
                f.periodHint.isVisible(), endName.getText(), f.endLabel.getText(), f.realTime.getText()));
    }

    @Test
    void tDef14NightStartShowsRealDay() {
        TaskForm f = new TaskForm(d(20), LocalDateTime.of(2026, 11, 21, 1, 28), DayOfWeek.MONDAY);
        f.timed.doClick();
        assertEquals(List.of("2026-11-20 (금)", "01:30(다음 날)", "(토 01:30)", "02:00(다음 날)에 끝나요"),
                List.of(text(f.date), f.start.getValue(), f.realTime.getText(), f.endLabel.getText()));
        f.start.setValue("02:00(다음 날)");
        Ui.setDate(f.date, d(21));
        assertEquals(List.of("(일 02:00)", "02:30(다음 날)에 끝나요"), List.of(f.realTime.getText(), f.endLabel.getText()));
    }

    @Test
    void tDef20BackFromFixedRestoresTask() {
        TaskForm f = form(d(18));
        f.fixed.doClick();
        f.task.doClick();
        assertEquals(List.of(false, true), List.of(f.timed.isSelected(), f.timed.isEnabled()));
        f.timed.doClick();
        f.start.setValue("10:00");
        f.fixed.doClick();
        f.start.setValue("11:00");
        f.task.doClick();
        assertEquals(List.of(true, "11:00"), List.of(f.timed.isSelected(), f.start.getValue()));
        f.timed.doClick();
        f.period.doClick();
        Ui.setDate(f.due, d(25));
        f.fixed.doClick();
        assertEquals(List.of(true, false, true), List.of(f.timed.isSelected(), f.timed.isEnabled(), f.start.isEnabled()));
        f.task.doClick();
        assertEquals(List.of(true, "2026-11-25", false, false), List.of(f.period.isSelected(), text(f.due),
                f.timed.isSelected(), f.timed.isEnabled()));
    }

    @Test
    void tDef21PeriodAndRepeatRestoreFields() {
        TaskForm f = form(d(18));
        f.timed.doClick();
        f.start.setValue("10:00");
        f.period.doClick();
        f.period.doClick();
        assertEquals(List.of(true, "10:00"), List.of(f.timed.isSelected(), f.start.getValue()));
        f.period.doClick();
        Ui.setDate(f.due, d(25));
        f.fixed.doClick();
        f.repeat.setSelectedIndex(1);
        f.task.doClick();
        assertEquals(List.of(false, false), List.of(f.period.isSelected(), f.period.isEnabled()));
        f.repeat.setSelectedIndex(0);
        assertEquals(List.of(true, "2026-11-25", false, false), List.of(f.period.isSelected(), text(f.due),
                f.timed.isSelected(), f.timed.isEnabled()));
        f.period.doClick();
        assertEquals(List.of(true, "10:00"), List.of(f.timed.isSelected(), f.start.getValue()));
    }

    /** 11-17 14:12에 연 11-17 추가 창. */
    private static TaskForm at1412() {
        return new TaskForm(d(17), LocalDateTime.of(2026, 11, 17, 14, 12), DayOfWeek.MONDAY);
    }

    /** 앱 시계를 11-17 14:41로 옮긴다. */
    private static TaskForm at1441(TaskForm f) {
        f.clock = () -> LocalDateTime.of(2026, 11, 17, 14, 41);
        return f;
    }

    @Test
    void tDef23UntouchedStartFollowsDate() {
        TaskForm f = at1412();
        List<Object> got = new ArrayList<>(List.of(f.start.getValue()));
        Ui.setDate(f.date, d(18));
        got.add(f.start.getValue());
        Ui.setDate(f.date, d(17));
        got.add(f.start.getValue());
        f.start.setValue("15:00");
        Ui.setDate(f.date, d(18));
        got.add(f.start.getValue());
        assertEquals(List.of("14:15", "09:00", "14:15", "15:00"), got);
    }

    @Test
    void tDef23StartTakesClockWhenDateChangesOrFieldOpens() {
        TaskForm f = at1441(at1412());
        f.title.setText("빨래");
        f.minutes.setValue(45);
        assertEquals("14:15", f.start.getValue());
        Ui.setDate(f.date, d(18));
        Ui.setDate(f.date, d(17));
        assertEquals("14:45", f.start.getValue());
        TaskForm timedLater = at1441(at1412());
        timedLater.timed.doClick();
        TaskForm timedFirst = at1412();
        timedFirst.timed.doClick();
        at1441(timedFirst).title.setText("운동");
        TaskForm fixedLater = at1441(at1412());
        fixedLater.fixed.doClick();
        TaskForm periodOff = at1412();
        periodOff.timed.doClick();
        periodOff.period.doClick();
        at1441(periodOff).period.doClick();
        assertEquals(List.of("14:45", "14:15", "14:45", "14:45"), List.of(timedLater.start.getValue(),
                timedFirst.start.getValue(), fixedLater.start.getValue(), periodOff.start.getValue()));
    }

    /** 골라진 요일 단추의 글. 월요일부터다. */
    private static String picked(TaskForm f) {
        return String.join(", ", List.of(f.days).stream().filter(JToggleButton::isSelected).map(JToggleButton::getText).toList());
    }

    @Test
    void tDef19PickedDaysStay() {
        TaskForm f = form(d(18));
        f.repeat.setSelectedIndex(2);
        List<String> got = new ArrayList<>(List.of(picked(f)));
        Ui.setDate(f.date, d(19));
        got.add(picked(f));
        f.days[3].doClick();
        f.days[1].doClick();
        f.days[3].doClick();
        Ui.setDate(f.date, d(20));
        got.add(picked(f));
        for (int how : new int[] {1, 2, 0, 2}) {
            f.repeat.setSelectedIndex(how);
        }
        got.add(picked(f));
        assertEquals(List.of("수", "목", "화, 목", "화, 목"), got);
        f.days[1].doClick();
        f.days[3].doClick();
        f.repeat.setSelectedIndex(1);
        f.repeat.setSelectedIndex(2);
        assertEquals("", picked(f));
        assertTrue(f.draft().validate().contains(new FieldError("반복", "요일을 하나 이상 골라 주세요")));
        TaskForm g = form(d(18));
        g.repeat.setSelectedIndex(2);
        g.days[2].doClick();
        g.days[2].doClick();
        Ui.setDate(g.date, d(19));
        assertEquals("수", picked(g));
    }

    /** 저장하면 칸 아래에 보일 그 칸의 오류 글. 없으면 빈 글. */
    private static String saveError(TaskForm f, String field) {
        return f.draft().validate().stream().filter(e -> e.field().equals(field)).map(FieldError::message).findFirst().orElse("");
    }

    @Test
    void tDef12RepeatEndFollowsUntilTouched() {
        TaskForm f = form(d(16));
        f.repeat.setSelectedIndex(1);
        Ui.setDate(f.date, d(12, 1));
        assertEquals("2026-12-29", text(f.repeatEnd));
        Ui.setDate(f.repeatEnd, d(12, 10));
        Ui.setDate(f.date, d(12, 20));
        assertEquals(List.of("2026-12-10", "반복 끝나는 날이 날짜보다 전이에요"), List.of(text(f.repeatEnd), saveError(f, "반복")));
    }

    @Test
    void eForm14RepeatEndFollowsKind() {
        TaskForm f = form(d(16));
        f.repeat.setSelectedIndex(1);
        f.fixed.doClick();
        assertEquals("2027-03-01", text(f.repeatEnd));
        f.task.doClick();
        assertEquals("2026-12-14", text(f.repeatEnd));
    }

    @Test
    void tDef16PickedDueStays() {
        TaskForm f = form(d(16));
        f.period.doClick();
        Ui.setDate(f.due, d(12, 10));
        f.fixed.doClick();
        f.task.doClick();
        assertEquals(List.of(true, "2026-12-10"), List.of(f.period.isSelected(), text(f.due)));
        Ui.setDate(f.date, d(12, 12));
        assertEquals(List.of("2026-12-10", "마감일이 시작일보다 전이에요"), List.of(text(f.due), saveError(f, "마감")));
        TaskForm g = form(d(16));
        g.period.doClick();
        g.fixed.doClick();
        g.task.doClick();
        assertEquals("2026-11-22", text(g.due));
    }

    @Test
    void tDef18UnpickedDueFollowsWeek() {
        TaskForm f = form(d(16));
        f.title.setText("보고서");
        f.period.doClick();
        Ui.setDate(f.date, d(23));
        assertEquals(List.of("2026-11-29", List.of()), List.of(text(f.due), f.draft().validate()));
        Ui.setDate(f.date, d(25));
        assertEquals(List.of("2026-11-29", List.of()), List.of(text(f.due), f.draft().validate()));
    }

    /** 11-18에 연 창. 빈 제목, 정하기를 켜고 "03:30(다음 날)", 40분(T-VAL-14). */
    private static TaskForm lateStart() {
        TaskForm f = form(d(18));
        f.timed.doClick();
        f.start.setValue(Times.value(27 * 60 + 30));
        f.minutes.setValue(40);
        return f;
    }

    @Test
    void tVal14ErrorsInFieldOrder() {
        TaskForm f = lateStart();
        assertEquals("", f.errorText());
        f.showErrors(f.draft().validate());
        assertEquals("제목을 써 주세요 / 다음 날 04:00까지 끝나야 해요", f.errorText());
        assertEquals(List.of(true, false, true, false, false, false), f.errors.values().stream().map(JLabel::isVisible).toList());
    }

    /** 06 T-VAL-21의 27분 시작 시각 글. */
    private static final String TWO_PROBLEMS = "다음 날 04:00까지 끝나야 해요. 5분 단위로 정해 주세요. 걸리는 시간 25분이면 03:35(다음 날)이 맞아요";

    /** 칸을 놓은 11-16 창에 T-VAL-21의 값(시작 03:53(다음 날), 27분)을 저장한 오류 글을 보인다. */
    private static TaskForm twoProblems() {
        TaskForm f = form(d(16));
        f.panel.setSize(f.panel.getPreferredSize());
        f.panel.doLayout();
        f.showErrors(Tasks.task("일", d(16)).at("03:53").nextDay().minutes(27).draft().validate());
        return f;
    }

    @Test
    void tVal21LongErrorWrapsInTitleWidth() {
        TaskForm f = twoProblems();
        JLabel l = f.errors.get("시작 시각");
        for (Font font : new Font[] {l.getFont(), new Font(Font.DIALOG, Font.PLAIN, 12), new Font(Font.DIALOG, Font.BOLD, 16)}) {
            l.setFont(font);
            f.showErrors(Tasks.task("일", d(16)).at("03:53").nextDay().minutes(27).draft().validate());
            List<String> lines = List.of(l.getText().replaceAll("^<html><font[^>]*>|</font></html>$", "").split("<br>"));
            FontMetrics fm = l.getFontMetrics(font);
            assertTrue(lines.size() > 1, font.toString());
            assertTrue(lines.stream().allMatch(line -> fm.stringWidth(line) <= f.title.getWidth()), lines.toString());
            assertEquals(TWO_PROBLEMS, String.join(" ", lines));
        }
    }

    @Test
    void eForm10ErrorKeepsSentence() {
        TaskForm f = twoProblems();
        assertEquals(List.of(TWO_PROBLEMS, ""), List.of(f.error("시작 시각"), f.error("제목")));
        assertEquals(TWO_PROBLEMS + " / 10분부터 720분까지, 5분 단위예요", f.errorText());
        assertEquals("10분부터 720분까지, 5분 단위예요", f.errors.get("걸리는 시간").getText());
    }

    @Test
    void eForm9FixedFieldsClearAfterSave() {
        TaskForm f = lateStart();
        f.showErrors(f.draft().validate());
        f.title.setText("운동");
        assertEquals(List.of("다음 날 04:00까지 끝나야 해요", false), List.of(f.errorText(), f.errors.get("제목").isVisible()));
        f.minutes.setValue(30);
        assertEquals(List.of("", false), List.of(f.errorText(), f.errors.get("시작 시각").isVisible()));
    }

    /** 11-18에 연 창에서 "보고서", 기간을 켜고 picked면 마감 11-25를 고른 뒤 고정 일정에서 날짜 11-30, 할 일로 돌아옴(T-DEF-22). */
    private static TaskForm movedPastDue(boolean picked) {
        TaskForm f = form(d(18));
        f.title.setText("보고서");
        f.period.doClick();
        if (picked) {
            Ui.setDate(f.due, d(25));
        }
        f.fixed.doClick();
        Ui.setDate(f.date, d(30));
        f.task.doClick();
        return f;
    }

    @Test
    void tDef22DueCheckedBeforeSave() {
        TaskForm f = movedPastDue(true);
        assertEquals(List.of("2026-11-25", "마감일이 시작일보다 전이에요"), List.of(text(f.due), f.errorText()));
        Ui.setDate(f.due, d(12, 1));
        assertEquals(List.of("", false), List.of(f.errorText(), f.errors.get("마감").isVisible()));
        Ui.setDate(f.date, d(12, 2));
        assertEquals("마감일이 시작일보다 전이에요", f.errorText());
        TaskForm g = movedPastDue(false);
        assertEquals(List.of("2026-12-06", ""), List.of(text(g.due), g.errorText()));
        TaskForm h = form(d(18));
        h.period.doClick();
        Ui.setDate(h.due, d(20));
        Ui.setDate(h.date, d(23));
        assertEquals("마감일이 시작일보다 전이에요", h.errorText());
    }

    @Test
    void tVal23DueBeforeToday() {
        TaskForm f = form(d(10));
        f.period.doClick();
        assertEquals(List.of("2026-11-15", "마감일이 오늘보다 전이에요"), List.of(text(f.due), f.errorText()));
        Ui.setDate(f.due, d(17));
        assertEquals("마감일이 오늘보다 전이에요", f.errorText());
        Ui.setDate(f.due, d(9));
        assertEquals("마감일이 시작일보다 전이에요", f.errorText());
        Ui.setDate(f.due, d(18));
        assertEquals("", f.errorText());
    }

    @Test
    void eForm15Relayout() {
        TaskForm f = form(d(18));
        int[] runs = new int[1];
        f.relayout = () -> runs[0]++;
        f.title.setText("운동");
        assertEquals(0, runs[0]);
        f.period.doClick();
        assertEquals(1, runs[0]);
        f.period.doClick();
        f.title.setText("");
        assertEquals(2, runs[0]);
        f.showErrors(f.draft().validate());
        assertEquals(3, runs[0]);
        f.title.setText("운동");
        assertEquals(4, runs[0]);
    }

    private static TaskForm edit(Tasks.Spec spec) {
        return new TaskForm(spec.task(), NOW, DayOfWeek.MONDAY);
    }

    @Test
    void eForm11EditStartValues() {
        TaskForm f = edit(Tasks.task("운동", d(20)).at("10:00").minutes(45).category(Category.EXERCISE).memo("스트레칭"));
        assertEquals(List.of(true, "운동", "운동", "2026-11-20 (금)", false, true, "10:00", 45, "스트레칭"),
                List.of(f.task.isSelected(), f.title.getText(), f.category.getSelectedItem(), text(f.date), f.period.isSelected(),
                        f.timed.isSelected(), f.start.getValue(), f.minutes.getValue(), f.memo.getText()));
        Ui.setDate(f.date, d(21));
        assertEquals("10:00", f.start.getValue());
        assertTrue(f.draft().editing());
        TaskForm p = edit(Tasks.task("보고서", d(18)).at("09:00").period(d(16), d(22)));
        assertEquals(List.of("시작일", "2026-11-16 (월)", true, "2026-11-22", false, false), List.of(p.dateLabel.getText(),
                text(p.date), p.period.isSelected(), text(p.due), p.timed.isSelected(), p.timed.isEnabled()));
        TaskForm fixed = edit(Tasks.task("수업", d(20)).kind(Kind.FIXED).at("13:00").minutes(75));
        assertEquals(List.of(true, true, false, "13:00", 75), List.of(fixed.fixed.isSelected(), fixed.timed.isSelected(),
                fixed.timed.isEnabled(), fixed.start.getValue(), fixed.minutes.getValue()));
        TaskForm untimed = edit(Tasks.task("빨래", d(18)));
        assertEquals(List.of(false, "10:10"), List.of(untimed.timed.isSelected(), untimed.start.getValue()));
        Ui.setDate(untimed.date, d(19));
        assertEquals("09:00", untimed.start.getValue());
    }

    @Test
    void eForm13OffStepStoredValues() {
        assertThrows(IllegalArgumentException.class, () -> edit(Tasks.task("운동", d(20)).at("09:03")));
        assertThrows(IllegalArgumentException.class, () -> edit(Tasks.task("운동", d(20)).at("10:00").minutes(27)));
        assertEquals(List.of(5, 725), List.of(edit(Tasks.task("짧게", d(20)).minutes(5)).minutes.getValue(),
                edit(Tasks.task("길게", d(20)).minutes(725)).minutes.getValue()));
    }
}
