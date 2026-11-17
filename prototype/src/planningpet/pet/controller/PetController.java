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

    /** 시작하기(UC-17). 이름은 10자까지, 비우면 나비, 바둑이. */
    public void choose(boolean dog, String name) {
        String n = name.strip();
        String picked = (n.length() > 10 ? n.substring(0, 10) : n);
        control.change(() -> {
            store.petKind = dog ? "DOG" : "CAT";
            store.petName = picked.isEmpty() ? (dog ? "바둑이" : "나비") : picked;
        });
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
