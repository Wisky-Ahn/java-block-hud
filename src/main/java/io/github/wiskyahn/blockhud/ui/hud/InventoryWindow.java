package io.github.wiskyahn.blockhud.ui.hud;

import io.github.wiskyahn.blockhud.domain.model.Item;
import io.github.wiskyahn.blockhud.domain.model.SlotAddress;
import io.github.wiskyahn.blockhud.service.ActionLauncher;
import io.github.wiskyahn.blockhud.ui.common.SlotGridView;
import java.util.List;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * 인벤토리 HUD 창 — 투명·항상위, 토글 가능. 마크 인벤토리 패널(inv.png) 위에
 * 9열 저장 그리드 3행 + 하단 행을 그린다. ({@code _OPEN_INVENTORY_}로 토글)
 *
 * <p>그리드 픽셀 좌표는 inv.png(708×668)에서 측정한 근사값이며 Phase 3(에디터)에서 정밀화한다.
 */
public final class InventoryWindow {

    // inv.png 측정 기반 그리드 지오메트리 (근사)
    private static final double PANEL_W = 708;
    private static final double PANEL_H = 668;
    private static final double SLOT = 72;
    private static final double ITEM_SIZE_OFFSET = -12;
    private static final double ORIGIN_X = 29;
    private static final double STORAGE_Y = 333;  // 저장 그리드(Y1~Y3) 상단
    private static final double BOTTOM_Y = 583;    // 하단 행(Y4)

    private final Stage stage = new Stage();

    public InventoryWindow(List<Item> inventoryItems, ActionLauncher launcher) {
        SlotGridView.Config config = new SlotGridView.Config(
                "inv.png", PANEL_W, PANEL_H, SLOT, ITEM_SIZE_OFFSET);
        SlotGridView view = new SlotGridView(
                config, inventoryItems, InventoryWindow::placement,
                item -> launcher.launch(item.action()), stage);

        Scene scene = new Scene(view, PANEL_W, PANEL_H);
        scene.setFill(Color.TRANSPARENT);

        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setAlwaysOnTop(true);
        stage.setTitle("Block HUD — Inventory");
        stage.setScene(scene);
    }

    /** GridSlot(col 1..9, row 1..4) → 좌상단 픽셀 좌표. */
    private static double[] placement(SlotAddress address) {
        if (!(address instanceof SlotAddress.GridSlot g)) {
            return null;
        }
        if (g.col() < 1 || g.col() > 9 || g.row() < 1 || g.row() > 4) {
            return null;
        }
        double x = ORIGIN_X + (g.col() - 1) * SLOT;
        double y = (g.row() <= 3) ? STORAGE_Y + (g.row() - 1) * SLOT : BOTTOM_Y;
        return new double[]{x, y};
    }

    public void toggle() {
        if (stage.isShowing()) {
            stage.hide();
        } else {
            stage.show();
            stage.toFront();
        }
    }

    public Stage stage() {
        return stage;
    }
}
