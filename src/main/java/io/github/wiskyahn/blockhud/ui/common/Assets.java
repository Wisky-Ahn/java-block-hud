package io.github.wiskyahn.blockhud.ui.common;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 리소스 이미지/폰트 로딩 + 캐시. 원본 에셋({@code @Resources})을
 * {@code /assets/{items,runtime,fonts}}로 이관해 사용한다. (DESIGN.md §9)
 */
public final class Assets {

    private static final Logger log = LoggerFactory.getLogger(Assets.class);
    private static final List<String> IMAGE_DIRS = List.of("items", "runtime");
    private static final ConcurrentMap<String, Image> IMAGE_CACHE = new ConcurrentHashMap<>();

    private static String fontFamily = "System";

    private Assets() {
    }

    /** 이미지 파일명(예: {@code Computer.png})으로 로드. items → runtime 순으로 탐색, 없으면 default. */
    public static Image image(String name) {
        return IMAGE_CACHE.computeIfAbsent(name, Assets::loadImage);
    }

    /** {@code /assets/} 하위 상대경로로 이미지 로드 (예: {@code indicators/heart/fill.png}). */
    public static Image imageAt(String relativePath) {
        return IMAGE_CACHE.computeIfAbsent("@" + relativePath, k -> {
            var url = Assets.class.getResource("/assets/" + relativePath);
            if (url != null) {
                return new Image(url.toExternalForm());
            }
            log.warn("이미지를 찾을 수 없음: {}", relativePath);
            return null;
        });
    }

    private static Image loadImage(String name) {
        for (String dir : IMAGE_DIRS) {
            var url = Assets.class.getResource("/assets/" + dir + "/" + name);
            if (url != null) {
                return new Image(url.toExternalForm());
            }
        }
        var fallback = Assets.class.getResource("/assets/items/default.png");
        if (fallback != null) {
            return new Image(fallback.toExternalForm());
        }
        log.warn("이미지를 찾을 수 없음: {}", name);
        return null;
    }

    /** 픽셀 폰트 로드 (앱 시작 시 1회). 반환된 family 이름을 {@link #fontFamily()}로 제공. */
    public static void loadFonts() {
        var url = Assets.class.getResource("/assets/fonts/Galmuri9.ttf");
        if (url == null) {
            log.warn("폰트를 찾을 수 없음: Galmuri9.ttf");
            return;
        }
        Font font = Font.loadFont(url.toExternalForm(), 12);
        if (font != null) {
            fontFamily = font.getFamily();
        }
    }

    public static String fontFamily() {
        return fontFamily;
    }
}
