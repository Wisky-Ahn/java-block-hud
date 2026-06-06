package io.github.wiskyahn.blockhud.platform;

/**
 * 창을 바탕화면 레벨(다른 창 영구히 뒤)에 고정. 원본 ZPos -2(ON DESKTOP) 완전 재현.
 * (DESIGN.md §8.1) JavaFX엔 API가 없어 OS별 네이티브로 구현한다.
 */
public interface WindowPinService {

    /** 제목에 {@code titleMarker}를 포함하는 창들을 바탕화면 레벨로 보냄. */
    void pinToDesktop(String titleMarker);

    /** 미지원 플랫폼용 no-op. */
    WindowPinService NOOP = marker -> { };
}
