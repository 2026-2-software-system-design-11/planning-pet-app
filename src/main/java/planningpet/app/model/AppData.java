package planningpet.app.model;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import planningpet.common.model.AppDay;
import planningpet.pet.model.PetKind;
import planningpet.pet.model.PetState;
import planningpet.schedule.model.TaskBook;

/** 앱 하나에 하나뿐인 값(06 데이터 절 AppData). 일정은 TaskBook, 펫은 PetState로 담는다(07). */
public final class AppData {
    private final Clock clock;
    private long nextSeq = 1;
    private final TaskBook book = new TaskBook(List.of(), () -> nextSeq++, this::today);
    private final List<PetState> pets = new ArrayList<>(List.of(new PetState(PetKind.CAT, "나비")));
    private PetKind activeKind = PetKind.CAT;
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
    /** 함께 키우는 펫. 데려온 순서다. */
    public List<PetState> getPets() { return Collections.unmodifiableList(pets); }
    public PetKind getActiveKind() { return activeKind; }

    /** 펫을 넣는다. 종류마다 하나인지는 06 30번이 본다. */
    public void addPet(PetState pet) { pets.add(pet); }
    public void setActiveKind(PetKind kind) { activeKind = kind; }

    /** 그 종류의 펫. 없으면 null. */
    public PetState pet(PetKind kind) {
        return pets.stream().filter(p -> p.getKind() == kind).findFirst().orElse(null);
    }

    /** 고른 펫(D-48). 고른 종류의 펫이 없으면 첫 펫이다. */
    public PetState activePet() {
        PetState chosen = pet(activeKind);
        return chosen != null ? chosen : pets.get(0);
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
