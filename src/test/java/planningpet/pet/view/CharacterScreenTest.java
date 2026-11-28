package planningpet.pet.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import planningpet.app.controller.AppController;
import planningpet.app.model.AppData;
import planningpet.app.model.Apps;
import planningpet.app.model.MemoryStore;
import planningpet.common.view.Pill;
import planningpet.common.view.Snap;
import planningpet.common.view.ThemeOn;
import planningpet.pet.controller.PetController;
import planningpet.pet.model.PetKind;
import planningpet.pet.model.PetState;
import planningpet.pet.model.Pose;

/**
 * 캐릭터 창 왼쪽 열의 펫 탭. 번호는 docs/porting/character.md S8e 2절. 고양이 나비(경험치 0)를 고른 앱
 * 데이터에서 시작한다.
 */
@ExtendWith(ThemeOn.class)
class CharacterScreenTest {
    final AppData data = Apps.data("2026-11-18T10:00");
    final MemoryStore store = new MemoryStore();
    final List<String> errors = new ArrayList<>();
    final PetController pets = new PetController(data, new AppController(data, store, errors::add));

    /** 강아지 바둑이(경험치 0)를 더하고 창 안을 만들어 그린 상태로 돌려준다. */
    CharacterScreen withDog() {
        data.addPet(new PetState(PetKind.DOG, "바둑이"));
        return screen();
    }

    CharacterScreen screen() {
        CharacterScreen s = new CharacterScreen(data, pets);
        s.refresh();
        return s;
    }

    static List<Pill> tabs(CharacterScreen s) {
        List<Pill> out = new ArrayList<>();
        for (Component c : s.petTabs.getComponents()) {
            out.add((Pill) c);
        }
        return out;
    }

    static String tab(Pill p) {
        return p.getText() + " " + p.small() + " " + p.second() + " " + p.isSelected();
    }

    static String size(Pill p) {
        return p.getPreferredSize().width + "x" + p.getPreferredSize().height;
    }

    /** 창 밖에서 같은 글로 만든 펫 탭의 크기. Pill은 내용 크기를 알맞은 크기로 내 setPreferredSize(폭 200 이상)가 먹지 않는다. */
    static String alone(String name, PetKind kind, int level) {
        return size(new Pill(name, null, 16).small(kind.label()).picture(PetArt.face(kind, Set.of())).second("Lv. " + level));
    }

    @Test
    void eChr1OneAndTwoTabs() {
        List<Pill> one = tabs(screen());
        assertEquals(List.of("나비 고양이 Lv. 1 true"), one.stream().map(CharacterScreenTest::tab).toList());
        assertEquals(List.of(alone("나비", PetKind.CAT, 1)), one.stream().map(CharacterScreenTest::size).toList());
        assertTrue(one.get(0).getPreferredSize().width < 200);
        assertEquals(58, one.get(0).getPreferredSize().height);
        data.clearPets();
        PetState dog = new PetState(PetKind.DOG, "가나다라마바사아자차");
        dog.setExp(130);
        data.addPet(dog);
        data.addPet(new PetState(PetKind.CAT, "나비"));
        data.setActiveKind(PetKind.DOG);
        List<Pill> two = tabs(screen());
        assertEquals(List.of("나비 고양이 Lv. 1 false", "가나다라마바사아자차 강아지 Lv. 3 true"),
                two.stream().map(CharacterScreenTest::tab).toList());
        assertEquals(List.of(alone("나비", PetKind.CAT, 1), alone(dog.getName(), PetKind.DOG, 3)),
                two.stream().map(CharacterScreenTest::size).toList());
    }

    @Test
    void eChr2PressTab() {
        CharacterScreen s = withDog();
        Pill cat = tabs(s).get(0);
        cat.doClick(0);
        assertEquals(List.of(PetKind.CAT, 0), List.of(data.getActiveKind(), store.saves));
        assertNotSame(cat, tabs(s).get(0));
        assertTrue(tabs(s).get(0).isSelected());
        tabs(s).get(1).doClick(0);
        assertEquals(List.of(PetKind.DOG, 1), List.of(data.getActiveKind(), store.saves));
        assertEquals(List.of(false, true), tabs(s).stream().map(Pill::isSelected).toList());
    }

    @Test
    void eChr10BigPetBoxAndGround() {
        assertEquals(List.of(222, 270), List.of(CharacterScreen.petSize(400, 300), CharacterScreen.ground(300)));
        assertEquals(List.of(186, 360), List.of(CharacterScreen.petSize(300, 400), CharacterScreen.ground(400)));
        PetState cat = data.activePet();
        assertEquals(140, CharacterScreen.drawnHeight(cat, 222));
        cat.setExp(990);
        cat.setPose(Pose.SLEEP);
        assertEquals(113, CharacterScreen.drawnHeight(cat, 222));
        // 방 그림, 러그, 펫, 털실 공, 하트를 그려도 멈추지 않고 무대 가운데가 칠해진다(그림 비교는 하지 않는다).
        assertTrue(Snap.paint(screen().stage, 400, 300).getRGB(200, 150) >>> 24 > 0);
    }
}
