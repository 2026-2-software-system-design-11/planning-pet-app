package planningpet.pet.model;

/** 장신구 표(D-51). 장신구마다 입는 칸과 열리는 레벨이다. 칸 안의 카드 순서는 WearId 순서다. */
public final class Wardrobe {
    private Wardrobe() {
    }

    /** 이 장신구가 열리는 펫 레벨(06 10번). 펫 레벨이 이 값 이상이면 입을 수 있다. */
    public static int opensAt(WearId wear) {
        return switch (wear) {
            case BASIC_NECKLACE -> 1;
            case FLOWER_PIN, SCARF -> 2;
            case RIBBON_NECKLACE, STRAW_HAT -> 3;
            case RIBBON_PIN -> 4;
            case STAR_NECKLACE, VEST -> 5;
            case BERET -> 6;
            case GLASSES -> 7;
            case CAPE -> 8;
            case CROWN -> 9;
            case CROWN_NECKLACE -> 10;
        };
    }

    /** 이 장신구를 입는 칸. */
    public static Slot slotOf(WearId wear) {
        return switch (wear) {
            case BASIC_NECKLACE, RIBBON_NECKLACE, STAR_NECKLACE, CROWN_NECKLACE -> Slot.NECK;
            case FLOWER_PIN, RIBBON_PIN, GLASSES -> Slot.ACCESSORY;
            case STRAW_HAT, BERET, CROWN -> Slot.HAT;
            case SCARF, VEST, CAPE -> Slot.CLOTHES;
        };
    }
}
