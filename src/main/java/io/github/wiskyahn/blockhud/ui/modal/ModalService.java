package io.github.wiskyahn.blockhud.ui.modal;

import io.github.wiskyahn.blockhud.i18n.LocalizationService;
import io.github.wiskyahn.blockhud.ui.common.Assets;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * 로컬라이즈된 알림/확인 모달. 플레이스홀더 치환 + 콜백 버튼. 원본 Modal.lua 대체. (DESIGN.md §8.5)
 */
public final class ModalService {

    private final LocalizationService i18n;

    public ModalService(LocalizationService i18n) {
        this.i18n = i18n;
    }

    /** 확인 다이얼로그. "확정" 시 {@code onConfirm} 실행. */
    public void confirm(String messageKey, Runnable onConfirm, Object... args) {
        Stage stage = baseStage(i18n.format(messageKey, args));
        VBox root = (VBox) stage.getScene().getRoot().lookup("#modalRoot");

        Button ok = button(i18n.get("modal.confirm"), () -> {
            stage.close();
            if (onConfirm != null) {
                onConfirm.run();
            }
        });
        Button cancel = button(i18n.get("modal.cancel"), stage::close);

        HBox buttons = new HBox(10, cancel, ok);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        root.getChildren().add(buttons);
        stage.showAndWait();
    }

    /** 단순 알림(확인 버튼만). */
    public void alert(String messageKey, Object... args) {
        Stage stage = baseStage(i18n.format(messageKey, args));
        VBox root = (VBox) stage.getScene().getRoot().lookup("#modalRoot");

        Button ok = button(i18n.get("modal.confirm"), stage::close);
        HBox buttons = new HBox(ok);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        root.getChildren().add(buttons);
        stage.showAndWait();
    }

    private Stage baseStage(String message) {
        Text text = new Text(message);
        text.setFont(Font.font(Assets.fontFamily(), 14));
        text.setFill(Color.WHITE);
        text.setTextAlignment(TextAlignment.CENTER);
        text.setWrappingWidth(280);

        VBox content = new VBox(16, text);
        content.setId("modalRoot");
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: rgba(30,30,30,0.96);"
                + " -fx-background-radius: 10; -fx-border-color: rgba(255,255,255,0.25);"
                + " -fx-border-radius: 10; -fx-border-width: 1;");

        StackPane wrapper = new StackPane(content);
        wrapper.setPadding(new Insets(12));
        wrapper.setStyle("-fx-background-color: transparent;");

        Scene scene = new Scene(wrapper);
        scene.setFill(Color.TRANSPARENT);

        Stage stage = new Stage();
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setAlwaysOnTop(true);
        stage.setScene(scene);
        return stage;
    }

    private Button button(String label, Runnable action) {
        Button b = new Button(label);
        b.setFont(Font.font(Assets.fontFamily(), 13));
        b.setStyle("-fx-background-color: rgba(255,255,255,0.12); -fx-text-fill: white;"
                + " -fx-background-radius: 6; -fx-padding: 6 16 6 16;");
        b.setOnAction(e -> action.run());
        return b;
    }
}
