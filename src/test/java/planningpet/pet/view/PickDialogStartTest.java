package planningpet.pet.view;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import planningpet.app.controller.AppController;
import planningpet.app.model.AppData;
import planningpet.app.model.DataStore;
import planningpet.app.model.LoadResult;
import planningpet.pet.controller.PetController;
import planningpet.pet.model.PetKind;

/** 고르기 창의 바로 시작, Enter, 닫기. 번호는 docs/porting/pick.md 2절의 E-PWIN. 지금은 11-16 10:00, 첫 실행이다. */
class PickDialogStartTest {
    private final AppData data = new AppData(Clock.fixed(
            LocalDateTime.of(2026, 11, 16, 10, 0).atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault()));
    /** 저장, 오류 창의 글, 창 닫기, 고른 뒤 알림을 일어난 차례로 적는다. */
    private final List<String> calls = new ArrayList<>();
    private boolean failing;
    private final AppController app = new AppController(data, new DataStore() {
        public LoadResult load() { return LoadResult.firstRun(); }
        public void save(AppData d) throws IOException {
            if (failing) {
                throw new IOException("디스크가 찼다");
            }
            calls.add("저장");
        }
    }, calls::add);
    private final PickDialog pick = new PickDialog();

    PickDialogStartTest() {
        pick.close = () -> calls.add("닫기");
        pick.onStart(new PetController(data, app), data, () -> calls.add("고름"));
    }

    private List<String> pets() {
        return data.getPets().stream().map(p -> p.getKind() + " " + p.getName()).toList();
    }

    @Test
    void ePwin1StartWithEmptyName() {
        pick.dog.doClick();
        pick.start.doClick();
        assertEquals(List.of("DOG 바둑이"), pets());
        assertEquals(PetKind.DOG, data.getActiveKind());
        assertEquals(List.of("저장", "닫기", "고름"), calls);
    }

    @Test
    void ePwin3ExamplesDoNothingYet() {
        pick.examples.doClick();
        assertEquals(List.of(), pets());
        assertEquals(List.of(), calls);
    }

    @Test
    void ePwin4SaveFailedKeepsWindow() {
        failing = true;
        pick.dog.doClick();
        pick.start.doClick();
        assertEquals(List.of(), pets());
        assertEquals(List.of("반려동물 고르기 저장에 실패해서 바꾸기 전으로 돌렸어요."), calls);
        failing = false;
        pick.start.doClick();
        assertEquals(List.of("DOG 바둑이"), pets());
        assertEquals(List.of("반려동물 고르기 저장에 실패해서 바꾸기 전으로 돌렸어요.", "저장", "닫기", "고름"), calls);
    }
}
