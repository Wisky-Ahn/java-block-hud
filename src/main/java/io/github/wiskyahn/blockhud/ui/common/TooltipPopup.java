package io.github.wiskyahn.blockhud.ui.common;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.text.Font;
import javafx.stage.Popup;

/**
 * 슬롯 호버 시 아이템 이름을 표시하는 툴팁. 원본 Tooltip.lua 대체. (DESIGN.md §8.5)
 *
 * <p>저사양 모드({@code lowSpecDisableHoverTextTooltip})에서 {@link #setEnabled(boolean)}로 끌 수 있다.
 */
public final class TooltipPopup {

    private static volatile boolean enabled = true;

    private final Popup popup = new Popup();
    private final Label label = new Label();

    public TooltipPopup() {
        label.setFont(Font.font(Assets.fontFamily(), 13));
        label.setStyle("-fx-background-color: rgba(20,20,20,0.92); -fx-text-fill: white;"
                + " -fx-padding: 4 8 4 8; -fx-background-radius: 4;");
        popup.getContent().add(label);
        popup.setAutoFix(true);
    }

    public static void setEnabled(boolean value) {
        enabled = value;
    }

    public void show(Node owner, String text, double screenX, double screenY) {
        if (!enabled || text == null || text.isBlank()) {
            hide();
            return;
        }
        label.setText(text);
        popup.show(owner, screenX + 14, screenY + 18);
    }

    public void hide() {
        popup.hide();
    }
}
