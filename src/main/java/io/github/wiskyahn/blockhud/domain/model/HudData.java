package io.github.wiskyahn.blockhud.domain.model;

import java.util.List;

/**
 * HUD 전체 아이템 데이터(핫바 + 인벤토리)의 불변 스냅샷. (DESIGN.md §4 items.json)
 */
public record HudData(List<Item> hotbar, List<Item> inventory) {

    public HudData {
        hotbar = List.copyOf(hotbar);
        inventory = List.copyOf(inventory);
    }

    public static HudData empty() {
        return new HudData(List.of(), List.of());
    }
}
