package planningpet.app.view;

import java.awt.Component;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import planningpet.app.model.LoadResult.Reason;

/**
 * 데이터 파일을 못 읽음 창(05 그 밖의 창과 알림, 02 UC-01 2b). 06 21번 load가 UNREADABLE이면 Main이 띄운다.
 * 원인 글과 다음에 할 일은 05의 원인 표 그대로다(docs/porting/unreadable.md).
 */
public final class UnreadableDialog {
    static final String TITLE = "데이터를 읽지 못했어요";
    static final String KEPT = "파일은 지우지 않고 그대로 두었어요";
    static final String CLOSING = "확인을 누르면 앱을 끝내요";
    static final String WHERE = "파일 위치";
    static final String OK = "확인";

    private UnreadableDialog() {
    }

    /** 창을 띄우고 확인이나 닫기를 누를 때까지 기다린다. 앱을 끝내는 일은 부른 쪽(Main)이 한다. */
    public static void show(Component parent, Reason reason, Path file) {
        pane(reason, file).createDialog(parent, TITLE).setVisible(true);
    }

    /** 창 내용. 위에서 아래로 05의 글줄, 파일 위치, 파일 자리 칸이고 버튼은 확인 하나다. */
    public static JOptionPane pane(Reason reason, Path file) {
        List<Object> lines = new ArrayList<>();
        int width = 0;
        for (String line : List.of(KEPT, cause(reason), next(reason), CLOSING, WHERE)) {
            JLabel label = new JLabel(line);
            width = Math.max(width, label.getPreferredSize().width);
            lines.add(label);
        }
        // 05: 칸 폭은 위 글줄 가운데 가장 긴 줄이다. 경로에는 빈칸이 없을 때가 많아 글자 단위로 줄을 바꾼다.
        JTextArea where = new JTextArea(file.toString());
        where.setEditable(false);
        where.setLineWrap(true);
        where.setWrapStyleWord(false);
        where.setSize(width, Short.MAX_VALUE); // 폭을 주면 줄을 바꾼 높이가 바라는 크기가 된다.
        lines.add(where);
        return new JOptionPane(lines.toArray(), JOptionPane.ERROR_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[] {OK}, OK);
    }

    /** 원인 글. */
    static String cause(Reason reason) {
        return switch (reason) {
            case NEWER_VERSION -> "새 버전의 플래닝펫에서 만든 파일이에요";
            case NOT_DATA_FILE -> "플래닝펫 데이터 파일이 아니에요";
            case UPGRADE_FAILED -> "옛 형식 파일을 새 형식으로 바꾸지 못했어요";
            case READ_ERROR -> "파일을 읽다 오류가 났어요";
        };
    }

    /** 원인마다 다음에 할 일 한 줄. */
    static String next(Reason reason) {
        return switch (reason) {
            case NEWER_VERSION -> "이 컴퓨터의 플래닝펫을 최신 버전으로 업데이트해 주세요";
            case NOT_DATA_FILE -> "원래 planningpet.db를 이 자리에 다시 두세요";
            case UPGRADE_FAILED -> "앱을 다시 켜 보거나 최신 버전을 다시 받아 주세요";
            case READ_ERROR -> "백업해 둔 파일이 있으면 이 자리에 넣어 주세요";
        };
    }
}
