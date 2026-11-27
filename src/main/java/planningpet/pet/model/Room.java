package planningpet.pet.model;

import java.util.LinkedHashSet;
import java.util.Set;
import planningpet.app.model.AppData;
import planningpet.shop.model.ItemId;

/** 나의 방(06 20번): 가진 아이템을 방에 놓거나 치운다. 놓인 아이템은 펫 둘이 함께 쓴다(D-48). */
public final class Room {
    private final AppData data;

    public Room(AppData data) {
        this.data = data;
    }

    /** 놓인 아이템이면 치우고, 아니면 놓는다. 미리보기 칸의 "방에 놓기"와 "방에서 치우기"가 부른다. */
    public void toggle(ItemId item) {
        if (!data.getOwnedItems().contains(item)) {
            throw new IllegalStateException("사지 않은 아이템"); // 06 20번 오류(E-ROOM-1).
        }
        Set<ItemId> placed = new LinkedHashSet<>(data.getPlacedItems());
        if (!placed.remove(item)) {
            placed.add(item);
        }
        data.setPlacedItems(placed);
    }
}
