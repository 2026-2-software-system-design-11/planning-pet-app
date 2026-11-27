package planningpet.app.store;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import planningpet.app.model.AppData;
import planningpet.app.model.SampleLook;
import planningpet.pet.model.PetKind;
import planningpet.pet.model.PetState;
import planningpet.pet.model.Pose;
import planningpet.pet.model.Slot;
import planningpet.pet.model.WearId;
import planningpet.schedule.model.Category;
import planningpet.schedule.model.Kind;
import planningpet.schedule.model.Repeat;
import planningpet.schedule.model.Task;
import planningpet.shop.model.ItemId;

/**
 * 앱 데이터와 07 저장 형식의 표 행 사이를 바꾸고, 표를 읽어 앱 데이터를 만든다. 날짜는 2026-11-16, 시각은 14:30, 반복은
 * DAILY나 MON,WED, 값 목록은 이름, 참거짓은 1과 0이고, 값이 없으면 null이다.
 */
final class Rows {
    static final DateTimeFormatter HHMM = DateTimeFormatter.ofPattern("HH:mm");

    private Rows() {
    }

    /** 표 이름에서, 기본 키에서 행으로 가는 모음으로. 행은 Schema의 열 순서다. */
    static Map<String, Map<List<Object>, List<Object>>> of(AppData d) {
        Map<String, Map<List<Object>, List<Object>>> out = new HashMap<>();
        Schema.TABLES.forEach(t -> out.put(t.name(), new LinkedHashMap<>()));
        put(out, "app", d.getPoints(), text(d.getRewardCountDay()), d.getRewardCount(), text(d.getAttendanceDay()),
                text(d.getLastOpenDay()), d.getStreak(), text(d.getActiveKind()), bit(d.isTrayNoticeShown()),
                bit(d.isCalendarFixed()), bit(d.isListClickTime()), text(d.getWeekStart()), d.getNextSeq());
        d.getBook().getTasks().forEach(t -> task(out, t, false));
        d.getBook().getDeletedRewarded().forEach(t -> task(out, t, true));
        List<PetState> pets = d.getPets();
        for (int i = 0; i < pets.size(); i++) {
            PetState p = pets.get(i);
            put(out, "pet", text(p.getKind()), p.getName(), p.getExp(), text(p.getPose()), i);
            p.getWear().forEach((slot, wear) -> put(out, "wear", text(p.getKind()), text(slot), text(wear)));
        }
        d.getAttendDays().forEach(day -> put(out, "attend_day", text(day)));
        d.getOwnedItems().forEach(item -> put(out, "item", text(item), bit(d.getPlacedItems().contains(item))));
        for (String id : d.getSampleIds()) {
            SampleLook l = d.getSampleLooks().get(id);
            put(out, "sample", id, l == null ? null : l.title(), l == null ? null : text(l.startTime()),
                    l == null ? null : bit(l.nextDay()), l == null ? null : l.memo(), l == null ? null : text(l.date()));
        }
        return out;
    }

    private static void task(Map<String, Map<List<Object>, List<Object>>> out, Task t, boolean deleted) {
        put(out, "task", t.getId(), t.getSeq(), text(t.getKind()), t.getTitle(), text(t.getDate()),
                text(t.getStartTime()), t.getDurationMinutes(), t.getMemo(), bit(t.isNextDay()), text(t.getCategory()),
                t.getSeriesId(), text(t.getRepeat()), text(t.getDueDate()), text(t.getStartDay()), bit(t.isDone()),
                text(t.getDoneDay()), t.getRewardPoints(), t.getRewardExp(), text(t.getRewardDay()),
                text(t.getRewardPet()), bit(deleted));
    }

    private static void put(Map<String, Map<List<Object>, List<Object>>> out, String table, Object... values) {
        int keys = Schema.TABLES.stream().filter(t -> t.name().equals(table)).findFirst().orElseThrow().keys();
        List<Object> row = Arrays.asList(values);
        out.get(table).put(row.subList(0, keys), row);
    }

    static int bit(boolean b) {
        return b ? 1 : 0;
    }

    /** 값 하나를 표의 글로. */
    static String text(Object v) {
        if (v instanceof LocalTime t) {
            return t.format(HHMM);
        }
        if (v instanceof Repeat r) {
            return r.daily() ? "DAILY" : String.join(",",
                    r.days().stream().sorted().map(day -> day.name().substring(0, 3)).toList());
        }
        if (v instanceof Enum<?> e) {
            return e.name();
        }
        return v == null ? null : v.toString();
    }

    /** 표의 반복 글을 반복 방식으로. */
    static Repeat repeat(String s) {
        if (s == null || s.equals("DAILY")) {
            return s == null ? null : Repeat.DAILY;
        }
        return new Repeat(false, Arrays.stream(s.split(",")).map(Rows::day).collect(Collectors.toSet()));
    }

    private static DayOfWeek day(String three) {
        return Arrays.stream(DayOfWeek.values()).filter(d -> d.name().startsWith(three)).findFirst().orElseThrow();
    }

