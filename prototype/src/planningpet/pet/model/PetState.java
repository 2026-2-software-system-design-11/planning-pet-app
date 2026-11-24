package planningpet.pet.model;

import java.util.LinkedHashMap;
import java.util.Map;

/** 함께 키우는 펫 하나(D-48). 종류마다 한 마리라 종류로 알아본다. */
public final class PetState {
    public final String kind;
    public String name;
    public int exp;
    /** D-50의 자세. */
    public String pose = Poses.SIT;
    /** D-51. 칸 이름에서 입힌 장신구 이름으로. 안 입은 칸은 없다. */
    public final Map<String, String> wear = new LinkedHashMap<>();

    public PetState(String kind, String name) {
        this.kind = kind;
        this.name = name;
        wear.put(Wardrobe.NECK, "기본 목걸이");
    }

    public boolean dog() {
        return "DOG".equals(kind);
    }

    /** "고양이" 또는 "강아지". */
    public String kindName() {
        return dog() ? "강아지" : "고양이";
    }

    /** D-27. 이름을 비우면 붙는 이름. */
    public static String defaultName(String kind) {
        return "DOG".equals(kind) ? "바둑이" : "나비";
    }
}
