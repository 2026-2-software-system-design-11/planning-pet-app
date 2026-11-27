package planningpet.app.store;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import planningpet.app.model.AppData;
import planningpet.app.model.SampleLook;
import planningpet.pet.model.PetState;
import planningpet.schedule.model.Repeat;
import planningpet.schedule.model.Task;

/**
 * 앱 데이터를 07 저장 형식의 표 행으로 바꾼다. 날짜는 2026-11-16, 시각은 14:30, 반복은 DAILY나 MON,WED, 값 목록은 이름, 참거짓은
 * 1과 0이고, 값이 없으면 null이다.
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
}
