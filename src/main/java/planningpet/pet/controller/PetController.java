package planningpet.pet.controller;

import planningpet.app.controller.AppController;
import planningpet.app.model.AppData;
import planningpet.pet.model.PetKind;
import planningpet.pet.model.PetState;

/**
 * 펫 controller(06 30번, 07). 반려동물 고르기, 데려오기, 고른 펫 바꾸기를 받아 22번 change로 감싼다. choose의 examples 인자는
 * C5가, step, rename, pose, applyWear는 S8이 더한다(docs/porting/pet-controller.md).
 */
public final class PetController {
    private final AppData data;
    private final AppController app;

    public PetController(AppData data, AppController app) {
        this.data = data;
        this.app = app;
    }

    /** D-27: 앞뒤 공백을 지우고 10자까지, 비우면 고양이는 나비, 강아지는 바둑이. choose, adopt, rename이 같이 쓴다. */
    static String nameOf(PetKind kind, String name) {
        String n = name.strip();
        String picked = n.length() > 10 ? n.substring(0, 10) : n;
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
}
