package planningpet.app.model;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

/** docs/porting/store.md 2절의 "모든 값을 채운 데이터"와 값 모두를 늘어놓는 검사 도구. 저장 검사(#352)도 쓴다. */
public final class Filled {
    private static LocalDate d(int day) { return LocalDate.of(2026, 11, day); }

    /** 06 데이터 절의 값마다 처음 값과 다른 값을 넣은 앱 데이터. */
    public static AppData data(Clock clock) {
        Task period = new Task("a", 1, Kind.TASK, "E사 지원서", d(18), LocalTime.of(14, 0), 90, "초안", false,
                Category.STUDY, null, null, d(22), d(16), true, d(18), 10, 10, d(18), PetKind.CAT);
        Task copy = new Task("b", 2, Kind.FIXED, "알바", d(21), LocalTime.of(1, 0), 240, "", true, Category.OTHER, "s",
                Repeat.weekly(DayOfWeek.SATURDAY), null, null, false, null, 0, 0, null, null);
        Task gone = new Task("c", 3, Kind.TASK, "운동", d(17), null, 30, "", false, Category.EXERCISE, null, null, null,
                null, true, d(17), 5, 5, d(17), PetKind.DOG);
        AppData data = new AppData(clock, List.of(period, copy), List.of(gone));
        data.setNextSeq(4);
        data.setPoints(-20);
        data.setRewardCount(d(18), 2);
        data.setAttendanceDay(d(18));
        data.setAttendDays(Set.of(d(17), d(18)));
        data.setLastOpenDay(d(18));
        data.setStreak(3);
        PetState cat = new PetState(PetKind.CAT, "나비");
        cat.setExp(120);
        cat.setPose(Pose.SING);
        cat.setWear(Map.of(Slot.NECK, WearId.STAR_NECKLACE, Slot.HAT, WearId.CROWN));
        PetState dog = new PetState(PetKind.DOG, "바둑이");
        dog.setWear(Map.of());
        data.addPet(cat);
        data.addPet(dog);
        data.setActiveKind(PetKind.DOG);
        data.setOwnedItems(Set.of(ItemId.CUSHION, ItemId.BONE));
        data.setPlacedItems(Set.of(ItemId.CUSHION));
        data.setTrayNoticeShown(true);
        data.setCalendarFixed(true);
        data.setListClickTime(true);
        data.setSamples(Set.of("b"), Map.of("b", new SampleLook("알바", LocalTime.of(1, 0), true, "", d(21))));
        data.setWeekStart(DayOfWeek.SUNDAY);
        return data;
    }

    /** 앱 데이터의 값 모두를 그때의 값으로 뜬 것. 두 데이터의 값이 같은지 이것으로 견준다. 모음은 순서를 보지 않는다. */
    public static List<Object> values(AppData a) {
        List<Object> out = new ArrayList<>(List.of(a.getNextSeq(), a.getPoints(), a.getRewardCount(),
                Set.copyOf(a.getAttendDays()), a.getStreak(), a.isTrayNoticeShown(), a.isCalendarFixed(),
                a.isListClickTime(), Set.copyOf(a.getOwnedItems()), Set.copyOf(a.getPlacedItems()),
                Set.copyOf(a.getSampleIds()), Map.copyOf(a.getSampleLooks()), a.getWeekStart()));
        out.addAll(Arrays.asList(a.getRewardCountDay(), a.getAttendanceDay(), a.getLastOpenDay(),
                a.getActiveKind()));
        a.getPets().forEach(p -> out.add(List.of(p.getKind(), p.getName(), p.getExp(), p.getPose(), Map.copyOf(p.getWear()))));
        a.getBook().getTasks().forEach(t -> out.add(values(t)));
        out.add("지운 보상 기록");
        a.getBook().getDeletedRewarded().forEach(t -> out.add(values(t)));
        return out;
    }

    public static List<Object> values(Task t) {
        return Arrays.asList(t.getId(), t.getSeq(), t.getKind(), t.getTitle(), t.getDate(),
                t.getStartTime(), t.getDurationMinutes(), t.getMemo(), t.isNextDay(), t.getCategory(),
                t.getSeriesId(), t.getRepeat(), t.getDueDate(), t.getStartDay(), t.isDone(), t.getDoneDay(),
                t.getRewardPoints(), t.getRewardExp(), t.getRewardDay(), t.getRewardPet());
    }
}
