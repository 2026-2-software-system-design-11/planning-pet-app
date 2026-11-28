package planningpet.pet.controller;

import java.util.List;
import planningpet.app.controller.AppController;
import planningpet.app.model.AppData;
import planningpet.pet.model.PetKind;
import planningpet.pet.model.PetState;
import planningpet.pet.model.PetTalk;
import planningpet.pet.model.Pose;
import planningpet.pet.model.Situation;

/**
 * 펫 controller(06 30번, 07). 반려동물 고르기, 데려오기, 고른 펫 바꾸기를 받아 22번 change로 감싼다. choose의 examples 인자는
 * C5가, step, rename, pose, applyWear는 S8이 더한다(docs/porting/pet-controller.md).
 */
public final class PetController {
    private final AppData data;
    private final AppController app;
    private final PetTalk talk;

    public PetController(AppData data, AppController app) {
        this.data = data;
        this.app = app;
        this.talk = new PetTalk(data);
    }

    /** D-27: 앞뒤 공백을 지우고 10자까지, 비우면 고양이는 나비, 강아지는 바둑이. choose, adopt, rename이 같이 쓴다. */
    static String nameOf(PetKind kind, String name) {
        String n = name.strip();
        // 06 읽는 법: 글자 수는 코드 포인트로 센다. 프로토타입은 UTF-16 단위로 잘라 이모지가 반쪽만 남았다.
        String picked = n.codePointCount(0, n.length()) > 10 ? n.substring(0, n.offsetByCodePoints(0, 10)) : n;
        return !picked.isEmpty() ? picked : kind == PetKind.DOG ? "바둑이" : "나비";
    }

    /** UC-17 바로 시작. 펫 목록을 이 펫 하나로 바꾸고 고른다(프로토타입과 같다). 말하지 않는다(첫 말은 15번 FIRST_RUN). */
    public void choose(PetKind kind, String name) {
        app.change("반려동물 고르기", () -> {
            data.clearPets();
            data.addPet(new PetState(kind, nameOf(kind, name)));
            data.setActiveKind(kind);
        });
    }

    /**
     * UC-20(D-48). 그 종류가 없으면 펫을 만들어 넣고 고르며, 새 펫이 ADOPTED를 메인과 나의 방 말풍선에 말한다(D-56, D-62). 이미 있으면
     * 아무것도 하지 않고 false다. 저장에 실패해 되돌렸으면 데려오지 않았으므로 false다(06 30번 출력).
     */
    public boolean adopt(PetKind kind, String name) {
        if (data.pet(kind) != null) {
            return false;
        }
        app.change("펫 데려오기", () -> {
            PetState pet = new PetState(kind, nameOf(kind, name));
            data.addPet(pet);
            data.setActiveKind(kind);
            String line = talk.say(Situation.ADOPTED, pet);
            app.say(line, null);
            app.sayInRoom(line);
        });
        return data.pet(kind) != null;
    }

    /**
     * UC-21(D-48). 그 종류의 펫이 있고 지금 고른 펫이 아니면 고르고, 새 펫이 SELECTED를 말한다(D-62). 그 말은 오늘 목록을 따라 다시
     * 세면 USUAL이 된다(06 16번 다시 세기). 없거나 이미 고른 펫이면 아무것도 하지 않는다.
     */
    public void select(PetKind kind) {
        if (data.pet(kind) == null || data.activePet().getKind() == kind) {
            return;
        }
        app.change("고른 펫 바꾸기", () -> {
            data.setActiveKind(kind);
            app.say(talk.say(Situation.SELECTED), data.today());
        });
    }

    /** UC-21 화살표(D-80). pets 차례에서 앞(-1)이나 다음(+1) 펫을 고르고 끝에서 처음으로 돈다. 고르기와 말은 select와 같다. */
    public void step(int delta) {
        List<PetState> all = data.getPets();
        select(all.get(Math.floorMod(all.indexOf(data.activePet()) + delta, all.size())).getKind());
    }

    /** UC-23(D-57). D-27 규칙으로 이름을 바꾸고 그 펫이 RENAMED를 메인 말풍선에 말한다(D-62). 종류는 바꾸지 않는다. */
    public void rename(PetKind kind, String name) {
        PetState pet = data.pet(kind);
        app.change("펫 이름 바꾸기", () -> {
            pet.setName(nameOf(kind, name));
            app.say(talk.say(Situation.RENAMED, pet), null);
        });
    }

    /** UC-22(D-50). 다섯 자세 가운데 하나일 때만 바꾼다. 다섯 밖의 값은 Pose로 넘길 수 없어 null이 그 자리이고 아무것도 하지 않는다. */
    public void pose(PetKind kind, Pose pose) {
        if (pose == null) {
            return;
        }
        PetState pet = data.pet(kind);
        app.change("자세 고르기", () -> pet.setPose(pose));
    }
}
