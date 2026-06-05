package io.github.wiskyahn.blockhud.ui.hud;

import io.github.wiskyahn.blockhud.ui.common.Assets;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;

/**
 * 마크 인디케이터 게이지 한 줄. {@code empty.png}(빈 바) 위에 {@code fill.png}를 비율만큼
 * 클리핑해 표시한다. 원본 gauge meter(fill/empty 스왑) 대체. (DESIGN.md §8.2)
 *
 * <p>원본 아이콘 스트립은 586×65 (10칸). {@code scale}로 표시 크기를 조절한다.
 */
public final class IndicatorBar extends Pane {

    private static final double NATIVE_W = 586;
    private static final double NATIVE_H = 65;

    private final double width;
    private final double height;
    private final ImageView fillView;
    private final Rectangle clip;

    public IndicatorBar(String dir, double scale) {
        this.width = NATIVE_W * scale;
        this.height = NATIVE_H * scale;
        setPrefSize(width, height);

        Image empty = Assets.imageAt("indicators/" + dir + "/empty.png");
        Image fill = Assets.imageAt("indicators/" + dir + "/fill.png");

        ImageView emptyView = imageView(empty);
        fillView = imageView(fill);

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

    /** 0..1 비율로 채움 폭 설정. */
    public void setRatio(double ratio) {
        double r = Math.max(0, Math.min(1, ratio));
        clip.setWidth(width * r);
    }
}
