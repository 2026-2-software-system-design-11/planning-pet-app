package planningpet.schedule.model;

import java.util.List;

/** D-54의 일정 유형. */
public final class Categories {
    public static final String OTHER = "기타";
    public static final List<String> ALL = List.of("학업", "운동", "프로젝트", "개인/취미", OTHER);

    private Categories() {
    }
}
