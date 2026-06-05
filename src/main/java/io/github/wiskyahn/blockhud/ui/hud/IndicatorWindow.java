package io.github.wiskyahn.blockhud.ui.hud;

import io.github.wiskyahn.blockhud.domain.model.IndicatorSource;
import io.github.wiskyahn.blockhud.service.SystemMetricsService;
import io.github.wiskyahn.blockhud.ui.common.WindowDrag;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

/**
 * 시스템 인디케이터 창 — 마크 하트/갑옷/배고픔/공기/경험치 게이지를 1Hz로 갱신.
 * 미지원 소스(배터리 없음, GPU 등)는 자동 숨김. (DESIGN.md §8.2)
 *
 * <p>Phase 4: 5개 바를 세로로 스택. 핫바 주변 정밀 배치는 LayoutEngine(§8.1, Phase 7)에서.
 */
public final class IndicatorWindow {

    private static final double SCALE = 0.5; // 586×65 → 293×32.5

    /** (아이콘 디렉터리, 소스) — Phase 5 설정 연동 전 기본값. */
    private record BarSpec(String dir, IndicatorSource source) {
    }

    private static final List<BarSpec> SPECS = List.of(
            new BarSpec("heart", IndicatorSource.RAM_USED),
            new BarSpec("armor", IndicatorSource.CPU_LOAD),
            new BarSpec("food", IndicatorSource.DISK_USED),
            new BarSpec("air", IndicatorSource.BATTERY_CHARGE),
            new BarSpec("exp", IndicatorSource.CPU_IDLE));

    private final Stage stage = new Stage();
    private final SystemMetricsService metrics;
    private final List<Binding> bindings = new ArrayList<>();
    private final VBox root = new VBox(4);
    private Timeline timeline;

    private record Binding(IndicatorBar bar, IndicatorSource source) {
    }

    public IndicatorWindow(SystemMetricsService metrics) {
        this.metrics = metrics;

        for (BarSpec spec : SPECS) {
            IndicatorBar bar = new IndicatorBar(spec.dir(), SCALE);
            bindings.add(new Binding(bar, spec.source()));
            root.getChildren().add(bar);
        }
        root.setPadding(new Insets(6));

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);

        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setAlwaysOnTop(true);
        stage.setTitle("Block HUD — Indicators");
        stage.setScene(scene);
        WindowDrag.enable(stage, root);
    }

    public void show() {
        update();
        stage.show();
        startTimer();
    }

    public Stage stage() {
        return stage;
    }

    private void startTimer() {
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> update()));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    private void update() {
        metrics.poll();
        for (Binding b : bindings) {
            Optional<Double> value = metrics.read(b.source(), null);
            if (value.isPresent()) {
                b.bar().setVisible(true);
                b.bar().setManaged(true);
                b.bar().setRatio(value.get());
            } else {
                // 미지원 소스 → 숨김 (graceful degradation)
                b.bar().setVisible(false);
                b.bar().setManaged(false);
            }
        }
    }
}
