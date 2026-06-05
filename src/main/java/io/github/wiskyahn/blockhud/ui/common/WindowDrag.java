package io.github.wiskyahn.blockhud.ui.common;

import javafx.scene.Node;
import javafx.stage.Stage;

/** 노드 드래그로 창 이동(+엣지 스냅). HUD 창 공용. (DESIGN.md §5) */
public final class WindowDrag {

    private WindowDrag() {
    }

    public static void enable(Stage stage, Node handle) {
        final double[] offset = new double[2];
        handle.setOnMousePressed(e -> {
            offset[0] = e.getSceneX();
            offset[1] = e.getSceneY();
        });
        handle.setOnMouseDragged(e -> {
            stage.setX(e.getScreenX() - offset[0]);
            stage.setY(e.getScreenY() - offset[1]);
        });
        handle.setOnMouseReleased(e -> SnapUtil.snapToScreenEdges(stage));
    }
}
