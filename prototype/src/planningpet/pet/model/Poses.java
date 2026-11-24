package planningpet.pet.model;

import java.util.List;

/** D-50의 자세 다섯. */
public final class Poses {
    public static final String SIT = "앉기";
    public static final List<String> ALL = List.of(SIT, "자기", "기지개", "밥 먹기", "노래");

    private Poses() {
    }
}
