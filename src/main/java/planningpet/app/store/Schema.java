package planningpet.app.store;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;

/** 07 저장 형식의 표와 형식 번호(D-111). 표나 열이 바뀔 때만 VERSION을 올린다. */
public final class Schema {
    /** 형식 번호 n 파일을 n + 1로 옮기는 단계 하나. 새로 생긴 값은 06 데이터 절의 처음 값으로 채운다(07). */
    @FunctionalInterface
    interface Step {
        void apply(Connection db) throws SQLException;
    }

    /** 옮기는 단계들. STEPS.get(n - 1)이 형식 n 파일을 n + 1로 옮긴다. 형식이 1뿐이라 비었다. */
    static final List<Step> STEPS = List.of();

    /** 파일의 PRAGMA user_version에 적는 형식 번호. 1부터 세고 옮기는 단계마다 하나 늘어난다. */
    public static final int VERSION = 1 + STEPS.size();

    /** 표 하나. 열 이름은 06의 값 이름을 밑줄로 잇고, 앞의 keys개 열이 기본 키다. app 표는 기본 키가 없는 한 행이다. */
    record Table(String name, int keys, List<String> columns) {
        String create() {
            String key = keys == 0 ? "" : ", PRIMARY KEY (" + String.join(", ", columns.subList(0, keys)) + ")";
            return "CREATE TABLE " + name + " (" + String.join(", ", columns) + key + ")";
        }

        String insert() {
            return "INSERT INTO " + name + " (" + String.join(", ", columns) + ") VALUES ("
                    + String.join(", ", Collections.nCopies(columns.size(), "?")) + ")";
        }

        String delete() {
            List<String> where = columns.subList(0, keys).stream().map(c -> c + " = ?").toList();
            return "DELETE FROM " + name + (keys == 0 ? "" : " WHERE " + String.join(" AND ", where));
        }
    }

    static final List<Table> TABLES = List.of(
            new Table("app", 0, List.of("points", "reward_count_day", "reward_count", "attendance_day",
                    "last_open_day", "streak", "active_kind", "tray_notice_shown", "calendar_fixed", "list_click_time",
                    "week_start", "next_seq")),
            new Table("task", 1, List.of("id", "seq", "kind", "title", "date", "start_time", "duration_minutes", "memo",
                    "next_day", "category", "series_id", "repeat", "due_date", "start_day", "done", "done_day",
                    "reward_points", "reward_exp", "reward_day", "reward_pet", "deleted")),
            new Table("pet", 1, List.of("kind", "name", "exp", "pose", "ord")),
            new Table("wear", 2, List.of("pet_kind", "slot", "wear_id")),
            new Table("attend_day", 1, List.of("day")),
            new Table("item", 1, List.of("item_id", "placed")),
            new Table("sample", 1, List.of("task_id", "title", "start_time", "next_day", "memo", "date")));

    private Schema() {
    }

    /** 빈 파일에 표 일곱 개를 만들고 형식 번호를 적는다. 부르는 쪽의 트랜잭션 안에서 한다. */
    public static void create(Connection db) throws SQLException {
        try (Statement s = db.createStatement()) {
            for (Table t : TABLES) {
                s.execute(t.create());
            }
            s.execute("PRAGMA user_version = " + VERSION);
        }
    }

    /** from 번호 파일을 steps로 한 단계씩 옮기고 단계마다 형식 번호를 올린다. 부르는 쪽의 트랜잭션 안에서 한다. */
    static void upgrade(Connection db, int from, List<Step> steps) throws SQLException {
        try (Statement s = db.createStatement()) {
            for (int n = from; n <= steps.size(); n++) {
                steps.get(n - 1).apply(db);
                s.execute("PRAGMA user_version = " + (n + 1));
            }
        }
    }
}
