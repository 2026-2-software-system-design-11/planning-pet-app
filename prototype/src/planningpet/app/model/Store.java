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
    public final TaskBook book = new TaskBook(tasks, () -> nextSeq++, this::today);
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
    /** 설정 "달력에 고정 일정 보이기"(D-86). 처음은 끔. */
    public boolean calendarFixed;
    /** 첫 실행의 "예시로 둘러보기"로 넣은 예시 일정의 id(D-74). 예시 지우기가 지우고 비운다. */
    public final Set<String> sampleIds = new LinkedHashSet<>();
    /** 예시 일정마다 넣을 때의 제목, 시각, 메모, 날짜(D-94). 이와 다르면 사용자가 고친 예시라 예시 지우기가 남긴다. */
    public final java.util.Map<String, String> sampleLooks = new java.util.HashMap<>();
    /** 설정 "한 주 시작"(D-91). 앱의 한 주는 이 요일부터 7일이다. 처음은 월요일. */
    public java.time.DayOfWeek weekStart = java.time.DayOfWeek.MONDAY;
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

    /**
     * 홈 말풍선에만 남기는 말(D-94). 새 말 번호(talkSeq)를 올리지 않아, 일정관리 말풍선은 새 말로 보지 않고 D-39대로 힌트를 다시
     * 정한다.
     */
    public void sayAtHome(String line) {
        talk = line;
    }

    public void changed() {
        listeners.forEach(Runnable::run);
    }
}
