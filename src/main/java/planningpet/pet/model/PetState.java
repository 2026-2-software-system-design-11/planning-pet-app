package planningpet.pet.model;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/** 함께 키우는 펫 하나(06 데이터 절 `PetState`, D-48). 종류마다 하나라 종류로 알아본다. */
public final class PetState {
    private final PetKind kind;
    private String name;
    private int exp;
    private Pose pose = Pose.SIT;
    /** 칸마다 입은 장신구. 안 입은 칸은 없다(D-51). */
    private final Map<Slot, WearId> wear = new EnumMap<>(Slot.class);

    /** 새 펫. 경험치 0, 앉기 자세, 기본 목걸이다(D-50, D-51, 06 30번). 이름 규칙(D-27)은 만드는 쪽이 지킨다. */
    public PetState(PetKind kind, String name) {
        this.kind = kind;
        this.name = name;
        wear.put(Slot.NECK, WearId.BASIC_NECKLACE);
    }

    public PetKind getKind() { return kind; }
    public String getName() { return name; }
    /** 누적 경험치. 레벨과 모습 단계는 이 값으로 06 10번이 구한다. */
    public int getExp() { return exp; }
    public Pose getPose() { return pose; }
    /** 칸마다 입은 장신구. 바꿀 수 없는 모음이다. */
    public Map<Slot, WearId> getWear() { return Collections.unmodifiableMap(wear); }
}
