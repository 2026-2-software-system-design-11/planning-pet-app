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
}
