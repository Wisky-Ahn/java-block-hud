package io.github.wiskyahn.blockhud.app;

import io.github.wiskyahn.blockhud.data.DefaultDataLoader;
import io.github.wiskyahn.blockhud.data.ItemRepository;
import io.github.wiskyahn.blockhud.domain.model.HudData;
import io.github.wiskyahn.blockhud.platform.Platforms;
import io.github.wiskyahn.blockhud.service.ActionLauncher;
import io.github.wiskyahn.blockhud.ui.common.Assets;
import io.github.wiskyahn.blockhud.ui.hud.HotbarWindow;
import io.github.wiskyahn.blockhud.ui.hud.InventoryWindow;
import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 앱 부트스트랩. 데이터 로드 → HUD 창(핫바/인벤토리) 생성·배치. (DESIGN.md §3 ui)
 *
 * <p>Phase 2: 핫바 렌더링 + 클릭 실행 + 드래그/스냅, 인벤토리 토글({@code _OPEN_INVENTORY_}).
 * 정밀 배치(LayoutEngine §8.1)와 설정 연동은 이후 단계에서 확장한다.
 */
public class HudApplication extends Application {

    private static final Logger log = LoggerFactory.getLogger(HudApplication.class);

    @Override
    public void start(Stage primaryStage) {
        Assets.loadFonts();

        HudData data = loadData();
        ActionLauncher launcher = new ActionLauncher(Platforms.shell());

        HotbarWindow hotbar = new HotbarWindow(data.hotbar(), launcher);
        InventoryWindow inventory = new InventoryWindow(data.inventory(), launcher);

        // 내부 명령: 핫바의 "인벤토리" 슬롯 → 인벤토리 토글
        launcher.registerInternal("_OPEN_INVENTORY_", inventory::toggle);

        placeInitial(hotbar.stage(), inventory.stage());
        hotbar.show();
    }

    private HudData loadData() {
        try {
            HudData saved = ItemRepository.atDefaultLocation().load();
            if (!saved.hotbar().isEmpty() || !saved.inventory().isEmpty()) {
                return saved;
            }
        } catch (Exception e) {
            log.warn("저장된 데이터 로드 실패, 기본 데이터 사용: {}", e.getMessage());
        }
        return DefaultDataLoader.load();
    }

    /** 핫바를 화면 하단 중앙에, 인벤토리를 화면 중앙에 임시 배치 (LayoutEngine 도입 전). */
    private void placeInitial(Stage hotbarStage, Stage inventoryStage) {
        Rectangle2D screen = Screen.getPrimary().getVisualBounds();
        hotbarStage.setX(screen.getMinX() + (screen.getWidth() - hotbarStage.getScene().getWidth()) / 2);
        hotbarStage.setY(screen.getMaxY() - hotbarStage.getScene().getHeight() - 8);

        inventoryStage.setX(screen.getMinX() + (screen.getWidth() - inventoryStage.getScene().getWidth()) / 2);
        inventoryStage.setY(screen.getMinY() + (screen.getHeight() - inventoryStage.getScene().getHeight()) / 2);
    }
}
