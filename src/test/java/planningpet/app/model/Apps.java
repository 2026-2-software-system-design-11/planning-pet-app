package planningpet.app.model;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import planningpet.pet.model.PetKind;
import planningpet.pet.model.PetState;
import planningpet.schedule.model.Task;

/** 테스트가 함께 쓰는 고정 시계와 앱 데이터(#518). */
public final class Apps {
    private Apps() {
    }

    /** "2026-11-18T10:00"에 멈춘 시계. 시간대는 이 컴퓨터의 것이다. */
    public static Clock clock(String now) {
        ZoneId zone = ZoneId.systemDefault();
        return Clock.fixed(LocalDateTime.parse(now).atZone(zone).toInstant(), zone);
    }

    /** now에 멈춘 시계, 일정 tasks, 고른 고양이 "나비"가 있는 앱 데이터. */
    public static AppData data(String now, Task... tasks) {
        AppData data = new AppData(clock(now), List.of(tasks), List.of());
        data.addPet(new PetState(PetKind.CAT, "나비"));
        data.setActiveKind(PetKind.CAT);
        return data;
    }
}
