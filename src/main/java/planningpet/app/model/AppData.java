package planningpet.app.model;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import planningpet.common.model.AppDay;
import planningpet.pet.model.PetKind;
import planningpet.pet.model.PetState;
import planningpet.schedule.model.TaskBook;
import planningpet.shop.model.ItemId;

/** 앱 하나에 하나뿐인 값(06 데이터 절 AppData). 일정은 TaskBook, 펫은 PetState로 담는다(07). */
public final class AppData {
    private final Clock clock;
    private long nextSeq = 1;
    private final TaskBook book = new TaskBook(List.of(), () -> nextSeq++, this::today);
    private int points;
    private LocalDate rewardCountDay;
    private int rewardCount;
    private LocalDate attendanceDay;
    private final Set<LocalDate> attendDays = new LinkedHashSet<>();
    private LocalDate lastOpenDay;
    private int streak;
    /** 첫 실행이면 펫이 없고 반려동물 고르기 창(06 30번 choose)이 넣는다(07 seq-uc01). */
    private final List<PetState> pets = new ArrayList<>();
    private PetKind activeKind;
    private final Set<ItemId> ownedItems = new LinkedHashSet<>();
    private final Set<ItemId> placedItems = new LinkedHashSet<>();
    private boolean trayNoticeShown;
    private boolean calendarFixed;
    private boolean listClickTime;
    private final Set<String> sampleIds = new LinkedHashSet<>();
    private final Map<String, SampleLook> sampleLooks = new LinkedHashMap<>();
    private DayOfWeek weekStart = DayOfWeek.MONDAY;
    private final List<Runnable> listeners = new ArrayList<>();

    /** 06 데이터 절의 처음 값인 앱 데이터. 시계는 Main이 컴퓨터 시계를, 검사가 고정 시계를 넘긴다(07 실행 모드). */
    public AppData(Clock clock) {
        this.clock = clock;
    }

    /** 앱의 오늘(06 1번, D-15). */
    public LocalDate today() {
        return AppDay.of(LocalDateTime.now(clock));
    }

    public TaskBook getBook() { return book; }
    /** 다음 일정에 줄 seq. */
    public long getNextSeq() { return nextSeq; }
    /** 보유 포인트. 음수가 될 수 있다(D-19). */
    public int getPoints() { return points; }
    public LocalDate getRewardCountDay() { return rewardCountDay; }
    public int getRewardCount() { return rewardCount; }
    public LocalDate getAttendanceDay() { return attendanceDay; }
    public Set<LocalDate> getAttendDays() { return Collections.unmodifiableSet(attendDays); }
    public LocalDate getLastOpenDay() { return lastOpenDay; }
    public int getStreak() { return streak; }
    /** 함께 키우는 펫. 데려온 순서다. */
    public List<PetState> getPets() { return Collections.unmodifiableList(pets); }
    public PetKind getActiveKind() { return activeKind; }
    public Set<ItemId> getOwnedItems() { return Collections.unmodifiableSet(ownedItems); }
    public Set<ItemId> getPlacedItems() { return Collections.unmodifiableSet(placedItems); }
    public boolean isTrayNoticeShown() { return trayNoticeShown; }
    public boolean isCalendarFixed() { return calendarFixed; }
    public boolean isListClickTime() { return listClickTime; }
    public Set<String> getSampleIds() { return Collections.unmodifiableSet(sampleIds); }
    public Map<String, SampleLook> getSampleLooks() { return Collections.unmodifiableMap(sampleLooks); }
    public DayOfWeek getWeekStart() { return weekStart; }

    /** 펫을 넣는다. 종류마다 하나인지는 06 30번이 본다. */
    public void addPet(PetState pet) { pets.add(pet); }
    public void setActiveKind(PetKind kind) { activeKind = kind; }

    /** 그 종류의 펫. 없으면 null. */
    public PetState pet(PetKind kind) {
        return pets.stream().filter(p -> p.getKind() == kind).findFirst().orElse(null);
    }

    /** 고른 펫(D-48). 고른 종류의 펫이 없으면 첫 펫이고, 펫이 없으면 null이다. */
    public PetState activePet() {
        PetState chosen = pet(activeKind);
        return chosen != null || pets.isEmpty() ? chosen : pets.get(0);
    }

    /** 데이터 변경 알림을 받을 곳을 넣는다(06 22번). */
    public void onChange(Runnable listener) {
        listeners.add(listener);
    }

    /** 데이터 변경 알림. 넣은 순서대로 부른다. */
    public void changed() {
        listeners.forEach(Runnable::run);
    }
}
