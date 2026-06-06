package io.github.wiskyahn.blockhud.ui.common;

import javafx.stage.Stage;

/**
 * 창 Z-순서 제어. 원본 ZPosArrangement(핫바/시계/인디케이터 = -2 ON DESKTOP) 대응. (DESIGN.md §8.1)
 *
 * <p>JavaFX엔 "바탕화면 레벨" API가 없어 always-on-top 해제 + {@link Stage#toBack()}로 근사한다.
 * 완전한 OS별 데스크톱 고정(NSWindow level / WorkerW)은 후속(Phase 7) 네이티브 작업.
 */
public final class WindowLevel {

    private WindowLevel() {
    }

    /** 바탕화면 위젯처럼 다른 창 뒤로 보냄 (핫바/시계/인디케이터). show() 이후 호출. */
    public static void sendToBack(Stage stage) {
        stage.setAlwaysOnTop(false);
        stage.toBack();
    }

    /** 조작용 오버레이를 앞으로 (인벤토리/에디터/설정). */
    public static void bringToFront(Stage stage) {
        stage.setAlwaysOnTop(false);
        stage.toFront();
    }
}
