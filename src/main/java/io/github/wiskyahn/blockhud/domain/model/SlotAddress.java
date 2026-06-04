package io.github.wiskyahn.blockhud.domain.model;

/**
 * 슬롯 위치. 원본의 두 가지 주소 체계를 표현한다. (DESIGN.md §8.4)
 *
 * <ul>
 *   <li>{@link HotbarSlot} — 핫바(맨 아랫줄) 순차 인덱스: {@code Slot01}~{@code Slot10}</li>
 *   <li>{@link GridSlot} — 인벤토리 그리드 좌표: {@code SlotX{col}Y{row}}</li>
 * </ul>
 */
public sealed interface SlotAddress permits SlotAddress.HotbarSlot, SlotAddress.GridSlot {

    record HotbarSlot(int index) implements SlotAddress {
    }

    record GridSlot(int col, int row) implements SlotAddress {
    }
}
