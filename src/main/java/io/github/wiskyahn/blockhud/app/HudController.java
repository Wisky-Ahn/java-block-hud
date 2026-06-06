package io.github.wiskyahn.blockhud.app;

import io.github.wiskyahn.blockhud.data.DefaultDataLoader;
import io.github.wiskyahn.blockhud.data.ItemRepository;
import io.github.wiskyahn.blockhud.data.SettingsRepository;
import io.github.wiskyahn.blockhud.domain.model.HudData;
import io.github.wiskyahn.blockhud.domain.model.IndicatorSource;
import io.github.wiskyahn.blockhud.domain.model.Item;
import io.github.wiskyahn.blockhud.domain.settings.Settings;
import io.github.wiskyahn.blockhud.i18n.LocalizationService;
import io.github.wiskyahn.blockhud.platform.Platforms;
import io.github.wiskyahn.blockhud.platform.StartupService;
import io.github.wiskyahn.blockhud.service.ActionLauncher;
import io.github.wiskyahn.blockhud.service.EditorDraft;
import io.github.wiskyahn.blockhud.service.SystemMetricsService;
import io.github.wiskyahn.blockhud.service.update.GitHubReleaseClient;
import io.github.wiskyahn.blockhud.service.update.UpdateService;
import io.github.wiskyahn.blockhud.ui.common.TooltipPopup;
import io.github.wiskyahn.blockhud.ui.editor.EditorWindow;
import io.github.wiskyahn.blockhud.ui.hud.ClockWindow;
import io.github.wiskyahn.blockhud.ui.hud.HotbarWindow;
import io.github.wiskyahn.blockhud.ui.hud.IndicatorWindow;
import io.github.wiskyahn.blockhud.ui.hud.InventoryWindow;
import io.github.wiskyahn.blockhud.ui.modal.ModalService;
import io.github.wiskyahn.blockhud.ui.settings.SettingsWindow;
import java.util.Map;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.input.KeyCode;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HUD 조정자. 데이터/설정/저장소/드래프트와 모든 창을 소유하고 실행·편집·설정 적용을 중재한다.
 * (DESIGN.md §3, §8.3, §8.4)
 */
public final class HudController {

    private static final Logger log = LoggerFactory.getLogger(HudController.class);

    private final ItemRepository repository = ItemRepository.atDefaultLocation();
    private final SettingsRepository settingsRepository = SettingsRepository.atDefaultLocation();
    private final ActionLauncher launcher = new ActionLauncher(Platforms.shell());
    private final StartupService startupService = new StartupService();
    private final ModalService modal;
    private final EditorWindow editor;
    private final SettingsWindow settingsWindow;
    private final HotbarWindow hotbar;
    private final InventoryWindow inventory;
    private final IndicatorWindow indicators;
    private final ClockWindow clock;

    private EditorDraft draft;
    private Settings settings;
    private boolean lastStartupEnabled;

    public HudController(LocalizationService i18n) {
        this.modal = new ModalService(i18n);
        this.editor = new EditorWindow(i18n);
        this.hotbar = new HotbarWindow(this::activate, this::edit);
        this.inventory = new InventoryWindow(this::activate, this::edit, this::openSettings);
        this.indicators = new IndicatorWindow(new SystemMetricsService());
        this.clock = new ClockWindow();
        this.settingsWindow = new SettingsWindow(i18n, Map.of(
                "resetAllSkinPositions", this::placeInitial,
                "openVersionManager", this::checkForUpdate,
                "openLogFolder", this::openLogFolder));

        launcher.registerInternal("_OPEN_INVENTORY_", inventory::toggle);
        launcher.registerInternal("_OPEN_SETTINGS_", this::openSettings);

        loadData();
        loadSettings();
    }

    private void loadData() {
        HudData data = resolveData();
        draft = new EditorDraft(data);
        hotbar.setItems(data.hotbar());
        inventory.setItems(data.inventory());
    }

