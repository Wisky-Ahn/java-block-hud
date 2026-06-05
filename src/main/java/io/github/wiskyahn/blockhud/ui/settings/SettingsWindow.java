package io.github.wiskyahn.blockhud.ui.settings;

import io.github.wiskyahn.blockhud.domain.settings.ControlType;
import io.github.wiskyahn.blockhud.domain.settings.SettingField;
import io.github.wiskyahn.blockhud.domain.settings.Settings;
import io.github.wiskyahn.blockhud.domain.settings.SettingsSchema;
import io.github.wiskyahn.blockhud.domain.settings.SettingsTab;
import io.github.wiskyahn.blockhud.i18n.LocalizationService;
import io.github.wiskyahn.blockhud.ui.common.Assets;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * 데이터 드리븐 설정창 — {@link SettingsSchema}의 7탭/필드에서 폼을 자동 생성한다.
 * 실행취소/재실행/리셋/저장 지원. 원본 Settings.ini + SettingsRender 대체. (DESIGN.md §8.3)
 */
public final class SettingsWindow {

    private final LocalizationService i18n;
    private final Map<String, Runnable> actions;
    private final Stage stage = new Stage();

    private Settings working;
    private Consumer<Settings> onSave;
    private final Deque<Map<String, String>> undo = new ArrayDeque<>();
    private final Deque<Map<String, String>> redo = new ArrayDeque<>();

    private final TabPane tabPane = new TabPane();
    private Button undoButton;
    private Button redoButton;

    /** @param actions ACTION 필드 키 → 핸들러 (resetAllSettings는 내부 처리). */
    public SettingsWindow(LocalizationService i18n, Map<String, Runnable> actions) {
        this.i18n = i18n;
        this.actions = actions;
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setAlwaysOnTop(true);
        stage.setTitle("Block HUD — Settings");
    }

    public void open(Settings current, Consumer<Settings> saveCallback) {
        this.working = current.copy();
        this.onSave = saveCallback;
        undo.clear();
        redo.clear();
        stage.setScene(buildScene());
        stage.show();
        stage.centerOnScreen();
    }

    public Stage stage() {
        return stage;
    }

    private Scene buildScene() {
        rebuildTabs();

        undoButton = toolButton("↶", this::doUndo);
        redoButton = toolButton("↷", this::doRedo);
        Button reset = toolButton(i18n.get("settings.reset"), this::doResetAll);
        HBox header = new HBox(8, title(i18n.get("settings.title")), spacer(), undoButton, redoButton, reset);
        header.setAlignment(Pos.CENTER_LEFT);

        Button save = footerButton(i18n.get("settings.save"), () -> {
            stage.close();
            onSave.accept(working);
        });
        Button cancel = footerButton(i18n.get("modal.cancel"), stage::close);
        HBox footer = new HBox(10, spacer(), cancel, save);
        footer.setAlignment(Pos.CENTER_RIGHT);

        VBox rootBox = new VBox(12, header, tabPane, footer);
        rootBox.setPadding(new Insets(16));
        rootBox.setStyle("-fx-background-color: rgba(26,26,26,0.98); -fx-background-radius: 10;"
                + " -fx-border-color: rgba(255,255,255,0.2); -fx-border-radius: 10; -fx-border-width: 1;");
        rootBox.setPrefWidth(420);

        updateUndoRedoState();

        Scene scene = new Scene(rootBox);
        scene.setFill(Color.TRANSPARENT);
        return scene;
    }

    private void rebuildTabs() {
        int selected = tabPane.getSelectionModel().getSelectedIndex();
        tabPane.getTabs().clear();
        for (SettingsTab tab : SettingsSchema.tabs()) {
            Tab uiTab = new Tab(i18n.get(tab.labelKey()));
            uiTab.setClosable(false);
            VBox content = new VBox(10);
            content.setPadding(new Insets(14));
            for (SettingField field : tab.fields()) {
                content.getChildren().add(buildRow(field));
            }
            ScrollPane scroll = new ScrollPane(content);
            scroll.setFitToWidth(true);
            scroll.setPrefHeight(300);
            scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
            uiTab.setContent(scroll);
            tabPane.getTabs().add(uiTab);
        }
        if (selected >= 0 && selected < tabPane.getTabs().size()) {
            tabPane.getSelectionModel().select(selected);
        }
    }

