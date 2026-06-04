package io.github.wiskyahn.blockhud.app;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Phase 0 스캐폴드: 투명·항상위 HUD 창 하나를 띄워 JavaFX 오버레이 동작을 검증한다.
 *
 * <p>이후 단계에서 이 클래스는 각 HUD 창(Hotbar/Inventory/Indicators/Clock 등)을
 * 생성·배치하는 부트스트랩 역할로 확장된다. (DESIGN.md §3 ui, §8.1 LayoutEngine)
 */
public class HudApplication extends Application {

    // 드래그 이동용 마우스 오프셋 (Phase 2의 DragSnapSupport로 대체 예정)
    private double dragOffsetX;
    private double dragOffsetY;

    @Override
    public void start(Stage stage) {
        Label label = new Label("Java Block HUD\nPhase 0 ✓");
        label.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-text-alignment: center;");

        StackPane root = new StackPane(label);
        root.setStyle("-fx-background-color: rgba(0,0,0,0.45); -fx-background-radius: 10;");

        // 임시 드래그 이동 (창이 떠 있고 조작 가능함을 확인하기 위함)
        root.setOnMousePressed(e -> {
            dragOffsetX = e.getSceneX();
            dragOffsetY = e.getSceneY();
        });
        root.setOnMouseDragged(e -> {
            stage.setX(e.getScreenX() - dragOffsetX);
            stage.setY(e.getScreenY() - dragOffsetY);
        });

        Scene scene = new Scene(root, 320, 90);
        scene.setFill(Color.TRANSPARENT);

        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setAlwaysOnTop(true);
        stage.setTitle("Java Block HUD");
        stage.setScene(scene);
        stage.show();
    }
}