    /** before에 있고 달라졌거나 없어진 행을 지우고, 새로 생기거나 달라진 행을 넣는다. 달라진 행 수를 돌려준다. */
    static int write(Connection db, Map<String, Map<List<Object>, List<Object>>> before,
            Map<String, Map<List<Object>, List<Object>>> after) throws SQLException {
        int n = 0;
        for (Schema.Table t : Schema.TABLES) {
            Map<List<Object>, List<Object>> old = before.getOrDefault(t.name(), Map.of());
            Map<List<Object>, List<Object>> now = after.get(t.name());
            try (PreparedStatement del = db.prepareStatement(t.delete());
                    PreparedStatement ins = db.prepareStatement(t.insert())) {
                for (var e : old.entrySet()) {
                    if (!e.getValue().equals(now.get(e.getKey()))) {
                        run(del, e.getKey());
                        n++;
                    }
                }
                for (var e : now.entrySet()) {
                    if (!e.getValue().equals(old.get(e.getKey()))) {
                        run(ins, e.getValue());
                        n += old.containsKey(e.getKey()) ? 0 : 1;
                    }
                }
            }
        }
        return n;
    }

    private static void run(PreparedStatement s, List<Object> values) throws SQLException {
        for (int i = 0; i < values.size(); i++) {
            s.setObject(i + 1, values.get(i));
        }
        s.executeUpdate();
    }

    private interface Row {
        void read(ResultSet r) throws SQLException;
    }

    /** 표를 읽어 앱 데이터를 만든다. 일정과 지운 보상 기록은 넣은 순, 펫은 ord 순이다. */
    static AppData read(Connection db, Clock clock) throws SQLException {
        List<Task> tasks = new ArrayList<>();
        List<Task> deleted = new ArrayList<>();
        each(db, "task ORDER BY seq", r -> (r.getInt("deleted") == 1 ? deleted : tasks).add(task(r)));
        AppData d = new AppData(clock, tasks, deleted);
        each(db, "app", r -> {
            d.setPoints(r.getInt("points"));
            d.setRewardCount(date(r, "reward_count_day"), r.getInt("reward_count"));
            d.setAttendanceDay(date(r, "attendance_day"));
            d.setLastOpenDay(date(r, "last_open_day"));
            d.setStreak(r.getInt("streak"));
            String active = r.getString("active_kind");
            d.setActiveKind(active == null ? null : PetKind.valueOf(active));
            d.setTrayNoticeShown(r.getInt("tray_notice_shown") == 1);
            d.setCalendarFixed(r.getInt("calendar_fixed") == 1);
            d.setListClickTime(r.getInt("list_click_time") == 1);
            d.setWeekStart(DayOfWeek.valueOf(r.getString("week_start")));
            d.setNextSeq(r.getLong("next_seq"));
        });
        Map<String, Map<Slot, WearId>> wear = new LinkedHashMap<>();
        each(db, "wear", r -> wear.computeIfAbsent(r.getString("pet_kind"), k -> new EnumMap<>(Slot.class))
                .put(Slot.valueOf(r.getString("slot")), WearId.valueOf(r.getString("wear_id"))));
        each(db, "pet ORDER BY ord", r -> {
            PetState p = new PetState(PetKind.valueOf(r.getString("kind")), r.getString("name"));
            p.setExp(r.getInt("exp"));
            p.setPose(Pose.valueOf(r.getString("pose")));
            p.setWear(wear.getOrDefault(r.getString("kind"), Map.of()));
            d.addPet(p);
        });
        Set<LocalDate> days = new LinkedHashSet<>();
        each(db, "attend_day ORDER BY day", r -> days.add(date(r, "day")));
        d.setAttendDays(days);
        Set<ItemId> owned = new LinkedHashSet<>();
        Set<ItemId> placed = new LinkedHashSet<>();
        each(db, "item", r -> {
            ItemId item = ItemId.valueOf(r.getString("item_id"));
            owned.add(item);
            if (r.getInt("placed") == 1) {
                placed.add(item);
            }
        });
        d.setOwnedItems(owned);
        d.setPlacedItems(placed);
        Set<String> ids = new LinkedHashSet<>();
        Map<String, SampleLook> looks = new LinkedHashMap<>();
        each(db, "sample", r -> {
            ids.add(r.getString("task_id"));
            if (r.getString("title") != null) {
                looks.put(r.getString("task_id"), new SampleLook(r.getString("title"), time(r, "start_time"),
                        r.getInt("next_day") == 1, r.getString("memo"), date(r, "date")));
            }
        });
        d.setSamples(ids, looks);
        return d;
    }

    private static Task task(ResultSet r) throws SQLException {
        String pet = r.getString("reward_pet");
        return new Task(r.getString("id"), r.getLong("seq"), Kind.valueOf(r.getString("kind")), r.getString("title"),
                date(r, "date"), time(r, "start_time"), r.getInt("duration_minutes"), r.getString("memo"),
                r.getInt("next_day") == 1, Category.valueOf(r.getString("category")), r.getString("series_id"),
                repeat(r.getString("repeat")), date(r, "due_date"), date(r, "start_day"), r.getInt("done") == 1,
                date(r, "done_day"), r.getInt("reward_points"), r.getInt("reward_exp"), date(r, "reward_day"),
                pet == null ? null : PetKind.valueOf(pet));
    }

    /** 표의 행마다 row를 부른다. from은 표 이름과 그 뒤의 ORDER BY다. */
    private static void each(Connection db, String from, Row row) throws SQLException {
        try (Statement s = db.createStatement(); ResultSet r = s.executeQuery("SELECT * FROM " + from)) {
            while (r.next()) {
                row.read(r);
            }
        }
    }

    private static LocalDate date(ResultSet r, String column) throws SQLException {
        String s = r.getString(column);
        return s == null ? null : LocalDate.parse(s);
    }

    private static LocalTime time(ResultSet r, String column) throws SQLException {
        String s = r.getString(column);
        return s == null ? null : LocalTime.parse(s, HHMM);
    }
}
