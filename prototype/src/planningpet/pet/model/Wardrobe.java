package planningpet.pet.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** D-51의 장신구. 칸마다 열리는 레벨 순이다. */
public final class Wardrobe {
    public static final String NECK = "목걸이";
    public static final String ACCESSORY = "액세서리";
    public static final String HAT = "모자";
    public static final String CLOTHES = "의상";
    public static final List<String> SLOTS = List.of(NECK, ACCESSORY, HAT, CLOTHES);

    public record Wear(String name, String slot, int level) {
    }

    /** 장신구 이름에서 장신구로. 칸 안에서는 레벨 순이다. */
    public static final Map<String, Wear> ALL = new LinkedHashMap<>();

    static {
        add(NECK, "기본 목걸이", 1, "리본 목걸이", 3, "별 목걸이", 5, "왕관 목걸이", 10);
        add(ACCESSORY, "꽃 핀", 2, "리본 핀", 4, "안경", 7);
        add(HAT, "밀짚모자", 3, "베레모", 6, "왕관", 9);
        add(CLOTHES, "스카프", 2, "조끼", 5, "망토", 8);
    }

    private Wardrobe() {
    }

    private static void add(String slot, Object... pairs) {
        for (int i = 0; i < pairs.length; i += 2) {
            String name = (String) pairs[i];
            ALL.put(name, new Wear(name, slot, (Integer) pairs[i + 1]));
        }
    }

    public static List<Wear> inSlot(String slot) {
        return ALL.values().stream().filter(w -> w.slot().equals(slot)).toList();
    }
}
