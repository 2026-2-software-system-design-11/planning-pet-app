package planningpet.shop.model;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import planningpet.app.model.AppData;

/** 상점(06 20번): 아이템과 가격, 분류, 사기, 되팔기, 찾기, 방 꾸미기 만족도. 포인트와 아이템은 펫 둘이 함께 쓴다(D-48). */
public final class Shop {
    /** D-52의 아이템. `ItemId` 순서라 가격 순이다. */
    private static final List<Item> ITEMS = List.of(
            new Item(ItemId.YARN_BALL, 40, "장난감"),
            new Item(ItemId.BONE, 40, "장난감"),
            new Item(ItemId.CUSHION, 50, "기타"),
            new Item(ItemId.PLANT, 80, "장식"),
            new Item(ItemId.RUG, 100, "장식"),
            new Item(ItemId.FRAME, 120, "장식"),
            new Item(ItemId.HANGING_PLANT, 130, "장식"),
            new Item(ItemId.LAMP, 150, "장식"),
            new Item(ItemId.BOOKSHELF, 200, "가구"),
            new Item(ItemId.DESK, 250, "가구"),
            new Item(ItemId.SOFA, 300, "가구"),
            new Item(ItemId.CAT_TOWER, 350, "가구"),
            new Item(ItemId.WOOD_BED, 400, "가구"));
    private static final int ALL_PRICES = ITEMS.stream().mapToInt(Item::price).sum();

    private final AppData data;

    public Shop(AppData data) {
        this.data = data;
    }

    private static int price(ItemId id) {
        return ITEMS.get(id.ordinal()).price();
    }

    /** 06 20번. 가진 아이템이면 포인트를 보지 않는다. 사면 가격을 빼고 보유하고 방에 놓는다(D-28). */
    public BuyResult buy(ItemId item) {
        if (data.getOwnedItems().contains(item)) {
            return new BuyResult(BuyResult.Outcome.ALREADY_OWNED, 0);
        }
        int price = price(item);
        if (data.getPoints() < price) {
            return new BuyResult(BuyResult.Outcome.NOT_ENOUGH, price - data.getPoints());
        }
        data.setPoints(data.getPoints() - price);
        data.setOwnedItems(with(data.getOwnedItems(), item));
        data.setPlacedItems(with(data.getPlacedItems(), item));
        return new BuyResult(BuyResult.Outcome.BOUGHT, 0);
    }

    /** 06 20번, D-85. 가진 아이템을 산 값 그대로 되판다. 방에 놓였으면 함께 뺀다. 돌려준 포인트, 가지지 않았으면 -1. */
    public int sell(ItemId item) {
        if (!data.getOwnedItems().contains(item)) {
            return -1;
        }
        int price = price(item);
        data.setPoints(data.getPoints() + price);
        data.setOwnedItems(without(data.getOwnedItems(), item));
        data.setPlacedItems(without(data.getPlacedItems(), item));
        return price;
    }

    private static Set<ItemId> without(Set<ItemId> items, ItemId item) {
        Set<ItemId> r = new LinkedHashSet<>(items);
        r.remove(item);
        return r;
    }

    /** 산 순서를 지키며 하나를 더한 목록. */
    private static Set<ItemId> with(Set<ItemId> items, ItemId item) {
        Set<ItemId> r = new LinkedHashSet<>(items);
        r.add(item);
        return r;
    }

    /** D-52 방 꾸미기 만족도: 놓인 아이템 가격 합을 모든 아이템 가격 합으로 나눈 백분율을 반올림한다. */
    public int satisfaction(Set<ItemId> placed) {
        int sum = placed.stream().mapToInt(Shop::price).sum();
        return Math.round(sum * 100f / ALL_PRICES);
    }

    /** D-53. 분류("전체"면 모두)와 앞뒤 공백을 지운 검색어(비면 모두)에 맞는 아이템. ownedOnly면 가진 것만. 가격 순이다. */
    public List<Item> find(String category, String query, boolean ownedOnly) {
        String q = query == null ? "" : query.strip();
        return ITEMS.stream()
                .filter(i -> "전체".equals(category) || i.category().equals(category))
                .filter(i -> q.isEmpty() || i.id().label().contains(q))
                .filter(i -> !ownedOnly || data.getOwnedItems().contains(i.id()))
                .toList();
    }
}
