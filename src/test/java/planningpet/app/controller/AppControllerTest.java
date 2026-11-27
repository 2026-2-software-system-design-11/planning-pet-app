package planningpet.app.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import planningpet.app.model.AppData;
import planningpet.app.model.DataStore;
import planningpet.app.model.LoadResult;
import planningpet.schedule.model.Category;
import planningpet.schedule.model.Kind;
import planningpet.schedule.model.TaskDraft;

/** 06 22번과 docs/porting/store.md 2절의 E-SAVE 경계. 저장소는 저장한 포인트를 적거나 늘 실패한다(07 실행 모드). */
class AppControllerTest {
    private final AppData data = new AppData(Clock.systemDefaultZone());
    private final List<Integer> saved = new ArrayList<>();
    private final List<String> errors = new ArrayList<>();
    private final List<Integer> heard = new ArrayList<>();
    private boolean failing;
    private final DataStore store = new DataStore() {
        public LoadResult load() { return LoadResult.firstRun(); }
        public void save(AppData d) throws IOException {
            if (failing) {
                throw new IOException("디스크가 찼다");
            }
            saved.add(d.getBook().getTasks().size());
        }
    };
    private final AppController app = new AppController(data, store, errors::add);

    AppControllerTest() {
        data.onChange(() -> heard.add(data.getPoints()));
    }

    private void addTask() {
        LocalDate today = data.today();
        data.getBook().add(new TaskDraft(Kind.TASK, "과제", Category.OTHER, today, null, null, 30, "", null, null,
                false, null, today, false));
    }

    @Test
    void T_SAVE_3_failedAddKeepsTasksAndSeq() {
        failing = true;
        app.change("일정 추가", this::addTask);
        assertTrue(data.getBook().getTasks().isEmpty());
        assertEquals(1, data.getNextSeq());
    }

    @Test
    void E_SAVE_1_savesOnceAndTellsOnce() {
        app.change("일정 추가", this::addTask);
        assertEquals(List.of(1), saved);
        assertEquals(1, heard.size());
        assertEquals(List.of(), errors);
    }
}
