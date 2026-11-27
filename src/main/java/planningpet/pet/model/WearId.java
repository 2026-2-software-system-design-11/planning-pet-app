package planningpet.pet.model;

/** 장신구(D-51). 칸 순서이고 칸 안에서는 열리는 레벨 순서다. 칸과 레벨은 `Wardrobe`(M14)가 정한다. */
public enum WearId {
    BASIC_NECKLACE("기본 목걸이"),
    RIBBON_NECKLACE("리본 목걸이"),
    STAR_NECKLACE("별 목걸이"),
    CROWN_NECKLACE("왕관 목걸이"),
    FLOWER_PIN("꽃 핀"),
    RIBBON_PIN("리본 핀"),
    GLASSES("안경"),
    STRAW_HAT("밀짚모자"),
    BERET("베레모"),
    CROWN("왕관"),
    SCARF("스카프"),
    VEST("조끼"),
    CAPE("망토");

    private final String label;

    WearId(String label) {
        this.label = label;
    }

    /** 화면에 보이는 이름. */
    public String label() {
        return label;
    }
}
