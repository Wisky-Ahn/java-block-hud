package io.github.wiskyahn.blockhud.app;

import io.github.wiskyahn.blockhud.data.DefaultDataLoader;
import io.github.wiskyahn.blockhud.data.ItemRepository;
import io.github.wiskyahn.blockhud.domain.model.HudData;
import io.github.wiskyahn.blockhud.domain.model.Item;
import io.github.wiskyahn.blockhud.i18n.LocalizationService;
import io.github.wiskyahn.blockhud.platform.Platforms;
import io.github.wiskyahn.blockhud.service.ActionLauncher;
import io.github.wiskyahn.blockhud.service.EditorDraft;
import io.github.wiskyahn.blockhud.service.SystemMetricsService;
import io.github.wiskyahn.blockhud.ui.editor.EditorWindow;
import io.github.wiskyahn.blockhud.ui.hud.ClockWindow;
import io.github.wiskyahn.blockhud.ui.hud.HotbarWindow;
import io.github.wiskyahn.blockhud.ui.hud.IndicatorWindow;
import io.github.wiskyahn.blockhud.ui.hud.InventoryWindow;
import io.github.wiskyahn.blockhud.ui.modal.ModalService;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HUD 조정자. 데이터/저장소/드래프트와 창들을 소유하고 실행·편집·저장·새로고침을 중재한다.
 * (DESIGN.md §3, §8.4 드래프트→커밋)
 */
public final class HudController {

    private static final Logger log = LoggerFactory.getLogger(HudController.class);

    private final ItemRepository repository = ItemRepository.atDefaultLocation();
    private final ActionLauncher launcher = new ActionLauncher(Platforms.shell());
    private final ModalService modal;
    private final EditorWindow editor;
    private final HotbarWindow hotbar;
    private final InventoryWindow inventory;
    private final IndicatorWindow indicators;
    private final ClockWindow clock;

    private EditorDraft draft;

    public HudController(LocalizationService i18n) {
        this.modal = new ModalService(i18n);
        this.editor = new EditorWindow(i18n);
        this.hotbar = new HotbarWindow(this::activate, this::edit);
        this.inventory = new InventoryWindow(this::activate, this::edit);
        this.indicators = new IndicatorWindow(new SystemMetricsService());
        this.clock = new ClockWindow();
        launcher.registerInternal("_OPEN_INVENTORY_", inventory::toggle);
        load();
    }

    private void load() {
        HudData data = loadData();
        draft = new EditorDraft(data);
        hotbar.setItems(data.hotbar());
        inventory.setItems(data.inventory());
    }

    private HudData loadData() {
        try {
            HudData saved = repository.load();
            if (!saved.hotbar().isEmpty() || !saved.inventory().isEmpty()) {
                return saved;
            }
        } catch (Exception e) {
            log.warn("저장 데이터 로드 실패, 기본 데이터 사용: {}", e.getMessage());
        }
        return DefaultDataLoader.load();
    }

    /** 좌클릭 실행 — confirmBeforeRun이면 확인 모달 후 실행. */
    private void activate(Item item) {
        if (item.confirmBeforeRun()) {
            modal.confirm("action.confirm.run", () -> launcher.launch(item.action()), item.label());
        } else {
            launcher.launch(item.action());
        }
    }

    /** 우클릭 편집 — 저장 시 드래프트에 반영 → 영속화 → HUD 새로고침. */
    private void edit(Item item) {
        editor.edit(item, edited -> {
            draft.put(edited);
            HudData updated = draft.working();
            try {
                repository.save(updated);
                draft.markCommitted();
            } catch (Exception e) {
                log.warn("저장 실패: {}", e.getMessage());
            }
            hotbar.setItems(updated.hotbar());
            inventory.setItems(updated.inventory());
        });
    }

    public void show() {
        placeInitial();
        hotbar.show();
        indicators.show();
        clock.show();
    }

    private void placeInitial() {
        Rectangle2D screen = Screen.getPrimary().getVisualBounds();

        Stage h = hotbar.stage();
        double hotbarX = screen.getMinX() + (screen.getWidth() - h.getScene().getWidth()) / 2;
        double hotbarY = screen.getMaxY() - h.getScene().getHeight() - 8;
        h.setX(hotbarX);
        h.setY(hotbarY);

        Stage inv = inventory.stage();
        inv.setX(screen.getMinX() + (screen.getWidth() - inv.getScene().getWidth()) / 2);
        inv.setY(screen.getMinY() + (screen.getHeight() - inv.getScene().getHeight()) / 2);

        // 인디케이터: 핫바 좌측 위로 스택
        Stage ind = indicators.stage();
        ind.setX(hotbarX);
        ind.setY(hotbarY - 180);

        // 시계: 화면 상단 중앙
        Stage clk = clock.stage();
        clk.setX(screen.getMinX() + (screen.getWidth() - 160) / 2);
        clk.setY(screen.getMinY() + 24);
    }
}
