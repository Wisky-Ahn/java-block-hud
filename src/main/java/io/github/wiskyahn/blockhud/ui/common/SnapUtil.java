package io.github.wiskyahn.blockhud.ui.common;

import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;

/** 창을 화면 가장자리에 스냅. 원본 {@code !SnapEdges} 대응. (DESIGN.md §5 드래그·스냅) */
public final class SnapUtil {

    private static final double THRESHOLD = 24;

    private SnapUtil() {
    }

    public static void snapToScreenEdges(Stage stage) {
        Rectangle2D b = Screen.getScreensForRectangle(
                        stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight())
                .stream().findFirst().orElse(Screen.getPrimary())
                .getVisualBounds();

        double x = stage.getX();
        double y = stage.getY();
        double w = stage.getWidth();
        double h = stage.getHeight();

        if (Math.abs(x - b.getMinX()) < THRESHOLD) {
            x = b.getMinX();
        }
        if (Math.abs((x + w) - b.getMaxX()) < THRESHOLD) {
            x = b.getMaxX() - w;
        }
        if (Math.abs(y - b.getMinY()) < THRESHOLD) {
            y = b.getMinY();
        }
        if (Math.abs((y + h) - b.getMaxY()) < THRESHOLD) {
            y = b.getMaxY() - h;
        }
        stage.setX(x);
        stage.setY(y);
    }
}
