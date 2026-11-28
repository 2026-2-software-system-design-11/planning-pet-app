package planningpet.app.view;

import planningpet.app.model.LoadResult.Reason;

/**
 * 데이터 파일을 못 읽음 창(05 그 밖의 창과 알림, 02 UC-01 2b). 06 21번 load가 UNREADABLE이면 Main이 띄운다.
 * 원인 글과 다음에 할 일은 05의 원인 표 그대로다(docs/porting/unreadable.md).
 */
public final class UnreadableDialog {
    private UnreadableDialog() {
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
