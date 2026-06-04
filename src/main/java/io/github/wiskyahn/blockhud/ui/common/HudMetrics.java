package io.github.wiskyahn.blockhud.ui.common;

/** HUD 렌더링 치수. 원본 기본 설정값에서 가져옴. (DESIGN.md §5, Hotbar.inc/Inventory.inc) */
public final class HudMetrics {

    /** 핫바 슬롯 한 변 크기(px). 원본 HotbarSlotSize=60. */
    public static final double HOTBAR_SLOT = 60;
    /** 핫바 칸 수. 원본 SlotColumns=10. */
    public static final int HOTBAR_COLUMNS = 10;
    /** 아이템 이미지 크기 = 슬롯 + 이 값. 원본 HotbarItemSizeOffset=-12. */
    public static final double ITEM_SIZE_OFFSET = -12;
    /** 수량 텍스트 폰트 크기. 원본 ItemCountTextFontSize=18. */
    public static final double QTY_FONT_SIZE = 18;

    /** 인벤토리 슬롯 크기. 원본 InventorySlotSize=72. */
    public static final double INVENTORY_SLOT = 72;

    private HudMetrics() {
    }
}
