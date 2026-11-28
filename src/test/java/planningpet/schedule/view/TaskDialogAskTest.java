package planningpet.schedule.view;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import javax.swing.JOptionPane;
import org.junit.jupiter.api.Test;
import planningpet.schedule.model.Category;
import planningpet.schedule.model.Kind;
import planningpet.schedule.model.Overlap;
import planningpet.schedule.model.Repeat;
import planningpet.schedule.model.Task;

/** 일정 입력 창의 물음 창 셋. 번호는 docs/porting/task-dialog.md S4a 2절. 창은 띄우지 않고 내용과 답 바꾸기를 본다. */
class TaskDialogAskTest {
    private static final LocalDate DAY = LocalDate.of(2026, 11, 20);

    /** 반복 사본이면 seriesId가 있는 매주 반복 고정 일정이다. */
    private static Task task(String title, boolean series) {
        return new Task(title, 1, series ? Kind.FIXED : Kind.TASK, title, DAY, LocalTime.of(14, 0), 60, "", false, Category.OTHER,
                series ? "s1" : null, series ? Repeat.weekly(DAY.getDayOfWeek()) : null, null, null, false, null, 0, 0, null, null);
    }

    /** 글, 아이콘 종류, 단추 종류, 단추 글, 처음 골라 둔 단추. */
    private static List<Object> shown(JOptionPane pane) {
        return List.of(pane.getMessage(), pane.getMessageType(), pane.getOptionType(), Arrays.asList(pane.getOptions()),
                pane.getInitialValue());
    }

    private static List<Object> asked(String message, List<String> options, String initial) {
        return List.of(message, JOptionPane.QUESTION_MESSAGE, JOptionPane.DEFAULT_OPTION, options, initial);
    }

    @Test
    void tOvl1QuestionAndButtons() {
        Overlap o = new Overlap(task("자소서 초안", false), 30, true);
        assertEquals(asked("14:00~15:00 '자소서 초안'과 30분 겹쳐요. 그래도 넣을까요?", List.of("넣기", "취소"), "넣기"),
                shown(TaskDialog.overlapPane(o)));
    }

    @Test
    void eAsk1OverlapWindow() {
        assertEquals("시간이 겹쳐요", TaskDialog.OVERLAP);
        Overlap other = new Overlap(task("자소서 초안", false), 30, false);
        assertEquals(other.question(), TaskDialog.overlapPane(other).getMessage());
        assertEquals(null, TaskDialog.overlapPane(other).getIcon());
    }
}
