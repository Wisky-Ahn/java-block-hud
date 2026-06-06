package io.github.wiskyahn.blockhud.ui.hud;

import io.github.wiskyahn.blockhud.ui.common.Assets;
import io.github.wiskyahn.blockhud.ui.common.WindowDrag;
import io.github.wiskyahn.blockhud.ui.common.WindowLevel;
import java.time.LocalTime;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

/**
 * 마인크래프트 해/달 시계(스프라이트). clockSpriteSheet.png(1280×3328)의 64프레임(256×256, 5열)을
 * 하루 시간대(정오 기준)에 매핑해 렌더. 원본 ClockSprite.lua 재현. (DESIGN.md §8.1)
 */
public final class ClockSpriteWindow {

    private static final int FRAME_COUNT = 64;
    private static final double FRAME = 256;
    private static final int FRAMES_PER_ROW = 5;
    private static final int SECONDS_PER_DAY = 86400;
    private static final int NOON = 43200;

    private double renderSize = 128;     // ClockSpriteSize 기본값

    private final Stage stage = new Stage();
    private final ImageView view = new ImageView();
    private Timeline timeline;

    public ClockSpriteWindow() {
        Image sheet = Assets.image("clockSpriteSheet.png");
        view.setImage(sheet);
        view.setSmooth(false);
        applySize();

        StackPane root = new StackPane(view);
        root.setStyle("-fx-background-color: transparent;");
        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);

        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setTitle("Block HUD — ClockSprite");
        stage.setScene(scene);
        WindowDrag.enable(stage, root);
    }

    public void setRenderSize(double size) {
        this.renderSize = Math.max(16, size);
        applySize();
    }

    private void applySize() {
        view.setFitWidth(renderSize);
        view.setFitHeight(renderSize);
    }

    public void show() {
        update();
        stage.show();
        WindowLevel.sendToBack(stage);
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> update()));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    public Stage stage() {
        return stage;
    }

    private void update() {
        LocalTime now = LocalTime.now();
        int secondsOfDay = now.toSecondOfDay();
        int phase = ((secondsOfDay - NOON) % SECONDS_PER_DAY + SECONDS_PER_DAY) % SECONDS_PER_DAY;
        int frame = (int) Math.floor((double) phase * FRAME_COUNT / SECONDS_PER_DAY);
        frame = Math.max(0, Math.min(FRAME_COUNT - 1, frame));

        double cropX = (frame % FRAMES_PER_ROW) * FRAME;
        double cropY = (frame / FRAMES_PER_ROW) * FRAME;
        view.setViewport(new Rectangle2D(cropX, cropY, FRAME, FRAME));
    }
}
