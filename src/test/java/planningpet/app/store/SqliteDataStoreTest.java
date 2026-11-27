package planningpet.app.store;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import planningpet.app.model.AppData;
import planningpet.app.model.Filled;
import planningpet.app.model.LoadResult;
import planningpet.pet.model.PetKind;
import planningpet.pet.model.PetState;
import planningpet.schedule.model.Category;
import planningpet.schedule.model.DeleteScope;
import planningpet.schedule.model.EditScope;
import planningpet.schedule.model.Kind;
import planningpet.schedule.model.Task;
import planningpet.schedule.model.TaskDraft;

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

    @Test
    void E_STORE_4_deletedTasks() throws IOException {
        SqliteDataStore store = store();
        AppData data = Filled.data(CLOCK);
        store.save(data);
        List.copyOf(data.getBook().getTasks()).forEach(t -> data.getBook().delete(t, DeleteScope.ONLY_THIS));
        store.save(data);
        assertEquals(2, store.written());
        AppData back = reload();
        assertTrue(back.getBook().getTasks().isEmpty());
        assertEquals(List.of(1L, 3L), back.getBook().getDeletedRewarded().stream().map(Task::getSeq).toList());
    }

    @Test
    void E_STORE_5_makesFolder() throws IOException {
        Path inner = folder.resolve("a").resolve("PlanningPet");
        new SqliteDataStore(inner, CLOCK).save(new AppData(CLOCK));
        assertTrue(Files.isRegularFile(inner.resolve(SqliteDataStore.FILE)));
    }

    @Test
    void E_STORE_6_order() throws IOException {
        AppData data = new AppData(CLOCK);
        data.addPet(new PetState(PetKind.DOG, "바둑이"));
        data.addPet(new PetState(PetKind.CAT, "나비"));
        for (String title : List.of("셋", "하나", "둘")) {
            add(data, title, "");
        }
        store().save(data);
        AppData back = reload();
        assertEquals(List.of(PetKind.DOG, PetKind.CAT), back.getPets().stream().map(PetState::getKind).toList());
        assertEquals(List.of("셋", "하나", "둘"), back.getBook().getTasks().stream().map(Task::getTitle).toList());
    }

    @Test
    void E_STORE_7_text() throws IOException {
        AppData data = new AppData(CLOCK);
        add(data, "it's \"따옴표\" 🐱", "첫 줄\n둘째 줄");
        store().save(data);
        Task back = reload().getBook().getTasks().get(0);
        assertEquals("it's \"따옴표\" 🐱", back.getTitle());
        assertEquals("첫 줄\n둘째 줄", back.getMemo());
    }

    @Test
    void E_STORE_8_failedSaveKeepsLastSave() throws IOException {
        Path blocked = folder.resolve("data");
        Files.writeString(blocked, "폴더 자리에 파일");
        SqliteDataStore store = new SqliteDataStore(blocked, CLOCK);
        AppData data = Filled.data(CLOCK);
        assertThrows(IOException.class, () -> store.save(data));
        Files.delete(blocked);
        store.save(data);
        assertEquals(Rows.of(data).values().stream().mapToInt(t -> t.size()).sum(), store.written());
    }

    @Test
    void T_STORE_3_notSqliteFileUntouched() throws IOException {
        Files.writeString(file(), "플래닝펫 파일이 아닌 글");
        assertUnreadable(LoadResult.Reason.NOT_DATA_FILE);
    }

    @Test
    void T_STORE_4_newerVersionUntouched() throws Exception {
        store().save(Filled.data(CLOCK));
        sql("PRAGMA user_version = " + (Schema.VERSION + 1));
        assertUnreadable(LoadResult.Reason.NEWER_VERSION);
    }

    @Test
    void E_STORE_9_emptyFileIsFirstRun() throws IOException {
        Files.write(file(), new byte[0]);
        assertEquals(LoadResult.Status.FIRST_RUN, store().load().status());
        assertEquals(0, Files.size(file()));
    }

    @Test
    void E_STORE_10_otherSqliteFileUntouched() throws Exception {
        sql("CREATE TABLE note (body)");
        assertUnreadable(LoadResult.Reason.NOT_DATA_FILE);
    }

    private Path file() {
        return folder.resolve(SqliteDataStore.FILE);
    }

    /** 데이터 파일에 sql 한 줄을 쓴다. */
    private void sql(String line) throws SQLException {
        try (Connection db = DriverManager.getConnection("jdbc:sqlite:" + file())) {
            db.createStatement().execute(line);
        }
    }

    /** load가 그 이유로 UNREADABLE이고 폴더의 파일과 바이트가 그대로다. */
    private void assertUnreadable(LoadResult.Reason reason) throws IOException {
        byte[] before = Files.readAllBytes(file());
        assertEquals(LoadResult.unreadable(reason), store().load());
        assertArrayEquals(before, Files.readAllBytes(file()));
        try (Stream<Path> files = Files.list(folder)) {
            assertEquals(List.of(file()), files.toList());
        }
    }

    private static void add(AppData data, String title, String memo) {
        LocalDate today = data.today();
        data.getBook().add(new TaskDraft(Kind.TASK, title, Category.OTHER, today, null, null, 30, memo, null, null,
                false, null, today, false));
    }
}
