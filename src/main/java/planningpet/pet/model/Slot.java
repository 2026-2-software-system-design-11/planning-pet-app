package planningpet.pet.model;

/** 장신구 칸(06 데이터 절 `PetState.wear`, D-51). 순서는 꾸미기 탭의 칸 순서다. */
public enum Slot {
    NECK("목걸이"),
    ACCESSORY("액세서리"),
    HAT("모자"),
    CLOTHES("의상");

    private final String label;

    Slot(String label) {
        this.label = label;
    }

    /** 화면에 보이는 이름. */
    public String label() {
        return label;
    }
}
