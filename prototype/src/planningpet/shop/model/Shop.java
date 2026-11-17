package planningpet.shop.model;

import planningpet.app.model.Store;
import java.util.LinkedHashMap;
import java.util.Map;

/** 상점(06의 20번): 아이템과 가격, 사기. */
public final class Shop {
    /** D-28의 아이템과 가격. 가격 순이다. */
    public static final Map<String, Integer> ITEMS = new LinkedHashMap<>();

    static {
        String[] names = {"쿠션", "화분", "러그", "액자", "조명", "책장", "책상", "소파"};
        int[] prices = {50, 80, 100, 120, 150, 200, 250, 300};
        for (int i = 0; i < names.length; i++) {
            ITEMS.put(names[i], prices[i]);
        }
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
