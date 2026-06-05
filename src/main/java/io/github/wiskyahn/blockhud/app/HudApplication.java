package io.github.wiskyahn.blockhud.app;

import io.github.wiskyahn.blockhud.i18n.LocalizationService;
import io.github.wiskyahn.blockhud.ui.common.Assets;
import java.util.Locale;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * 앱 부트스트랩. 폰트/다국어 초기화 후 {@link HudController}에 위임한다. (DESIGN.md §3 ui)
 *
 * <p>Phase 3: 핫바/인벤토리 렌더링 + 실행(+확인 모달) + 우클릭 편집(드래프트→저장→새로고침) + 호버 툴팁.
 */
public class HudApplication extends Application {

    @Override
    public void start(Stage primaryStage) {
        Assets.loadFonts();
        LocalizationService i18n = new LocalizationService(Locale.getDefault());
        new HudController(i18n).show();
    }
}
