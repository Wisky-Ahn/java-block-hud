package io.github.wiskyahn.blockhud.ui.hud;

import io.github.wiskyahn.blockhud.domain.model.IndicatorSource;
import io.github.wiskyahn.blockhud.service.SystemMetricsService;
import io.github.wiskyahn.blockhud.ui.common.Assets;
import io.github.wiskyahn.blockhud.ui.common.WindowLevel;
import java.util.Optional;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

/**
 * 시스템 인디케이터 창 — 원본 배치 재현: 갑옷/공기(위), 하트/배고픔(중간, 좌/우),
 * 경험치(아래 전체폭 얇은 바) + 레벨 숫자. 핫바 바로 위에 놓인다. (DESIGN.md §8.1, §8.2)
 */
public final class IndicatorWindow {

    public static final double WIN_W = 600;      // 핫바 폭과 동일
    public static final double WIN_H = 86;
    private static final double SIDE_W = 270;   // 하트/갑옷/배고픔/공기 표시 폭
    private static final double RIGHT_X = WIN_W - SIDE_W;
    private static final double ROW1_Y = 0;     // 갑옷/공기
    private static final double ROW2_Y = 34;    // 하트/배고픔
    private static final double EXP_Y = 68;     // 경험치 바

    private final Stage stage = new Stage();
    private final SystemMetricsService metrics;
    private final Pane root = new Pane();

    private final IndicatorBar armor = new IndicatorBar("armor", SIDE_W);
    private final IndicatorBar air = new IndicatorBar("air", SIDE_W);
    private final IndicatorBar heart = new IndicatorBar("heart", SIDE_W);
    private final IndicatorBar food = new IndicatorBar("food", SIDE_W);
    private final IndicatorBar exp = new IndicatorBar("exp", WIN_W);
    private final Label level = new Label();

    // 기본 소스 (원본 기본값)
    private IndicatorSource heartSrc = IndicatorSource.CPU_IDLE;
    private IndicatorSource armorSrc = IndicatorSource.CPU_LOAD;
    private IndicatorSource foodSrc = IndicatorSource.RAM_FREE;
    private IndicatorSource airSrc = IndicatorSource.RAM_USED;
    private IndicatorSource expSrc = IndicatorSource.DISK_USED;

    private Timeline timeline;

    public IndicatorWindow(SystemMetricsService metrics) {
        this.metrics = metrics;

        place(armor, 0, ROW1_Y);
        place(air, RIGHT_X, ROW1_Y);
        place(heart, 0, ROW2_Y);
        place(food, RIGHT_X, ROW2_Y);
        place(exp, 0, EXP_Y);

        level.setFont(Font.font(Assets.fontFamily(), 16));
        level.setTextFill(Color.web("#80FF20"));
        level.setPrefWidth(WIN_W);
        level.setLayoutX(0);
        level.setLayoutY(ROW2_Y + 2);
        level.setAlignment(javafx.geometry.Pos.CENTER);
        level.setMouseTransparent(true);
        root.getChildren().add(level);

        root.setPrefSize(WIN_W, WIN_H);
        root.setStyle("-fx-background-color: transparent;");

        Scene scene = new Scene(root, WIN_W, WIN_H);
        scene.setFill(Color.TRANSPARENT);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setTitle("Block HUD — Indicators");
        stage.setScene(scene);
        WindowLevel.bringToFront(stage); // 위치만, 레벨은 show에서 sendToBack
    }

    private void place(IndicatorBar bar, double x, double y) {
        bar.setLayoutX(x);
        bar.setLayoutY(y);
        root.getChildren().add(bar);
    }

    public void setSources(IndicatorSource heart, IndicatorSource armor, IndicatorSource food,
                           IndicatorSource air, IndicatorSource exp) {
        this.heartSrc = heart;
        this.armorSrc = armor;
        this.foodSrc = food;
        this.airSrc = air;
        this.expSrc = exp;
        update();
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
        metrics.poll();
        bind(heart, heartSrc);
        bind(armor, armorSrc);
        bind(food, foodSrc);
        bind(air, airSrc);

        Optional<Double> expValue = metrics.read(expSrc, null);
        boolean expOk = expValue.isPresent();
        exp.setVisible(expOk);
        level.setVisible(expOk);
        if (expOk) {
            exp.setRatio(expValue.get());
            level.setText(Integer.toString((int) Math.round(expValue.get() * 100)));
        }
    }

    private void bind(IndicatorBar bar, IndicatorSource source) {
        Optional<Double> value = metrics.read(source, null);
        boolean ok = value.isPresent();
        bar.setVisible(ok);
        if (ok) {
            bar.setRatio(value.get());
        }
    }
}
