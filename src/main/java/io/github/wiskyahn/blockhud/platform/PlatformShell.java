package io.github.wiskyahn.blockhud.platform;

import io.github.wiskyahn.blockhud.domain.action.WellKnownTargetId;

/**
 * OS별 실행/열기 동작 추상화. 원본의 Windows 셸 의존 PowerShell/Bang을 대체한다.
 * (DESIGN.md §3 platform, §5 "기능적 1:1")
 */
public interface PlatformShell {

    /** 실행파일/명령 실행 (예: {@code calc.exe}). OS 종속적. */
    void launchProgram(String command) throws Exception;

    /** 파일/폴더 경로 열기. */
    void openPath(String path) throws Exception;

    /** 웹 링크 열기. */
    void openUrl(String url) throws Exception;

    /** "내 PC/휴지통/다운로드" 등 잘 알려진 대상을 OS에 맞게 연다. */
    void openWellKnown(WellKnownTargetId id) throws Exception;
}
