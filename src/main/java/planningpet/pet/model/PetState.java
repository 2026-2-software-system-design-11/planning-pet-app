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

    /** 나의 방에서 이 펫을 누르면 그 펫 위 말풍선에 한 번 보이는 말(06 16번 PETTED, D-80). 아무것도 바꾸지 않는다. */
    public String pat() {
        return kind == PetKind.DOG ? "멍멍, 좋아!" : "야옹, 좋아!";
    }

    // 값 하나를 넣는 메서드. 저장의 읽기가 쓰고, 규칙(06 10번, 28번, 30번)은 그 규칙을 옮기는 단위가 이것을 부르는 메서드로 더한다.
    public void setName(String name) { this.name = name; }
    public void setExp(int exp) { this.exp = exp; }
    public void setPose(Pose pose) { this.pose = pose; }
    /** 입은 장신구를 이 모음으로 바꾼다. 안 입은 칸은 넣지 않는다(D-51). */
    public void setWear(Map<Slot, WearId> wear) {
        this.wear.clear();
        this.wear.putAll(wear);
    }

    /** 값이 같은 새 펫(06 22번의 되돌리기에 쓰는 사본). */
    public PetState copy() {
        PetState c = new PetState(kind, name);
        c.restore(this);
        return c;
    }

    /** 같은 종류인 from의 이름, 경험치, 자세, 장신구를 넣는다. 되돌려도 이 객체가 그 펫이다(docs/porting/store.md). */
    public void restore(PetState from) {
        name = from.name;
        exp = from.exp;
        pose = from.pose;
        setWear(from.wear);
    }
}
