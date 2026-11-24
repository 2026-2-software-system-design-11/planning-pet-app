package planningpet.shop.model;

import planningpet.app.model.Store;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 상점(06의 20번): 아이템과 가격, 분류, 사기, 방 꾸미기 만족도. */
public final class Shop {
    public static final List<String> CATEGORIES = List.of("가구", "장식", "장난감", "기타");

    public record Item(String name, int price, String category) {
    }

    /** D-52의 아이템 이름에서 가격으로. 가격 순이다. */
    public static final Map<String, Integer> ITEMS = new LinkedHashMap<>();
    /** D-52의 아이템. ITEMS와 같은 순서다. */
    public static final Map<String, Item> CATALOG = new LinkedHashMap<>();

    static {
        add("털실 공", 40, "장난감");
        add("뼈다귀", 40, "장난감");
        add("쿠션", 50, "기타");
        add("화분", 80, "장식");
        add("러그", 100, "장식");
        add("액자", 120, "장식");
        add("걸이 화분", 130, "장식");
        add("조명", 150, "장식");
        add("책장", 200, "가구");
        add("책상", 250, "가구");
        add("소파", 300, "가구");
        add("캣타워", 350, "가구");
        add("우드 침대", 400, "가구");
    }

    private static void add(String name, int price, String category) {
        ITEMS.put(name, price);
        CATALOG.put(name, new Item(name, price, category));
    }

    private final Store s;

    public Shop(Store store) {
        this.s = store;
    }

    /** 06의 20번. 모자라면 모자란 포인트(양수), 사면 0, 이미 있으면 -1. */
    public int buy(String item) {
        if (s.owned.contains(item)) {
            return -1;
        }
        int price = ITEMS.get(item);
        if (s.points < price) {
            return price - s.points;
        }
        s.points -= price;
        s.owned.add(item);
        s.placed.add(item);
        return 0;
    }

    /** D-52 방 꾸미기 만족도: 놓은 아이템 가격 합 / 모든 아이템 가격 합, 반올림한 백분율. */
    public static int satisfaction(Iterable<String> placed) {
        int sum = 0;
        for (String name : placed) {
            sum += ITEMS.getOrDefault(name, 0);
        }
        int all = ITEMS.values().stream().mapToInt(Integer::intValue).sum();
        return Math.round(sum * 100f / all);
    }

    /** D-53. 분류("전체"면 모두)와 검색어(비면 모두)에 맞는 아이템. ownedOnly면 가진 것만. 가격 순이다. */
    public static List<Item> find(String category, String query, boolean ownedOnly, java.util.Set<String> owned) {
        String q = query == null ? "" : query.strip();
        return CATALOG.values().stream()
                .filter(i -> "전체".equals(category) || i.category().equals(category))
                .filter(i -> q.isEmpty() || i.name().contains(q))
                .filter(i -> !ownedOnly || owned.contains(i.name()))
                .toList();
    }
}
