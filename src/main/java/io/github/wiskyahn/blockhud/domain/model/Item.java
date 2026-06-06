package io.github.wiskyahn.blockhud.domain.model;

import io.github.wiskyahn.blockhud.domain.action.ItemAction;

/**
 * 핫바/인벤토리 슬롯 하나의 데이터. 원본 슬롯 변수
 * ({@code Label / Action / Image / Qty} + 이미지 오프셋)에 대응한다. (DESIGN.md §4)
 */
public record Item(
        SlotAddress address,
        String label,
        ItemAction action,
        String image,
        int qty,
        ImageOffset offset,
        boolean confirmBeforeRun
) {

    public Item {
        if (address == null) {
            throw new IllegalArgumentException("address must not be null");
        }
        if (label == null) {
            label = "";
        }
        if (action == null) {
            action = ItemAction.NONE;
        }
        if (image == null) {
            image = "";
        }
        if (offset == null) {
            offset = ImageOffset.ZERO;
        }
    }

    /** 라벨/액션/이미지가 모두 비어 있고 수량이 0이면 빈 슬롯으로 간주. */
    public boolean isEmpty() {
        return label.isBlank()
                && image.isBlank()
                && action instanceof ItemAction.None
                && qty == 0;
    }

    public Item withOffset(ImageOffset newOffset) {
        return new Item(address, label, action, image, qty, newOffset, confirmBeforeRun);
    }

    public Item withAddress(SlotAddress newAddress) {
        return new Item(newAddress, label, action, image, qty, offset, confirmBeforeRun);
    }
}
