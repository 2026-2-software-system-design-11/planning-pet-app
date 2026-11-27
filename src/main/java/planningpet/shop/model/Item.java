package planningpet.shop.model;

/** 상점 아이템 하나(D-52). 분류는 "가구", "장식", "장난감", "기타" 가운데 하나다. 이름은 `id.label()`이다. */
public record Item(ItemId id, int price, String category) {
}
