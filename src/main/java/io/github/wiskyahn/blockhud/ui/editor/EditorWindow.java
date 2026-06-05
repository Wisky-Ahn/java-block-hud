package io.github.wiskyahn.blockhud.ui.editor;

import io.github.wiskyahn.blockhud.domain.action.ActionText;
import io.github.wiskyahn.blockhud.domain.action.ItemActionParser;
import io.github.wiskyahn.blockhud.domain.model.ImageOffset;
import io.github.wiskyahn.blockhud.domain.model.Item;
import io.github.wiskyahn.blockhud.i18n.LocalizationService;
import io.github.wiskyahn.blockhud.ui.common.Assets;
import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * 슬롯 편집 창. 이름/실행경로/이미지/개수/이미지조정/실행전확인을 편집하고 저장한다.
 * 원본 Editor.ini + EditorItemService 대체. (DESIGN.md §3 ui, §8.4)
 */
public final class EditorWindow {

    private final LocalizationService i18n;
    private final Stage stage = new Stage();
    private double dragOffsetX;
    private double dragOffsetY;

    public EditorWindow(LocalizationService i18n) {
        this.i18n = i18n;
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setAlwaysOnTop(true);
        stage.setTitle("Block HUD — Editor");
    }

    /** 주어진 슬롯을 편집. 저장 시 {@code onSave}에 새 Item 전달. */
    public void edit(Item item, Consumer<Item> onSave) {
        TextField labelField = textField(item.label());
        TextField actionField = textField(ActionText.format(item.action()));
        TextField imageField = textField(item.image());
        Spinner<Integer> qty = spinner(0, 9999, item.qty());
        Spinner<Integer> offX = spinner(-200, 200, item.offset().x());
        Spinner<Integer> offY = spinner(-200, 200, item.offset().y());
        Spinner<Integer> offSize = spinner(-200, 200, item.offset().size());
        CheckBox confirm = new CheckBox(i18n.get("editor.confirmRun"));
        confirm.setSelected(item.confirmBeforeRun());
        styleControl(confirm);

        Button save = button(i18n.get("editor.save"), () -> {
            Item edited = new Item(
                    item.address(),
                    labelField.getText(),
                    ItemActionParser.parse(actionField.getText()),
                    imageField.getText(),
                    qty.getValue(),
                    new ImageOffset(offX.getValue(), offY.getValue(), offSize.getValue()),
                    confirm.isSelected());
            stage.close();
            onSave.accept(edited);
        });
        Button cancel = button(i18n.get("modal.cancel"), stage::close);

        HBox offsetRow = new HBox(8,
                label(i18n.get("editor.offsetX")), offX,
                label(i18n.get("editor.offsetY")), offY,
                label(i18n.get("editor.offsetSize")), offSize);
        offsetRow.setAlignment(Pos.CENTER_LEFT);

        HBox actions = new HBox(10, cancel, save);
        actions.setAlignment(Pos.CENTER_RIGHT);

        VBox form = new VBox(10,
                title(i18n.get("editor.title")),
                field(i18n.get("editor.name"), labelField),
                field(i18n.get("editor.path"), actionField),
                field(i18n.get("editor.image"), imageField),
                field(i18n.get("editor.qty"), qty),
                title(i18n.get("editor.imageAdjust")),
                offsetRow,
                confirm,
                actions);
        form.setPadding(new Insets(18));
        form.setStyle("-fx-background-color: rgba(28,28,28,0.97); -fx-background-radius: 10;"
                + " -fx-border-color: rgba(255,255,255,0.22); -fx-border-radius: 10; -fx-border-width: 1;");
        form.setPrefWidth(360);

        enableDrag(form);

        Scene scene = new Scene(form);
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);
        stage.show();
        stage.centerOnScreen();
    }

    public Stage stage() {
        return stage;
    }

    private void enableDrag(Region handle) {
        handle.setOnMousePressed(e -> {
            dragOffsetX = e.getSceneX();
            dragOffsetY = e.getSceneY();
        });
        handle.setOnMouseDragged(e -> {
            stage.setX(e.getScreenX() - dragOffsetX);
            stage.setY(e.getScreenY() - dragOffsetY);
        });
    }

    private VBox field(String labelText, javafx.scene.Node control) {
        VBox box = new VBox(3, label(labelText), control);
        return box;
    }

    private Label title(String text) {
        Label l = new Label(text);
        l.setFont(Font.font(Assets.fontFamily(), 15));
        l.setTextFill(Color.web("#9FE870"));
        return l;
    }

    private Label label(String text) {
        Label l = new Label(text);
        l.setFont(Font.font(Assets.fontFamily(), 12));
        l.setTextFill(Color.web("#cccccc"));
        return l;
    }

    private TextField textField(String value) {
        TextField tf = new TextField(value);
        tf.setFont(Font.font(Assets.fontFamily(), 13));
        return tf;
    }

    private Spinner<Integer> spinner(int min, int max, int value) {
        Spinner<Integer> s = new Spinner<>(min, max, value);
        s.setEditable(true);
        s.setPrefWidth(80);
        return s;
    }

    private void styleControl(CheckBox cb) {
        cb.setFont(Font.font(Assets.fontFamily(), 13));
        cb.setTextFill(Color.web("#cccccc"));
    }

    private Button button(String text, Runnable action) {
        Button b = new Button(text);
        b.setFont(Font.font(Assets.fontFamily(), 13));
        b.setStyle("-fx-background-color: rgba(255,255,255,0.14); -fx-text-fill: white;"
                + " -fx-background-radius: 6; -fx-padding: 6 16 6 16;");
        b.setOnAction(e -> action.run());
        return b;
    }
}
