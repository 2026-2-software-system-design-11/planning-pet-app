package planningpet.pet.controller;

import planningpet.app.controller.AppController;
import planningpet.app.model.Store;

/** 펫 controller. 반려동물 고르기와 방에 아이템 놓기를 받아 Model을 바꾼다. */
public final class PetController {
    private final AppController control;
    private final Store store;

    public PetController(AppController control, Store store) {
        this.control = control;
        this.store = store;
    }

    /** 시작하기(UC-17). 이름은 10자까지, 비우면 나비, 바둑이. 첫 실행 창은 앱 창보다 먼저 떠서 알릴 화면이 없다. */
    public static void choose(Store store, boolean dog, String name) {
        String n = name.strip();
        n = n.length() > 10 ? n.substring(0, 10) : n;
        store.petKind = dog ? "DOG" : "CAT";
        store.petName = n.isEmpty() ? (dog ? "바둑이" : "나비") : n;
    }

    /** 방에 놓거나 치운다(06의 20번). */
    public void toggle(String item) {
        control.change(() -> {
            if (!store.placed.remove(item)) {
                store.placed.add(item);
            }
        });
    }
}
