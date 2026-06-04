package io.github.wiskyahn.blockhud.ui.hud;

import io.github.wiskyahn.blockhud.domain.model.Item;
import io.github.wiskyahn.blockhud.domain.model.SlotAddress;
import io.github.wiskyahn.blockhud.service.ActionLauncher;
import io.github.wiskyahn.blockhud.ui.common.HudMetrics;
import io.github.wiskyahn.blockhud.ui.common.SlotGridView;
import java.util.List;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * 핫바 HUD 창 — 투명·항상위. 마크 핫바(10칸 1행)를 그리고 클릭 시 액션을 실행한다.
 * (DESIGN.md §3 ui, §8.1)
 */
public final class HotbarWindow {

    private final Stage stage = new Stage();

    public HotbarWindow(List<Item> hotbarItems, ActionLauncher launcher) {
        double width = HudMetrics.HOTBAR_COLUMNS * HudMetrics.HOTBAR_SLOT;
        double height = HudMetrics.HOTBAR_SLOT;

        SlotGridView.Config config = new SlotGridView.Config(
                "hotbar.png", width, height, HudMetrics.HOTBAR_SLOT, HudMetrics.ITEM_SIZE_OFFSET);
        SlotGridView view = new SlotGridView(
                config, hotbarItems, HotbarWindow::placement,
                item -> launcher.launch(item.action()), stage);

        Scene scene = new Scene(view, width, height);
        scene.setFill(Color.TRANSPARENT);

        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setAlwaysOnTop(true);
        stage.setTitle("Block HUD — Hotbar");
        stage.setScene(scene);
    }

    /** HotbarSlot(1..10) → 좌상단 픽셀 좌표. */
    private static double[] placement(SlotAddress address) {
        if (address instanceof SlotAddress.HotbarSlot h
                && h.index() >= 1 && h.index() <= HudMetrics.HOTBAR_COLUMNS) {
            return new double[]{(h.index() - 1) * HudMetrics.HOTBAR_SLOT, 0};
        }
        return null;
    }

    public void show() {
        stage.show();
    }

    public Stage stage() {
        return stage;
    }
}
