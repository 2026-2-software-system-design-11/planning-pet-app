package planningpet.pet.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.event.KeyEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import planningpet.app.controller.AppController;
import planningpet.app.model.AppData;
import planningpet.app.model.DataStore;
import planningpet.app.model.LoadResult;
import planningpet.pet.controller.PetController;
import planningpet.pet.model.PetKind;

/** 고르기 창의 바로 시작, Enter, 닫기. 번호는 docs/porting/pick.md 2절의 E-PWIN. 지금은 11-16 10:00, 첫 실행이다. */
class PickDialogStartTest {
    private final AppData data = new AppData(Clock.fixed(
            LocalDateTime.of(2026, 11, 16, 10, 0).atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault()));
    /** 저장, 오류 창의 글, 창 닫기, 고른 뒤 알림을 일어난 차례로 적는다. */
    private final List<String> calls = new ArrayList<>();
    private boolean failing;
    private final AppController app = new AppController(data, new DataStore() {
        public LoadResult load() { return LoadResult.firstRun(); }
        public void save(AppData d) throws IOException {
            if (failing) {
                throw new IOException("디스크가 찼다");
            }
            calls.add("저장");
        }
    }, calls::add);
    private final PickDialog pick = new PickDialog();

    PickDialogStartTest() {
        pick.close = () -> calls.add("닫기");
        pick.onStart(new PetController(data, app), data, () -> calls.add("고름"));
    }

    private List<String> pets() {
        return data.getPets().stream().map(p -> p.getKind() + " " + p.getName()).toList();
    }

    @Test
    void ePwin1StartWithEmptyName() {
        pick.dog.doClick();
        pick.start.doClick();
        assertEquals(List.of("DOG 바둑이"), pets());
        assertEquals(PetKind.DOG, data.getActiveKind());
        assertEquals(List.of("저장", "닫기", "고름"), calls);
    }

    @Test
    void ePwin3ExamplesDoNothingYet() {
        pick.examples.doClick();
        assertEquals(List.of(), pets());
        assertEquals(List.of(), calls);
    }

    @Test
    void ePwin4SaveFailedKeepsWindow() {
        failing = true;
        pick.dog.doClick();
        pick.start.doClick();
        assertEquals(List.of(), pets());
        assertEquals(List.of("반려동물 고르기 저장에 실패해서 바꾸기 전으로 돌렸어요."), calls);
        failing = false;
        pick.start.doClick();
        assertEquals(List.of("DOG 바둑이"), pets());
        assertEquals(List.of("반려동물 고르기 저장에 실패해서 바꾸기 전으로 돌렸어요.", "저장", "닫기", "고름"), calls);
    }

    @Test
    void ePwin2EnterIsStart() {
        JRootPane root = new JRootPane();
        pick.into(root);
        JPanel holder = new JPanel();
        holder.add(root);
        holder.addNotify(); // 창 없이 보이는 부품으로 만든다. 기본 버튼은 보일 때만 눌린다.
        pick.name.setText("콩");
        KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
        assertFalse(pick.name.getActionMap().get(pick.name.getInputMap().get(enter)).isEnabled(), "칸의 Enter는 꺼져 있다");
        Action press = root.getActionMap().get(root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).get(enter));
        KeyEvent key = new KeyEvent(pick.name, KeyEvent.KEY_PRESSED, 0, 0, KeyEvent.VK_ENTER, '\n');
        assertTrue(SwingUtilities.notifyAction(press, enter, key, root, 0));
        holder.removeNotify();
        assertEquals(List.of("CAT 콩"), pets());
        assertEquals(List.of("저장", "닫기", "고름"), calls);
    }

    @Test
    void ePwin5CloseButtonOnly() {
        WindowListener listener = PickDialog.onClose(() -> calls.add("닫았다"));
        listener.windowClosed(null);
        assertEquals(List.of(), calls, "바로 시작 뒤의 dispose");
        listener.windowClosing(null);
        assertEquals(List.of("닫았다"), calls);
        assertEquals(List.of(), pets());
    }

    @Test
    void ePwin6Window() {
        JRootPane root = new JRootPane();
        pick.into(root);
        assertSame(pick.panel, root.getContentPane());
        assertSame(pick.start, root.getDefaultButton());
        assertEquals("플래닝펫 시작", PickDialog.TITLE);
    }
}
