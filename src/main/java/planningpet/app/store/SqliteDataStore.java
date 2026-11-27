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
import java.util.Map;
import planningpet.app.model.AppData;
import planningpet.app.model.DataStore;
import planningpet.app.model.LoadResult;

/** 데이터 폴더의 sqlite 파일 planningpet.db 하나에 쓰고 읽는다(06 21번, 07 저장 형식, D-111). */
public final class SqliteDataStore implements DataStore {
    static final String FILE = "planningpet.db";
    private final Path folder;
    /** 읽은 앱 데이터에 넘기는 시계(07 실행 모드). */
    private final Clock clock;
    /** 지난 저장(또는 읽기) 때의 값. save는 이것과 달라진 행만 쓴다. */
    private AppData saved;
    /** 지난 save가 쓰거나 지운 행 수. 검사가 본다. */
    private int written;

    public SqliteDataStore(Path folder, Clock clock) {
        this.folder = folder;
        this.clock = clock;
    }

    /** 파일이 없으면 만들지 않고 FIRST_RUN이다. 읽다 실패하면 UNREADABLE(READ_ERROR)이다. */
    @Override
    public LoadResult load() {
        Path file = folder.resolve(FILE);
        if (!Files.exists(file)) {
            return LoadResult.firstRun();
        }
        try (Connection db = open(file)) {
            AppData data = Rows.read(db, clock);
            saved = data.copy();
            return LoadResult.loaded(data);
        } catch (SQLException | RuntimeException e) {
            return LoadResult.unreadable(LoadResult.Reason.READ_ERROR);
        }
    }

    /** 지난 저장과 달라진 행만 트랜잭션 하나로 쓴다. 첫 저장은 표를 만드는 일도 같은 트랜잭션이다. */
    @Override
    public void save(AppData data) throws IOException {
        try {
            Files.createDirectories(folder);
            try (Connection db = open(folder.resolve(FILE))) {
                db.setAutoCommit(false);
                try (Statement s = db.createStatement(); ResultSet r = s.executeQuery("PRAGMA user_version")) {
                    if (r.getInt(1) == 0) {
                        Schema.create(db);
                    }
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
}
