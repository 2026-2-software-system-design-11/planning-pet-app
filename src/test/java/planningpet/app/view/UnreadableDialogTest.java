package planningpet.app.view;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Component;
import java.awt.Container;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import org.junit.jupiter.api.Test;
import planningpet.app.model.LoadResult.Reason;

/** 데이터 파일을 못 읽음 창. 번호는 docs/porting/unreadable.md 2절. */
class UnreadableDialogTest {
    private static final Path FILE = Path.of("/Users/나비/Library/Application Support/PlanningPet/planningpet.db");

    /** 창 안의 글줄, 파일 자리 칸, 버튼을 위에서 아래로. */
    private static <T extends Component> List<T> parts(Component c, Class<T> type) {
        List<T> found = new ArrayList<>();
        if (type.isInstance(c)) {
            found.add(type.cast(c));
        }
        if (c instanceof Container box) {
            for (Component child : box.getComponents()) {
                found.addAll(parts(child, type));
            }
        }
        return found;
    }

    private static List<String> texts(JOptionPane pane) {
        List<String> lines = new ArrayList<>();
        for (Component c : parts(pane, Component.class)) {
            if (c instanceof JLabel label && label.getText() != null) {
                lines.add(label.getText());
            } else if (c instanceof JTextArea area) {
                lines.add(area.getText());
            }
        }
        return lines;
    }

    @Test
    void eUnread4LinesTopToBottom() {
        assertEquals("데이터를 읽지 못했어요", UnreadableDialog.TITLE);
        for (Reason reason : Reason.values()) {
            JOptionPane pane = UnreadableDialog.pane(reason, FILE);
            assertEquals(JOptionPane.ERROR_MESSAGE, pane.getMessageType());
            assertEquals(List.of("파일은 지우지 않고 그대로 두었어요", UnreadableDialog.cause(reason), UnreadableDialog.next(reason),
                    "확인을 누르면 앱을 끝내요", "파일 위치", FILE.toString()), texts(pane), reason.name());
        }
    }

    @Test
    void eUnread5OneOkButtonCloses() {
        JOptionPane pane = UnreadableDialog.pane(Reason.READ_ERROR, FILE);
        List<JButton> buttons = parts(pane, JButton.class);
        assertEquals(1, buttons.size());
        assertEquals("확인", buttons.get(0).getText());
        assertEquals(JOptionPane.UNINITIALIZED_VALUE, pane.getValue());
        buttons.get(0).doClick(); // createDialog의 창은 값이 정해지면 닫힌다.
        assertEquals("확인", pane.getValue());
    }

    @Test
    void tStore4NewerVersion() {
        assertEquals("새 버전의 플래닝펫에서 만든 파일이에요", UnreadableDialog.cause(Reason.NEWER_VERSION));
        assertEquals("이 컴퓨터의 플래닝펫을 최신 버전으로 업데이트해 주세요", UnreadableDialog.next(Reason.NEWER_VERSION));
    }

    @Test
    void eUnread1NotDataFile() {
        assertEquals("플래닝펫 데이터 파일이 아니에요", UnreadableDialog.cause(Reason.NOT_DATA_FILE));
        assertEquals("원래 planningpet.db를 이 자리에 다시 두세요", UnreadableDialog.next(Reason.NOT_DATA_FILE));
    }

    @Test
    void eUnread2UpgradeFailed() {
        assertEquals("옛 형식 파일을 새 형식으로 바꾸지 못했어요", UnreadableDialog.cause(Reason.UPGRADE_FAILED));
        assertEquals("앱을 다시 켜 보거나 최신 버전을 다시 받아 주세요", UnreadableDialog.next(Reason.UPGRADE_FAILED));
    }

    @Test
    void eUnread3ReadError() {
        assertEquals("파일을 읽다 오류가 났어요", UnreadableDialog.cause(Reason.READ_ERROR));
        assertEquals("백업해 둔 파일이 있으면 이 자리에 넣어 주세요", UnreadableDialog.next(Reason.READ_ERROR));
    }
}
