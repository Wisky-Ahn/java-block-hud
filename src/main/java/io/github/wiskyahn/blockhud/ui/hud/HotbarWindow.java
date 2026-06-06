package io.github.wiskyahn.blockhud.ui.hud;

import io.github.wiskyahn.blockhud.domain.model.Item;
import io.github.wiskyahn.blockhud.domain.model.SlotAddress;
import io.github.wiskyahn.blockhud.ui.common.HudMetrics;
import io.github.wiskyahn.blockhud.ui.common.SlotGridView;
import java.util.List;
import java.util.function.Consumer;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * 핫바 HUD 창 — 투명·항상위. 마크 핫바(10칸 1행)를 그린다.
 * 좌클릭 실행, 우클릭 편집, 드래그 이동. (DESIGN.md §3 ui, §8.1)
 */
public final class HotbarWindow {

    private static final double WIDTH = HudMetrics.HOTBAR_COLUMNS * HudMetrics.HOTBAR_SLOT;
    private static final double HEIGHT = HudMetrics.HOTBAR_SLOT;

    private final Stage stage = new Stage();
    private final Consumer<Item> onActivate;
    private final Consumer<Item> onEdit;

    public HotbarWindow(Consumer<Item> onActivate, Consumer<Item> onEdit) {
        this.onActivate = onActivate;
        this.onEdit = onEdit;
        stage.initStyle(StageStyle.TRANSPARENT);
        // 바탕화면 위젯 — always-on-top 아님 (원본 ZPos -2, 다른 창 뒤). show()에서 toBack.
        stage.setTitle("Block HUD — Hotbar");
    }

    /** 아이템 목록으로 뷰를 (재)구성. 창 위치는 유지. */
    public void setItems(List<Item> items) {
        SlotGridView.Config config = new SlotGridView.Config(
                "hotbar.png", WIDTH, HEIGHT, HudMetrics.HOTBAR_SLOT, HudMetrics.ITEM_SIZE_OFFSET);
        SlotGridView view = new SlotGridView(
                config, items, HotbarWindow::placement, onActivate, stage);
        view.setOnEdit(onEdit);

        if (stage.getScene() == null) {
            Scene scene = new Scene(view, WIDTH, HEIGHT);
            scene.setFill(Color.TRANSPARENT);
            stage.setScene(scene);
        } else {
            stage.getScene().setRoot(view);
        }
    }

    private static double[] placement(SlotAddress address) {
        if (address instanceof SlotAddress.HotbarSlot h
                && h.index() >= 1 && h.index() <= HudMetrics.HOTBAR_COLUMNS) {
            return new double[]{(h.index() - 1) * HudMetrics.HOTBAR_SLOT, 0};
        }
        return null;
    }

    public void show() {
        stage.show();
        io.github.wiskyahn.blockhud.ui.common.WindowLevel.sendToBack(stage);
    }

    public Stage stage() {
        return stage;
    }
}
