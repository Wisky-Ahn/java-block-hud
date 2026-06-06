package io.github.wiskyahn.blockhud.ui.hud;

import io.github.wiskyahn.blockhud.ui.common.Assets;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;

/**
 * 마크 인디케이터 게이지 한 줄. {@code empty.png} 위에 {@code fill.png}를 비율만큼 클리핑.
 * 표시 폭(displayWidth)을 받고 높이는 <b>실제 이미지 종횡비</b>로 계산한다.
 * (하트/갑옷/배고픔/공기 586×65, 경험치 1500×41로 서로 다름 — DESIGN.md §8.2)
 */
public final class IndicatorBar extends Pane {

    private final double width;
    private final double height;
    private final Rectangle clip;

    public IndicatorBar(String dir, double displayWidth) {
        Image empty = Assets.imageAt("indicators/" + dir + "/empty.png");
        Image fill = Assets.imageAt("indicators/" + dir + "/fill.png");

        double nativeW = fill != null ? fill.getWidth() : 586;
        double nativeH = fill != null ? fill.getHeight() : 65;
        this.width = displayWidth;
        this.height = displayWidth * (nativeH / nativeW);
        setPrefSize(width, height);

        ImageView emptyView = imageView(empty);
        ImageView fillView = imageView(fill);

        clip = new Rectangle(0, 0, width, height);
        fillView.setClip(clip);

        getChildren().addAll(emptyView, fillView);
    }

    private ImageView imageView(Image image) {
        ImageView v = new ImageView(image);
        v.setFitWidth(width);
        v.setFitHeight(height);
        v.setSmooth(false);
        v.setMouseTransparent(true);
        return v;
    }

    public double barHeight() {
        return height;
    }

    public void setRatio(double ratio) {
        clip.setWidth(width * Math.max(0, Math.min(1, ratio)));
    }
}
