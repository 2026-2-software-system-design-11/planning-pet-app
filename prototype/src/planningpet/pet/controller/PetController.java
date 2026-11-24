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
            store.pets.clear();
            store.pets.add(new PetState(dog ? "DOG" : "CAT", nameOf(dog, name)));
            store.activeKind = dog ? "DOG" : "CAT";
        });
    }

    /** D-48. 남은 종류를 데려오고 그 펫을 고른다. 이미 있으면 false. */
    public boolean adopt(boolean dog, String name) {
        String kind = dog ? "DOG" : "CAT";
        if (store.pet(kind) != null) {
            return false;
        }
        control.change(() -> {
            store.pets.add(new PetState(kind, nameOf(dog, name)));
            store.activeKind = kind;
            store.say("안녕, 나는 " + store.pet().name + planningpet.common.model.Rules.iya(store.pet().name) + ". 잘 부탁해!");
        });
        return true;
    }

    /** D-48. 고른 펫을 바꾼다. */
    public void select(String kind) {
        if (store.pet(kind) == null || kind.equals(store.activeKind)) {
            return;
        }
        control.change(() -> store.activeKind = kind);
    }

    /** D-56. 방 화살표: 다음(+1) 또는 앞(-1) 펫을 고른다. */
    public void step(int delta) {
        int n = store.pets.size();
        int i = store.pets.indexOf(store.pet());
        select(store.pets.get(Math.floorMod(i + delta, n)).kind);
    }

    /** 이름 변경(D-27의 이름 규칙). */
    public void rename(String kind, String name) {
        PetState p = store.pet(kind);
        control.change(() -> p.name = nameOf(p.dog(), name));
    }

    /** D-50. 자세를 고른다. */
    public void pose(String kind, String pose) {
        if (!Poses.ALL.contains(pose)) {
            return;
        }
        PetState p = store.pet(kind);
        control.change(() -> p.pose = pose);
    }

    /** D-51 꾸미기 적용. 열리지 않은 장신구는 빼고 저장한다. 값이 null인 칸은 벗긴다. */
    public void applyWear(String kind, Map<String, String> chosen) {
        PetState p = store.pet(kind);
        int level = Level.level(p.exp);
        control.change(() -> {
            p.wear.clear();
            chosen.forEach((slot, item) -> {
                Wardrobe.Wear w = item == null ? null : Wardrobe.ALL.get(item);
                if (w != null && w.slot().equals(slot) && w.level() <= level) {
                    p.wear.put(slot, item);
                }
            });
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
