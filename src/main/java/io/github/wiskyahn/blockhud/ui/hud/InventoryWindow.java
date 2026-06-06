package io.github.wiskyahn.blockhud.ui.hud;

import io.github.wiskyahn.blockhud.domain.model.Item;
import io.github.wiskyahn.blockhud.domain.model.SlotAddress;
import io.github.wiskyahn.blockhud.ui.common.Assets;
import io.github.wiskyahn.blockhud.ui.common.SlotGridView;
import io.github.wiskyahn.blockhud.ui.common.WindowLevel;
import java.util.List;
import java.util.function.Consumer;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * 인벤토리 HUD 창 — inv.png 패널 + 9열 그리드 + 스티브 스킨, 그리고 전체화면 딤 배경(InventoryBG).
 * 원본: 패널 + InventoryBG(화면 전체 67% 검정, 클릭 시 닫기) + 플레이어 스킨. (DESIGN.md §8.1)
 */
public final class InventoryWindow {

    private static final double PANEL_W = 708;
    private static final double PANEL_H = 668;
    private static final double SLOT = 72;
    private static final double ITEM_SIZE_OFFSET = -12;
    private static final double ORIGIN_X = 29;
    private static final double STORAGE_Y = 333;
    private static final double BOTTOM_Y = 583;

    // inv.png 내 스티브 스킨 영역 (측정값)
    private static final double SKIN_X = 106;
    private static final double SKIN_Y = 34;
    private static final double SKIN_H = 272;
    // playerSpriteSheet.png 첫 프레임 (전신 스티브 ≈ 160×260)
    private static final double STEVE_FRAME_W = 162;
    private static final double STEVE_FRAME_H = 260;

    private final Stage stage = new Stage();
    private final Stage backdrop = new Stage();
    private final Consumer<Item> onActivate;
    private final Consumer<Item> onEdit;
    private final Pane root = new Pane();
    private SlotGridView slotView;
    private boolean showSteve = true;

    public InventoryWindow(Consumer<Item> onActivate, Consumer<Item> onEdit) {
        this.onActivate = onActivate;
        this.onEdit = onEdit;

        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setTitle("Block HUD — Inventory");
        root.setPrefSize(PANEL_W, PANEL_H);
        Scene scene = new Scene(root, PANEL_W, PANEL_H);
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);

        buildBackdrop();
    }

    /** 전체화면 딤 배경(원본 InventoryBG: 0,0,0,170 ≈ 67%). 클릭 시 닫기. */
    private void buildBackdrop() {
        backdrop.initStyle(StageStyle.TRANSPARENT);
        backdrop.setTitle("Block HUD — InventoryBG");
        Region dim = new Region();
        dim.setStyle("-fx-background-color: rgba(0,0,0,0.67);");
        dim.setOnMouseClicked(e -> hide());
        Scene scene = new Scene(dim);
        scene.setFill(Color.TRANSPARENT);
        backdrop.setScene(scene);
    }

    public void setShowSteve(boolean show) {
        this.showSteve = show;
        rebuild();
    }

    public void setItems(List<Item> items) {
        this.items = items;
        rebuild();
    }

    private List<Item> items = List.of();

    private void rebuild() {
        root.getChildren().clear();

        SlotGridView.Config config = new SlotGridView.Config(
                "inv.png", PANEL_W, PANEL_H, SLOT, ITEM_SIZE_OFFSET);
        slotView = new SlotGridView(config, items, InventoryWindow::placement, onActivate, null);
        slotView.setOnEdit(onEdit);
        root.getChildren().add(slotView);

        if (showSteve) {
            addSteve();
        }
    }

    /** 스프라이트 시트 첫 프레임을 스킨 영역에 렌더 (원본 InventoryPlayerSkin 기본 스티브). */
    private void addSteve() {
        Image sheet = Assets.image("playerSpriteSheet.png");
        if (sheet == null) {
            return;
        }
        ImageView steve = new ImageView(sheet);
        steve.setViewport(new Rectangle2D(0, 0, STEVE_FRAME_W, STEVE_FRAME_H));
        steve.setFitHeight(SKIN_H);
        steve.setPreserveRatio(true);
        steve.setSmooth(false);
        steve.setMouseTransparent(true);
        double drawW = STEVE_FRAME_W * (SKIN_H / STEVE_FRAME_H);
        steve.setLayoutX(SKIN_X + (195 - drawW) / 2);
        steve.setLayoutY(SKIN_Y);
        root.getChildren().add(steve);
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
            hide();
        } else {
            showBackdrop();
            stage.show();
            WindowLevel.bringToFront(stage);
        }
    }

    private void showBackdrop() {
        Rectangle2D b = Screen.getPrimary().getVisualBounds();
        backdrop.setX(b.getMinX());
        backdrop.setY(b.getMinY());
        backdrop.setWidth(b.getWidth());
        backdrop.setHeight(b.getHeight());
        backdrop.show();
        WindowLevel.bringToFront(backdrop);
    }

    private void hide() {
        stage.hide();
        backdrop.hide();
    }

    public Stage stage() {
        return stage;
    }
}
