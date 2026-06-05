package io.github.wiskyahn.blockhud.ui.common;

import io.github.wiskyahn.blockhud.domain.model.Item;
import io.github.wiskyahn.blockhud.domain.model.SlotAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * 마크 스타일 슬롯 그리드 렌더러. 배경 이미지 위에 아이템/수량을 그리고,
 * 호버 하이라이트·클릭 실행·창 드래그(+엣지 스냅)를 처리한다.
 * 원본 핫바/인벤토리 meter + HighlightSlot.lua 대체. (DESIGN.md §5, §8.1)
 *
 * <p>슬롯 위치는 {@code placement}가 슬롯의 좌상단 <b>픽셀 좌표</b>를 반환하는 방식이라
 * 핫바(균일 1행)와 인벤토리(갭 있는 비균일 그리드)를 모두 표현할 수 있다.
 */
public final class SlotGridView extends Pane {

    /** 슬롯 그리드 구성. width/height는 배경(=창) 크기. */
    public record Config(String backgroundImage, double width, double height,
                         double slotSize, double itemSizeOffset) {
    }

    private record Cell(double x, double y, Item item) {
    }

    private final Config config;
    private final Consumer<Item> onActivate;
    private final List<Cell> cells = new ArrayList<>();
    private final Region highlight = new Region();
    private final TooltipPopup tooltip = new TooltipPopup();

    /** 보조 클릭(우클릭) 시 편집 콜백. */
    private Consumer<Item> onEdit;

    private static final double DRAG_THRESHOLD = 5;
    private double pressX;
    private double pressY;
    private boolean dragging;

    public SlotGridView(Config config, List<Item> items,
                        Function<SlotAddress, double[]> placement,
                        Consumer<Item> onActivate, Stage stageToDrag) {
        this.config = config;
        this.onActivate = onActivate;

        setPrefSize(config.width(), config.height());
        addBackground();
        configureHighlight();
        for (Item item : items) {
            double[] pos = placement.apply(item.address());
            if (pos != null) {
                addItem(item, pos[0], pos[1]);
            }
        }
        getChildren().add(highlight);
        installMouseHandlers(stageToDrag);
    }

    private void addBackground() {
        Image bg = Assets.image(config.backgroundImage());
        if (bg != null) {
            ImageView view = new ImageView(bg);
            view.setFitWidth(config.width());
            view.setFitHeight(config.height());
            view.setSmooth(false); // 픽셀 아트 — 보간 없이
            getChildren().add(view);
        }
    }

    private void configureHighlight() {
        double s = config.slotSize();
        highlight.setPrefSize(s, s);
        highlight.resize(s, s);
        highlight.setStyle("-fx-background-color: rgba(0,0,0,0.43);"
                + " -fx-border-color: rgba(255,255,255,0.59); -fx-border-width: 2;");
        highlight.setVisible(false);
        highlight.setMouseTransparent(true);
    }

    private void addItem(Item item, double cellX, double cellY) {
        cells.add(new Cell(cellX, cellY, item));

        double slot = config.slotSize();
        double itemSize = slot + config.itemSizeOffset();
        double pad = (slot - itemSize) / 2.0;

        if (!item.image().isBlank()) {
            Image img = Assets.image(item.image());
            if (img != null) {
                ImageView iv = new ImageView(img);
                iv.setFitWidth(itemSize + item.offset().size());
                iv.setFitHeight(itemSize + item.offset().size());
                iv.setSmooth(false);
                iv.setMouseTransparent(true);
                iv.setLayoutX(cellX + pad + item.offset().x());
                iv.setLayoutY(cellY + pad + item.offset().y());
                getChildren().add(iv);
            }
        }

        if (item.qty() > 0) {
            Text qty = new Text(Integer.toString(item.qty()));
            qty.setFont(Font.font(Assets.fontFamily(), HudMetrics.QTY_FONT_SIZE));
            qty.setFill(Color.WHITE);
            qty.setStroke(Color.rgb(0, 0, 0, 0.6));
            qty.setStrokeWidth(0.6);
            qty.setMouseTransparent(true);
            qty.setLayoutX(cellX + slot - 18);
            qty.setLayoutY(cellY + slot - 6);
            getChildren().add(qty);
        }
    }

    /** 우클릭 편집 콜백 등록. */
    public void setOnEdit(Consumer<Item> handler) {
        this.onEdit = handler;
    }

    private void installMouseHandlers(Stage stageToDrag) {
        setOnMouseMoved(e -> {
            updateHighlight(e.getX(), e.getY());
            updateTooltip(e.getX(), e.getY(), e.getScreenX(), e.getScreenY());
        });
        setOnMouseExited(e -> {
            highlight.setVisible(false);
            tooltip.hide();
        });

        setOnMousePressed(e -> {
            pressX = e.getScreenX();
            pressY = e.getScreenY();
            dragging = false;
            if (e.isSecondaryButtonDown() && onEdit != null) {
                Cell hit = cellAt(e.getX(), e.getY());
                if (hit != null) {
                    tooltip.hide();
                    onEdit.accept(hit.item());
                }
            }
        });

        setOnMouseDragged(e -> {
            if (stageToDrag == null || !e.isPrimaryButtonDown()) {
                return;
            }
            tooltip.hide();
            if (Math.abs(e.getScreenX() - pressX) > DRAG_THRESHOLD
                    || Math.abs(e.getScreenY() - pressY) > DRAG_THRESHOLD) {
                dragging = true;
            }
            if (dragging) {
                stageToDrag.setX(e.getScreenX() - e.getX());
                stageToDrag.setY(e.getScreenY() - e.getY());
                highlight.setVisible(false);
            }
        });

        setOnMouseReleased(e -> {
            if (dragging) {
                if (stageToDrag != null) {
                    SnapUtil.snapToScreenEdges(stageToDrag);
                }
                dragging = false;
                return;
            }
            if (e.getButton() != MouseButton.PRIMARY) {
                return; // 우클릭은 누를 때 편집으로 처리됨
            }
            Cell hit = cellAt(e.getX(), e.getY());
            if (hit != null && onActivate != null) {
                onActivate.accept(hit.item());
            }
        });
    }

    private void updateTooltip(double localX, double localY, double screenX, double screenY) {
        Cell hit = cellAt(localX, localY);
        if (hit == null) {
            tooltip.hide();
        } else {
            tooltip.show(this, hit.item().label(), screenX, screenY);
        }
    }

    private void updateHighlight(double x, double y) {
        Cell hit = cellAt(x, y);
        if (hit == null) {
            highlight.setVisible(false);
            return;
        }
        highlight.setLayoutX(hit.x());
        highlight.setLayoutY(hit.y());
        highlight.setVisible(true);
    }

    private Cell cellAt(double x, double y) {
        double s = config.slotSize();
        for (Cell c : cells) {
            if (x >= c.x() && x < c.x() + s && y >= c.y() && y < c.y() + s) {
                return c;
            }
        }
        return null;
    }
}
