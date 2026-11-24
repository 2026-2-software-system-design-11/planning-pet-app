package planningpet.pet.controller;

import planningpet.app.controller.AppController;
import planningpet.app.model.Store;
import planningpet.pet.model.PetState;
import planningpet.pet.model.Poses;
import planningpet.pet.model.Wardrobe;
import planningpet.reward.model.Level;
import java.util.Map;

/** 펫 controller. 반려동물 고르기와 데려오기, 고른 펫, 이름, 자세, 장신구, 방에 아이템 놓기를 받아 Model을 바꾼다. */
public final class PetController {
    private final AppController control;
    private final Store store;

    public PetController(AppController control, Store store) {
        this.control = control;
        this.store = store;
    }

    private static String nameOf(boolean dog, String name) {
        String n = name.strip();
        String picked = n.length() > 10 ? n.substring(0, 10) : n;
        return picked.isEmpty() ? PetState.defaultName(dog ? "DOG" : "CAT") : picked;
    }

    /** 시작하기(UC-17). 이름은 10자까지, 비우면 나비, 바둑이. */
    public void choose(boolean dog, String name) {
        control.change(() -> {
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
