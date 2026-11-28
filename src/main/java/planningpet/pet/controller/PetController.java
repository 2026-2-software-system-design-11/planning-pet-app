package planningpet.pet.controller;

import planningpet.pet.model.PetKind;

/**
 * 펫 controller(06 30번, 07). 반려동물 고르기, 데려오기, 고른 펫 바꾸기를 받아 22번 change로 감싼다. choose의 examples 인자는
 * C5가, step, rename, pose, applyWear는 S8이 더한다(docs/porting/pet-controller.md).
 */
public final class PetController {
    /** D-27: 앞뒤 공백을 지우고 10자까지, 비우면 고양이는 나비, 강아지는 바둑이. choose, adopt, rename이 같이 쓴다. */
    static String nameOf(PetKind kind, String name) {
        String n = name.strip();
        String picked = n.length() > 10 ? n.substring(0, 10) : n;
        return !picked.isEmpty() ? picked : kind == PetKind.DOG ? "바둑이" : "나비";
    }
}
