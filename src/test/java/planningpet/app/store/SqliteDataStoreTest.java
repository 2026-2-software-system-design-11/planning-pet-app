package planningpet.app.store;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import planningpet.app.model.AppData;
import planningpet.app.model.Filled;
import planningpet.app.model.LoadResult;
import planningpet.schedule.model.EditScope;

/** 06 21번과 docs/porting/store.md 2절의 E-STORE 경계. 데이터 폴더는 @TempDir, 지금은 11-18(수) 10:00이다. */
class SqliteDataStoreTest {
    private static final ZoneId ZONE = ZoneId.systemDefault();
    private static final Clock CLOCK = Clock.fixed(LocalDateTime.parse("2026-11-18T10:00").atZone(ZONE).toInstant(), ZONE);
    @TempDir
    Path folder;

    private SqliteDataStore store() {
        return new SqliteDataStore(folder, CLOCK);
    }

    /** 새 저장소로 읽은 데이터. */
    private AppData reload() {
        LoadResult r = store().load();
        assertEquals(LoadResult.Status.LOADED, r.status());
        return r.data();
    }

    @Test
    void T_STORE_1_allValuesComeBack() throws IOException {
        AppData data = Filled.data(CLOCK);
        store().save(data);
        assertEquals(Filled.values(data), Filled.values(reload()));
    }

    @Test
    void T_STORE_2_emptyFolderIsFirstRun() {
        assertEquals(LoadResult.Status.FIRST_RUN, store().load().status());
        assertFalse(Files.exists(folder.resolve(SqliteDataStore.FILE)));
    }

    @Test
    void E_STORE_1_startValuesComeBack() throws IOException {
        AppData data = new AppData(CLOCK);
        store().save(data);
        AppData back = reload();
        assertEquals(Filled.values(data), Filled.values(back));
        assertTrue(back.getPets().isEmpty());
    }

    @Test
    void E_STORE_2_onlyChangedRows() throws IOException {
        SqliteDataStore store = store();
        AppData data = Filled.data(CLOCK);
        store.save(data);
        data.getBook().clearTime(data.getBook().getTasks().get(0), EditScope.ONLY_THIS);
        store.save(data);
        assertEquals(1, store.written());
        assertEquals(Filled.values(data), Filled.values(reload()));
    }

    @Test
    void E_STORE_3_nothingChanged() throws IOException {
        store().save(Filled.data(CLOCK));
        SqliteDataStore store = store();
        store.save(store.load().data());
        assertEquals(0, store.written());
    }
}
