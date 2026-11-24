package planningpet.app.model;

import planningpet.common.model.Rules;
import planningpet.pet.model.PetState;
import planningpet.schedule.model.Task;
import planningpet.schedule.model.TaskBook;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** 메모리에만 있는 앱 데이터(06 "데이터" 표의 AppData). 저장하지 않는다. */
public final class Store {
    public final List<Task> tasks = new ArrayList<>();
    /** 다음 일정에 줄 seq(06 "데이터" 표). */
    public long nextSeq = 1;
    /** 일정 다루기(06의 3~7번, 26번). 위 tasks와 nextSeq를 다룬다. */
    public final TaskBook book = new TaskBook(tasks, () -> nextSeq++);
    public LocalDateTime now;
    public int points;
    public LocalDate rewardCountDay;
    public int rewardCount;
    public LocalDate attendanceDay;
    /** 출석한 날(D-55 통계의 출석 포인트). */
    public final Set<LocalDate> attendDays = new LinkedHashSet<>();
    public LocalDate lastOpenDay;
    public int streak;
    /** D-48. 함께 키우는 펫. 첫 실행 전에는 기본 고양이 하나다. */
    public final List<PetState> pets = new ArrayList<>(List.of(new PetState("CAT", "나비")));
    /** 고른 펫의 종류. */
    public String activeKind = "CAT";
    public final Set<String> owned = new LinkedHashSet<>();
    public final Set<String> placed = new LinkedHashSet<>();
    public String talk = "";
    /** 펫이 새 말을 할 때마다 1씩 는다. 같은 문장을 다시 말해도 새 말로 알아본다. */
    public int talkSeq;
    private final List<Runnable> listeners = new ArrayList<>();

    /** 고른 펫(D-48). */
    public PetState pet() {
        return pet(activeKind) != null ? pet(activeKind) : pets.get(0);
    }

    /** 그 종류의 펫. 없으면 null. */
    public PetState pet(String kind) {
        return pets.stream().filter(p -> p.kind.equals(kind)).findFirst().orElse(null);
    }

    public LocalDate today() {
        return Rules.appDay(now);
    }

    public void onChange(Runnable listener) {
        listeners.add(listener);
    }

    /** 변경 알림을 받는 부품 수(검사용). */
    public int listenerCount() {
        return listeners.size();
    }

    /** 펫이 새 말을 한다. */
    public void say(String line) {
        talk = line;
        talkSeq++;
    }

    public void changed() {
        listeners.forEach(Runnable::run);
    }
}
