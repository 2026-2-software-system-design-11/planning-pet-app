package planningpet.shop.model;

import java.util.List;
import planningpet.app.model.AppData;

/** 상점(06 20번): 아이템과 가격, 분류, 찾기. 포인트와 아이템은 펫 둘이 함께 쓴다(D-48). */
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

    private final AppData data;

    public Shop(AppData data) {
        this.data = data;
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
