package io.github.wiskyahn.blockhud.domain.update;

/**
 * 릴리스 자산. 원본 자산 매칭/검증 대상. (DESIGN.md §8.6)
 *
 * @param name        파일명 (예: java-block-hud_1.0.0_amd64.deb)
 * @param downloadUrl 다운로드 URL
 * @param size        바이트 크기
 * @param sha256      기대 SHA256 (GitHub API digest, 없으면 빈 문자열)
 */
public record ReleaseAsset(String name, String downloadUrl, long size, String sha256) {
}
