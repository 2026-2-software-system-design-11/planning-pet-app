package planningpet.schedule.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.awt.HeadlessException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import javax.swing.JOptionPane;
import org.junit.jupiter.api.Test;
import planningpet.schedule.model.Category;
import planningpet.schedule.model.DeleteScope;
import planningpet.schedule.model.EditScope;
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

    @Test
    void eAsk2ScopeBatchim() {
        assertEquals("반복 일정 저장", TaskDialog.SCOPE);
        List<String> scopes = List.of("이 일정만", "이후 반복 모두", "취소");
        assertEquals(asked("'대학수학'은 반복 일정이에요. 어디까지 바꿀까요?", scopes, "취소"),
                shown(TaskDialog.scopePane(task("대학수학", true))));
        assertEquals(asked("'과제'는 반복 일정이에요. 어디까지 바꿀까요?", scopes, "취소"), shown(TaskDialog.scopePane(task("과제", true))));
    }

    @Test
    void eAsk3DeleteConfirm() {
        assertEquals("일정 삭제", TaskDialog.DELETE);
        List<String> buttons = List.of("삭제", "취소");
        assertEquals(asked("'대학수학'을 삭제할까요?", buttons, "취소"), shown(TaskDialog.deletePane(task("대학수학", false))));
        assertEquals(asked("'과제'를 삭제할까요?", buttons, "취소"), shown(TaskDialog.deletePane(task("과제", false))));
    }

    @Test
    void eAsk4SeriesDelete() {
        List<String> scopes = List.of("이 일정만", "이후 반복 모두", "취소");
        assertEquals(asked("'대학수학'은 반복 일정이에요.", scopes, "취소"), shown(TaskDialog.deletePane(task("대학수학", true))));
        assertEquals(asked("'과제'는 반복 일정이에요.", scopes, "취소"), shown(TaskDialog.deletePane(task("과제", true))));
    }

    @Test
    void eAsk5Answer() {
        JOptionPane pane = TaskDialog.scopePane(task("과제", true));
        List<Integer> got = new ArrayList<>(List.of(TaskDialog.answer(pane))); // 고르기 전
        for (Object picked : new Object[] {"이 일정만", "이후 반복 모두", "취소", null, JOptionPane.UNINITIALIZED_VALUE}) {
            pane.setValue(picked);
            got.add(TaskDialog.answer(pane));
        }
        assertEquals(List.of(-1, 0, 1, 2, -1, -1), got);
        assertEquals(List.of(true, false, false, false), IntStream.of(0, 1, 2, -1).mapToObj(TaskDialog::insert).toList());
        // 창 없이는 띄우려 할 때 창을 만들지 못한다.
        assertThrows(HeadlessException.class, () -> TaskDialog.askOverlap(null, new Overlap(task("과제", false), 30, true)));
    }

    @Test
    void eAsk5Scope() {
        Task one = task("과제", false);
        Task copy = task("과제", true);
        assertEquals(Arrays.asList(EditScope.ONLY_THIS, EditScope.THIS_AND_AFTER, null, null),
                IntStream.of(0, 1, 2, -1).mapToObj(TaskDialog::editScope).toList());
        assertEquals(Arrays.asList(DeleteScope.ONLY_THIS, null, null),
                IntStream.of(0, 1, -1).mapToObj(a -> TaskDialog.deleteScope(one, a)).toList());
        assertEquals(Arrays.asList(DeleteScope.ONLY_THIS, DeleteScope.THIS_AND_AFTER, null, null),
                IntStream.of(0, 1, 2, -1).mapToObj(a -> TaskDialog.deleteScope(copy, a)).toList());
        assertThrows(HeadlessException.class, () -> TaskDialog.chooseScope(null, copy));
        assertThrows(HeadlessException.class, () -> TaskDialog.confirmDelete(null, one));
    }
}
