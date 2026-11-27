package planningpet.app.store;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Clock;
import java.util.List;
import java.util.Map;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteErrorCode;
import org.sqlite.SQLiteException;
import planningpet.app.model.AppData;
import planningpet.app.model.DataStore;
import planningpet.app.model.LoadResult;
import planningpet.app.model.LoadResult.Reason;

/** 데이터 폴더의 sqlite 파일 planningpet.db 하나에 쓰고 읽는다(06 21번, 07 저장 형식, D-111). */
public final class SqliteDataStore implements DataStore {
    static final String FILE = "planningpet.db";
    private final Path folder;
    /** 읽은 앱 데이터에 넘기는 시계(07 실행 모드). */
    private final Clock clock;
    /** 낮은 번호 파일을 옮기는 단계(Schema.STEPS). 이 앱의 형식 번호는 단계 수 + 1이다. */
    private final List<Schema.Step> steps;
    /** 지난 저장(또는 읽기) 때의 값. save는 이것과 달라진 행만 쓴다. */
    private AppData saved;
    /** 지난 save가 쓰거나 지운 행 수. 검사가 본다. */
    private int written;

    public SqliteDataStore(Path folder, Clock clock) {
        this(folder, clock, Schema.STEPS);
    }

    /** 옮기는 단계를 바꿔 끼운다. 형식이 1뿐인 동안 검사가 형식 2를 흉내 내 옮기기를 검사한다(T-STORE-7). */
    SqliteDataStore(Path folder, Clock clock, List<Schema.Step> steps) {
        this.folder = folder;
        this.clock = clock;
        this.steps = steps;
    }

    /**
     * 파일이 없거나 표 없는 형식 번호 0 파일(첫 저장 도중 꺼진 파일)이면 FIRST_RUN이다. 먼저 읽기 전용으로 열어 형식 번호를 보고,
     * 읽을 수 없으면 파일을 건드리지 않고 UNREADABLE과 이유다(07 저장 형식).
     */
    @Override
    public LoadResult load() {
        Path file = folder.resolve(FILE);
        if (!Files.exists(file)) {
            return LoadResult.firstRun();
        }
        try (Connection db = openToRead(file)) {
            int version = version(db);
            if (version == 0) {
                return hasTables(db) ? LoadResult.unreadable(Reason.NOT_DATA_FILE) : LoadResult.firstRun();
            }
            if (version > steps.size() + 1) {
                return LoadResult.unreadable(Reason.NEWER_VERSION);
            }
            return version < steps.size() + 1 ? upgrade(file, version) : read(db);
        } catch (SQLiteException e) {
            return LoadResult.unreadable(e.getResultCode() == SQLiteErrorCode.SQLITE_NOTADB ? Reason.NOT_DATA_FILE
                    : Reason.READ_ERROR);
        } catch (SQLException | RuntimeException e) {
            return LoadResult.unreadable(Reason.READ_ERROR);
        }
    }

    /**
     * 낮은 번호 파일을 같은 폴더의 planningpet.v{번호}.db로 복사하고(그 이름이 있으면 덮어쓰지 않는다) 트랜잭션 하나로 옮긴 뒤 읽는다.
     * 옮기다 실패하면 sqlite가 되돌려 파일은 옮기기 전 그대로이고 UNREADABLE(UPGRADE_FAILED)이다(07).
     */
    private LoadResult upgrade(Path file, int version) {
        try {
            Path copy = folder.resolve("planningpet.v" + version + ".db");
            if (!Files.exists(copy)) {
                Files.copy(file, copy);
            }
            try (Connection db = open(file)) {
                db.setAutoCommit(false);
                Schema.upgrade(db, version, steps);
                db.commit();
                return read(db);
            }
        } catch (IOException | SQLException | RuntimeException e) {
            return LoadResult.unreadable(Reason.UPGRADE_FAILED);
        }
    }

    private LoadResult read(Connection db) throws SQLException {
        AppData data = Rows.read(db, clock);
        saved = data.copy();
        return LoadResult.loaded(data);
    }

    /** 지난 저장과 달라진 행만 트랜잭션 하나로 쓴다. 첫 저장은 표를 만드는 일도 같은 트랜잭션이다. */
    @Override
    public void save(AppData data) throws IOException {
        try {
            Files.createDirectories(folder);
            try (Connection db = open(folder.resolve(FILE))) {
                db.setAutoCommit(false);
                if (version(db) == 0) {
                    Schema.create(db);
                }
                int n = Rows.write(db, saved == null ? Map.of() : Rows.of(saved), Rows.of(data));
                db.commit();
                written = n;
            }
        } catch (SQLException e) {
            throw new IOException(e);
        }
        saved = data.copy();
    }

    int written() {
        return written;
    }

    private static Connection open(Path file) throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + file);
    }

    /** 읽기 전용으로 연다. 저장 도중 꺼져 저널이 남았으면 sqlite가 되돌리도록 읽고 쓰기로 다시 연다(06 21번). */
    private static Connection openToRead(Path file) throws SQLException {
        SQLiteConfig config = new SQLiteConfig();
        config.setReadOnly(true);
        Connection db = config.createConnection("jdbc:sqlite:" + file);
        try {
            version(db);
            return db;
        } catch (SQLiteException e) {
            db.close();
            if (e.getResultCode() != SQLiteErrorCode.SQLITE_READONLY_ROLLBACK) {
                throw e;
            }
            return open(file);
        }
    }

    /** 파일의 형식 번호(PRAGMA user_version). 표를 만들기 전은 0이다. */
    static int version(Connection db) throws SQLException {
        try (Statement s = db.createStatement(); ResultSet r = s.executeQuery("PRAGMA user_version")) {
            return r.getInt(1);
        }
    }

    /** 표가 하나라도 있으면 참이다. 형식 번호 0인데 표가 있으면 다른 프로그램의 sqlite 파일이다. */
    private static boolean hasTables(Connection db) throws SQLException {
        try (Statement s = db.createStatement(); ResultSet r = s.executeQuery("SELECT count(*) FROM sqlite_master")) {
            return r.getInt(1) > 0;
        }
    }
}
