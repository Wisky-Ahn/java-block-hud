package io.github.wiskyahn.blockhud.ui.hud;

import io.github.wiskyahn.blockhud.domain.model.IndicatorSource;
import io.github.wiskyahn.blockhud.service.SystemMetricsService;
import io.github.wiskyahn.blockhud.ui.common.WindowDrag;
import java.util.LinkedHashMap;
import java.util.Map;
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
 * 소스는 설정에서 주입(기본값은 원본과 동일). 미지원 소스는 자동 숨김. (DESIGN.md §8.2)
 */
public final class IndicatorWindow {

    private static final double SCALE = 0.5; // 586×65 → 293×32.5

    /** 표시 순서대로 (아이콘 디렉터리, 원본 기본 소스). */
    private static final Map<String, IndicatorSource> DEFAULT_SOURCES = new LinkedHashMap<>();

    static {
        DEFAULT_SOURCES.put("heart", IndicatorSource.CPU_IDLE);
        DEFAULT_SOURCES.put("armor", IndicatorSource.CPU_LOAD);
        DEFAULT_SOURCES.put("food", IndicatorSource.RAM_FREE);
        DEFAULT_SOURCES.put("air", IndicatorSource.RAM_USED);
        DEFAULT_SOURCES.put("exp", IndicatorSource.DISK_USED);
    }

    private final Stage stage = new Stage();
    private final SystemMetricsService metrics;
    private final Map<String, IndicatorBar> bars = new LinkedHashMap<>();
    private final Map<String, IndicatorSource> sources = new LinkedHashMap<>(DEFAULT_SOURCES);
    private final VBox root = new VBox(4);
    private Timeline timeline;

    public IndicatorWindow(SystemMetricsService metrics) {
        this.metrics = metrics;

        for (String dir : DEFAULT_SOURCES.keySet()) {
            IndicatorBar bar = new IndicatorBar(dir, SCALE);
            bars.put(dir, bar);
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

    /** 설정에서 각 바의 소스를 주입. */
    public void setSources(IndicatorSource heart, IndicatorSource armor, IndicatorSource food,
                           IndicatorSource air, IndicatorSource exp) {
        sources.put("heart", heart);
        sources.put("armor", armor);
        sources.put("food", food);
        sources.put("air", air);
        sources.put("exp", exp);
        update();
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
        bars.forEach((dir, bar) -> {
            Optional<Double> value = metrics.read(sources.get(dir), null);
            boolean supported = value.isPresent();
            bar.setVisible(supported);
            bar.setManaged(supported);
            if (supported) {
                bar.setRatio(value.get());
            }
        });
    }
}
