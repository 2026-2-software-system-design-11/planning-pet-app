package planningpet.app.model;

/** 06 21번 load의 결과. 읽었으면 data가, 읽을 수 없으면 reason이 있다. 화면 글은 그 창을 옮기는 S1이 정한다. */
public record LoadResult(Status status, AppData data, Reason reason) {
    public enum Status { LOADED, FIRST_RUN, UNREADABLE }

    /** 읽을 수 없는 이유(07 저장 형식). */
    public enum Reason {
        /** 앱보다 높은 형식 번호. 새 버전의 플래닝펫에서 만든 파일이다(T-STORE-4). */
        NEWER_VERSION,
        /** sqlite 파일이 아니거나 플래닝펫의 표가 없는 파일이다(T-STORE-3). */
        NOT_DATA_FILE,
        /** 낮은 형식 번호의 파일을 옮기다 실패했다. 파일은 옮기기 전 그대로다. */
        UPGRADE_FAILED,
        /** 파일을 읽다 오류가 났다. */
        READ_ERROR
    }

    public static LoadResult loaded(AppData data) { return new LoadResult(Status.LOADED, data, null); }
    public static LoadResult firstRun() { return new LoadResult(Status.FIRST_RUN, null, null); }
    public static LoadResult unreadable(Reason reason) { return new LoadResult(Status.UNREADABLE, null, reason); }
}
