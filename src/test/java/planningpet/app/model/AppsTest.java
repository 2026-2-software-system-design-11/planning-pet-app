package planningpet.app.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static planningpet.schedule.model.Tasks.d;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import planningpet.pet.model.PetKind;
import planningpet.schedule.model.Tasks;

/** 공용 고정 시계와 앱 데이터 Apps(#518). */
class AppsTest {
    @Test
    void clockStopsAtTheLocalTime() {
        assertEquals(LocalDateTime.of(2026, 11, 18, 23, 59), LocalDateTime.now(Apps.clock("2026-11-18T23:59")));
    }

    @Test
    void dataHasTasksAndChosenCat() {
        AppData data = Apps.data("2026-11-18T10:00", Tasks.task("운동", d(18)).task());
        assertEquals(List.of(d(18), "운동", "나비", PetKind.CAT), List.of(data.today(),
                data.getBook().getTasks().get(0).getTitle(), data.getPets().get(0).getName(), data.getActiveKind()));
    }
}
