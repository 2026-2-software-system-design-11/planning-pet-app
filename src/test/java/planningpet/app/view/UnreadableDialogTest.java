package planningpet.app.view;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import planningpet.app.model.LoadResult.Reason;

/** 데이터 파일을 못 읽음 창. 번호는 docs/porting/unreadable.md 2절. */
class UnreadableDialogTest {
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
