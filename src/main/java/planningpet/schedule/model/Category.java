package planningpet.schedule.model;

/** 일정의 유형(D-54). 순서는 화면 목록과 통계(06 29번)의 순서다. */
public enum Category {
    STUDY("학업"),
    EXERCISE("운동"),
    PROJECT("프로젝트"),
    PERSONAL("개인/취미"),
    OTHER("기타");

    private final String label;

    Category(String label) {
        this.label = label;
    }

    /** 화면에 보이는 이름. */
    public String label() {
        return label;
    }
}