    private HudData resolveData() {
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

    private void loadSettings() {
        try {
            settings = settingsRepository.load();
        } catch (Exception e) {
            log.warn("설정 로드 실패, 기본값 사용: {}", e.getMessage());
            settings = Settings.withDefaults();
        }
        lastStartupEnabled = settings.getBoolean("EnableRainmeterStartup");
        applySettings();
    }

    /** 설정값을 실제 창/서비스에 반영. */
    private void applySettings() {
        clock.setUse24Hour(settings.getBoolean("Use24HourClock"));
        indicators.setSources(
                IndicatorSource.fromKey(settings.get("HealthBarSource")),
                IndicatorSource.fromKey(settings.get("ArmorBarSource")),
                IndicatorSource.fromKey(settings.get("FoodBarSource")),
                IndicatorSource.fromKey(settings.get("AirBarSource")),
                IndicatorSource.fromKey(settings.get("ExpBarSource")));
        TooltipPopup.setEnabled(!settings.getBoolean("LowSpecDisableHoverTextTooltip"));
        inventory.setShowSteve(!settings.getBoolean("HideSteve"));

        toggleWindow(hotbar.stage(), settings.getBoolean("EnableHotbarSkin"));
        toggleWindow(clock.stage(), settings.getBoolean("EnableClockSkin"));
    }

    private void toggleWindow(Stage stage, boolean enabled) {
        if (enabled && !stage.isShowing()) {
            stage.show();
        } else if (!enabled && stage.isShowing()) {
            stage.hide();
        }
    }

    private void openSettings() {
        settingsWindow.open(settings, this::onSettingsSaved);
    }

    private void onSettingsSaved(Settings updated) {
        this.settings = updated;
        try {
            settingsRepository.save(updated);
        } catch (Exception e) {
            log.warn("설정 저장 실패: {}", e.getMessage());
        }
        applySettings();
        applyStartupIfChanged();
    }

    private void applyStartupIfChanged() {
        boolean enabled = settings.getBoolean("EnableRainmeterStartup");
        if (enabled != lastStartupEnabled) {
            startupService.setEnabled(enabled);
            lastStartupEnabled = enabled;
        }
    }

    /** 업데이트 확인 — 네트워크는 백그라운드, 결과는 FX 스레드 모달. */
    private void checkForUpdate() {
        new Thread(() -> {
            try {
                var releases = new GitHubReleaseClient(AppInfo.GITHUB_OWNER, AppInfo.GITHUB_REPO)
                        .fetchReleases();
                UpdateService.Result result =
                        new UpdateService(AppInfo.currentVersion()).check(releases);
                Platform.runLater(() -> showUpdateResult(result));
            } catch (Exception e) {
                log.warn("업데이트 확인 실패: {}", e.getMessage());
                Platform.runLater(() -> modal.alert("update.failed", e.getMessage()));
            }
        }, "update-check").start();
    }

    private void showUpdateResult(UpdateService.Result result) {
        switch (result.status()) {
            case UPDATE_AVAILABLE -> modal.alert("update.available", result.latest(), AppInfo.VERSION);
            case UP_TO_DATE -> modal.alert("update.upToDate", AppInfo.VERSION);
            case NO_RELEASES -> modal.alert("update.noReleases");
        }
    }

    private void openLogFolder() {
        try {
            java.nio.file.Files.createDirectories(Diagnostics.logDir());
            Platforms.shell().openPath(Diagnostics.logDir().toString());
        } catch (Exception e) {
            log.warn("로그 폴더 열기 실패: {}", e.getMessage());
        }
    }

    private void activate(Item item) {
        if (item.confirmBeforeRun()) {
            modal.confirm("action.confirm.run", () -> launcher.launch(item.action()), item.label());
        } else {
            launcher.launch(item.action());
        }
    }

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
        installShortcuts();
        applySettings();
        // 주의: macOS 바탕화면 레벨(kCGDesktopWindowLevel) 고정은 클릭 이벤트를 막는다.
        // 런처는 상호작용이 핵심이므로 toBack(클릭 가능 + 다른 창 뒤) 방식을 사용한다.
        // 네이티브 고정(WindowPinService)은 향후 '표시 전용' 옵션으로만 제공 예정.
    }

    /** 핫바에 단축키: S=설정, I=인벤토리 토글. */
    private void installShortcuts() {
        if (hotbar.stage().getScene() != null) {
            hotbar.stage().getScene().setOnKeyPressed(e -> {
                if (e.getCode() == KeyCode.S) {
                    openSettings();
                } else if (e.getCode() == KeyCode.I) {
                    inventory.toggle();
                }
            });
        }
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

        // 인디케이터: 핫바 폭과 동일, 핫바 바로 위
        Stage ind = indicators.stage();
        ind.setX(hotbarX);
        ind.setY(hotbarY - io.github.wiskyahn.blockhud.ui.hud.IndicatorWindow.WIN_H - 2);

        Stage clk = clock.stage();
        clk.setX(screen.getMinX() + (screen.getWidth() - 160) / 2);
        clk.setY(screen.getMinY() + 24);
    }
}
