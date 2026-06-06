package io.github.wiskyahn.blockhud.ui.hud;

import io.github.wiskyahn.blockhud.domain.model.Item;
import io.github.wiskyahn.blockhud.domain.model.SlotAddress;
import io.github.wiskyahn.blockhud.ui.common.Assets;
import io.github.wiskyahn.blockhud.ui.common.SlotGridView;
import io.github.wiskyahn.blockhud.ui.common.WindowLevel;
import java.util.List;
import java.util.function.Consumer;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * 인벤토리 HUD 창 — inv.png 패널 + 9열 그리드 + 스티브 스킨 + 버튼(닫기/설정/편집) + 딤 배경.
 * 원본 InventoryOptionMeters(버튼)·InventoryBG(딤)·InventoryPlayerSkin(스티브) 재현. (DESIGN.md §8.1)
 */
public final class InventoryWindow {

    private static final double PANEL_W = 708;
    private static final double PANEL_H = 668;
    private static final double SLOT = 72;
    private static final double ITEM_SIZE_OFFSET = -12;
    private static final double ORIGIN_X = 29;
    private static final double STORAGE_Y = 333;
    private static final double BOTTOM_Y = 583;

    private static final double SKIN_X = 106;
    private static final double SKIN_Y = 34;
    private static final double SKIN_H = 272;
    private static final double STEVE_FRAME_W = 162;
    private static final double STEVE_FRAME_H = 260;

    private final Stage stage = new Stage();
    private final Stage backdrop = new Stage();
    private final Consumer<Item> onActivate;
    private final Consumer<Item> onEdit;
    private final Runnable onOpenSettings;
    private final Pane root = new Pane();
    private final Label editBadge = new Label("EDIT");
    private List<Item> items = List.of();
    private boolean showSteve = true;
    private boolean editMode = false;

    public InventoryWindow(Consumer<Item> onActivate, Consumer<Item> onEdit, Runnable onOpenSettings) {
        this.onActivate = onActivate;
        this.onEdit = onEdit;
        this.onOpenSettings = onOpenSettings;

        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setTitle("Block HUD — Inventory");
        root.setPrefSize(PANEL_W, PANEL_H);
        Scene scene = new Scene(root, PANEL_W, PANEL_H);
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);

        buildBackdrop();
    }

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

    private void rebuild() {
        root.getChildren().clear();

        SlotGridView.Config config = new SlotGridView.Config(
                "inv.png", PANEL_W, PANEL_H, SLOT, ITEM_SIZE_OFFSET);
        // 편집 모드면 좌클릭=편집, 아니면 실행
        Consumer<Item> click = item -> {
            if (editMode) {
                onEdit.accept(item);
            } else {
                onActivate.accept(item);
            }
        };
        SlotGridView slotView = new SlotGridView(config, items, InventoryWindow::placement, click, null);
        slotView.setOnEdit(onEdit);
        root.getChildren().add(slotView);

        if (showSteve) {
            addSteve();
        }
        addButtons();
        addEditBadge();
    }

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

    /** 원본 InventoryOptionMeters 버튼: 닫기/설정/편집. */
    private void addButtons() {
        root.getChildren().add(button("InvClose.png", 648, 16, 44, 44, this::hide));
        root.getChildren().add(button("setting.png", 535, 256, 70, 70, onOpenSettings));
        root.getChildren().add(button("edit.png", 614, 261, 60, 60, this::toggleEditMode));
    }

    private ImageView button(String image, double x, double y, double w, double h, Runnable action) {
        ImageView view = new ImageView(Assets.image(image));
        view.setFitWidth(w);
        view.setFitHeight(h);
        view.setSmooth(false);
        view.setLayoutX(x);
        view.setLayoutY(y);
        view.setCursor(Cursor.HAND);
        view.setOnMouseClicked(e -> action.run());
        return view;
    }

    private void addEditBadge() {
        editBadge.setFont(Font.font(Assets.fontFamily(), 14));
        editBadge.setTextFill(Color.WHITE);
        editBadge.setStyle("-fx-background-color: rgba(47,125,50,0.95); -fx-padding: 4 12 4 12;"
                + " -fx-background-radius: 5;");
        editBadge.setPrefWidth(120);
        editBadge.setAlignment(Pos.CENTER);
        editBadge.setLayoutX((PANEL_W - 120) / 2);
        editBadge.setLayoutY(15);
        editBadge.setMouseTransparent(true);
        editBadge.setVisible(editMode);
        root.getChildren().add(editBadge);
    }

    private void toggleEditMode() {
        editMode = !editMode;
        editBadge.setVisible(editMode);
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
