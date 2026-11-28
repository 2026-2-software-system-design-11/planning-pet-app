package planningpet.app.model;

import java.io.IOException;

/** 파일 없이 저장을 세는 저장 대역(#518). failing이면 저장마다 IOException이다. */
public final class MemoryStore implements DataStore {
    private final LoadResult result;
    /** 저장에 성공한 횟수. */
    public int saves;
    /** 참이면 save가 "디스크가 찼다"로 실패한다. */
    public boolean failing;
    /** 마지막으로 저장하라고 받은 데이터(실패한 것도). */
    public AppData last;

    /** 첫 실행(파일 없음)을 읽는 저장소. */
    public MemoryStore() {
        this(LoadResult.firstRun());
    }

    public MemoryStore(LoadResult result) {
        this.result = result;
    }

    @Override
    public LoadResult load() {
        return result;
    }

    @Override
    public void save(AppData data) throws IOException {
        last = data;
        if (failing) {
            throw new IOException("디스크가 찼다");
        }
        saves++;
    }
}
