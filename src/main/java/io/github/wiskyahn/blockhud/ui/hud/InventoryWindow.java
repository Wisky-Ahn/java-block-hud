package io.github.wiskyahn.blockhud.ui.hud;

import io.github.wiskyahn.blockhud.domain.model.Item;
import io.github.wiskyahn.blockhud.domain.model.SlotAddress;
import io.github.wiskyahn.blockhud.ui.common.SlotGridView;
import java.util.List;
import java.util.function.Consumer;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * 인벤토리 HUD 창 — 투명·항상위, 토글 가능. inv.png 패널 위에 9열 저장 그리드 3행 + 하단 행.
 * 좌클릭 실행, 우클릭 편집. ({@code _OPEN_INVENTORY_}로 토글)
 *
 * <p>그리드 픽셀 좌표는 inv.png(708×668) 측정 근사값. (Phase 3 정밀화)
 */
public final class InventoryWindow {

    private static final double PANEL_W = 708;
    private static final double PANEL_H = 668;
    private static final double SLOT = 72;
    private static final double ITEM_SIZE_OFFSET = -12;
    private static final double ORIGIN_X = 29;
    private static final double STORAGE_Y = 333;
    private static final double BOTTOM_Y = 583;

    private final Stage stage = new Stage();
    private final Consumer<Item> onActivate;
    private final Consumer<Item> onEdit;

    public InventoryWindow(Consumer<Item> onActivate, Consumer<Item> onEdit) {
        this.onActivate = onActivate;
        this.onEdit = onEdit;
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setAlwaysOnTop(true);
        stage.setTitle("Block HUD — Inventory");
    }

    public void setItems(List<Item> items) {
        SlotGridView.Config config = new SlotGridView.Config(
                "inv.png", PANEL_W, PANEL_H, SLOT, ITEM_SIZE_OFFSET);
        SlotGridView view = new SlotGridView(
                config, items, InventoryWindow::placement, onActivate, stage);
        view.setOnEdit(onEdit);

        if (stage.getScene() == null) {
            Scene scene = new Scene(view, PANEL_W, PANEL_H);
            scene.setFill(Color.TRANSPARENT);
            stage.setScene(scene);
        } else {
            stage.getScene().setRoot(view);
        }
    }

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