    private Region buildRow(SettingField field) {
        Label label = new Label(i18n.get(field.labelKey()));
        label.setFont(Font.font(Assets.fontFamily(), 13));
        label.setTextFill(Color.web("#dddddd"));
        label.setPrefWidth(180);

        Region control = buildControl(field);
        HBox row = new HBox(10, label, control);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private Region buildControl(SettingField field) {
        return switch (field.type()) {
            case TOGGLE -> {
                CheckBox cb = new CheckBox();
                cb.setSelected(working.getBoolean(field.key()));
                cb.setOnAction(e -> commit(field.key(), cb.isSelected() ? "1" : "0"));
                yield cb;
            }
            case STEPPER -> {
                Spinner<Integer> sp = new Spinner<>(field.min(), field.max(),
                        working.getInt(field.key(), field.min()), field.step());
                sp.setEditable(true);
                sp.setPrefWidth(100);
                sp.valueProperty().addListener((o, a, b) -> commit(field.key(), String.valueOf(b)));
                yield sp;
            }
            case TEXT -> {
                TextField tf = new TextField(working.get(field.key()));
                tf.focusedProperty().addListener((o, was, focused) -> {
                    if (!focused) {
                        commit(field.key(), tf.getText());
                    }
                });
                yield tf;
            }
            case DROPDOWN -> {
                ComboBox<String> cb = new ComboBox<>();
                cb.getItems().addAll(field.options());
                cb.setValue(working.get(field.key()));
                cb.valueProperty().addListener((o, a, b) -> commit(field.key(), b));
                yield cb;
            }
            case ACTION -> {
                Button b = footerButton(i18n.get(field.labelKey()), () -> runAction(field.key()));
                yield b;
            }
            case READONLY -> {
                Label v = new Label(working.get(field.key()));
                v.setFont(Font.font(Assets.fontFamily(), 13));
                v.setTextFill(Color.web("#999999"));
                yield v;
            }
        };
    }

    private void commit(String key, String value) {
        pushUndo();
        working.set(key, value);
    }

    private void runAction(String key) {
        if ("resetAllSettings".equals(key)) {
            doResetAll();
            return;
        }
        Runnable handler = actions.get(key);
        if (handler != null) {
            handler.run();
        }
    }

    private void pushUndo() {
        undo.push(working.asMap());
        redo.clear();
        updateUndoRedoState();
    }

    private void doUndo() {
        if (undo.isEmpty()) {
            return;
        }
        redo.push(working.asMap());
        working = new Settings(undo.pop());
        rebuildTabs();
        updateUndoRedoState();
    }

    private void doRedo() {
        if (redo.isEmpty()) {
            return;
        }
        undo.push(working.asMap());
        working = new Settings(redo.pop());
        rebuildTabs();
        updateUndoRedoState();
    }

    private void doResetAll() {
        pushUndo();
        working = Settings.withDefaults();
        rebuildTabs();
    }

    private void updateUndoRedoState() {
        if (undoButton != null) {
            undoButton.setDisable(undo.isEmpty());
            redoButton.setDisable(redo.isEmpty());
        }
    }

    private Label title(String text) {
        Label l = new Label(text);
        l.setFont(Font.font(Assets.fontFamily(), 16));
        l.setTextFill(Color.web("#9FE870"));
        return l;
    }

    private Region spacer() {
        Region r = new Region();
        HBox.setHgrow(r, Priority.ALWAYS);
        return r;
    }

    private Button toolButton(String text, Runnable action) {
        Button b = new Button(text);
        b.setFont(Font.font(Assets.fontFamily(), 12));
        b.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-text-fill: white;"
                + " -fx-background-radius: 5; -fx-padding: 4 10 4 10;");
        b.setOnAction(e -> action.run());
        return b;
    }

    private Button footerButton(String text, Runnable action) {
        Button b = new Button(text);
        b.setFont(Font.font(Assets.fontFamily(), 13));
        b.setStyle("-fx-background-color: rgba(255,255,255,0.14); -fx-text-fill: white;"
                + " -fx-background-radius: 6; -fx-padding: 6 16 6 16;");
        b.setOnAction(e -> action.run());
        return b;
    }
}
