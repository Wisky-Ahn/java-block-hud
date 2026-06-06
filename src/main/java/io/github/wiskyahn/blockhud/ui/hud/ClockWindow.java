package io.github.wiskyahn.blockhud.ui.hud;

import io.github.wiskyahn.blockhud.ui.common.Assets;
import io.github.wiskyahn.blockhud.ui.common.WindowDrag;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

/**
 * 마크 스타일 텍스트 시계 — 시각(대) + 날짜(소), 1초 갱신. 원본 Clock.ini 대체. (DESIGN.md §3 ui)
 *
 * <p>Phase 4: 24시간 표기 기본. 12/24·색상·크기 설정 연동은 Phase 5. 스프라이트 시계는 후속.
 */
public final class ClockWindow {

    private boolean use24Hour = true;
    private boolean hideMeridiem = false;
    private DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm:ss");
    private final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final Stage stage = new Stage();
    private final Label timeLabel = new Label();
    private final Label dateLabel = new Label();
    private Timeline timeline;

    public ClockWindow() {
        timeLabel.setFont(Font.font(Assets.fontFamily(), 40));
        timeLabel.setTextFill(Color.WHITE);
        dateLabel.setFont(Font.font(Assets.fontFamily(), 16));
        dateLabel.setTextFill(Color.web("#dddddd"));

        VBox root = new VBox(2, timeLabel, dateLabel);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(8, 14, 8, 14));
        root.setStyle("-fx-background-color: rgba(0,0,0,0.35); -fx-background-radius: 8;");

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);

        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setTitle("Block HUD — Clock");
        stage.setScene(scene);
        WindowDrag.enable(stage, root);
    }

    public void setUse24Hour(boolean use24Hour) {
        this.use24Hour = use24Hour;
        updateFormat();
    }

    public void setHideMeridiem(boolean hideMeridiem) {
        this.hideMeridiem = hideMeridiem;
        updateFormat();
    }

    private void updateFormat() {
        String pattern = use24Hour ? "HH:mm:ss" : (hideMeridiem ? "hh:mm:ss" : "hh:mm:ss a");
        timeFormat = DateTimeFormatter.ofPattern(pattern);
        update();
    }

    public void show() {
        update();
        stage.show();
        io.github.wiskyahn.blockhud.ui.common.WindowLevel.sendToBack(stage);
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> update()));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    public Stage stage() {
        return stage;
    }

    private void update() {
        LocalDateTime now = LocalDateTime.now();
        timeLabel.setText(now.format(timeFormat));
        dateLabel.setText(now.format(dateFormat));
    }
}
