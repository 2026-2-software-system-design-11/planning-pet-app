package planningpet.pet.model;

/** 펫의 종류(06 데이터 절 `PetState.kind`, D-48). */
public enum PetKind {
    CAT("고양이"),
    DOG("강아지");

    private final String label;

    PetKind(String label) {
        this.label = label;
    }

    /** 화면에 보이는 이름. 이름 옆에 작게 적는다(D-81). */
    public String label() {
        return label;
    }
}
