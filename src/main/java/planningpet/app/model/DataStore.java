package planningpet.app.model;

import java.io.IOException;

/** 데이터 파일을 읽고 쓴다(06 21번). sqlite 구현은 app.store.SqliteDataStore다(07 저장 형식, D-111). */
public interface DataStore {
    /** 읽은 데이터, 첫 실행(파일 없음), 읽을 수 없음(이유와 함께) 가운데 하나. */
    LoadResult load();

    /** 바뀐 값을 트랜잭션 하나로 쓴다. 쓰지 못하면 IOException이다. */
    void save(AppData data) throws IOException;
}
