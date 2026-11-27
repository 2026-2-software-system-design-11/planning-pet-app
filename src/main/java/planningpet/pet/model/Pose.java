package planningpet.pet.model;

/** 펫의 자세(06 데이터 절 `PetState.pose`, D-50). 순서는 캐릭터 창의 자세 칸 순서다. */
public enum Pose {
    SIT("앉기"),
    SLEEP("자기"),
    STRETCH("기지개"),
    EAT("밥 먹기"),
    SING("노래");

    private final String label;

    Pose(String label) {
        this.label = label;
    }

    /** 화면에 보이는 이름. */
    public String label() {
        return label;
    }
}
