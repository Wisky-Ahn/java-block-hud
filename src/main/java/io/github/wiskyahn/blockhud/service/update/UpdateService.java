package io.github.wiskyahn.blockhud.service.update;

import io.github.wiskyahn.blockhud.domain.update.Release;
import io.github.wiskyahn.blockhud.domain.update.ReleaseAsset;
import io.github.wiskyahn.blockhud.domain.update.SemanticVersion;
import io.github.wiskyahn.blockhud.platform.Platforms;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * 업데이트 판정/다운로드/검증. 원본 UpdateToLatestVersion + InstallVersionRelease 대체. (DESIGN.md §8.6)
 *
 * <p>크로스플랫폼: variant 축은 OS별 설치본(.msi/.dmg/.deb). 실행 중 자기 바이너리 교체는
 * OS별 제약이 있어(§8.6) 여기서는 "확인 → 다운로드 → 검증"까지 책임지고 설치는 위임한다.
 */
public final class UpdateService {

    public enum Status { UP_TO_DATE, UPDATE_AVAILABLE, NO_RELEASES }

    public record Result(Status status, SemanticVersion latest, Release release) {
    }

    private final SemanticVersion current;
    private final Platforms.Os os;

    public UpdateService(SemanticVersion current) {
        this(current, Platforms.current());
    }

    public UpdateService(SemanticVersion current, Platforms.Os os) {
        this.current = current;
        this.os = os;
    }

    /** 안정 릴리스 중 최신을 현재 버전과 비교. */
    public Result check(List<Release> releases) {
        Optional<Release> latest = releases.stream()
                .filter(r -> !r.prerelease())
                .max(Comparator.comparing(Release::version));
        if (latest.isEmpty()) {
            return new Result(Status.NO_RELEASES, current, null);
        }
        Release release = latest.get();
        Status status = release.version().isNewerThan(current)
                ? Status.UPDATE_AVAILABLE : Status.UP_TO_DATE;
        return new Result(status, release.version(), release);
    }

    /** 현재 OS에 맞는 설치본 자산 선택. */
    public Optional<ReleaseAsset> selectAsset(Release release) {
        List<String> extensions = switch (os) {
            case WINDOWS -> List.of(".msi", ".exe");
            case MAC -> List.of(".dmg", ".pkg");
            case LINUX -> List.of(".deb", ".rpm", ".appimage");
        };
        for (String ext : extensions) {
            Optional<ReleaseAsset> match = release.assets().stream()
                    .filter(a -> a.name().toLowerCase(Locale.ROOT).endsWith(ext))
                    .findFirst();
            if (match.isPresent()) {
                return match;
            }
        }
        return Optional.empty();
    }

    /** 자산 다운로드 → {@code dir/자산명} 경로 반환. */
    public Path download(ReleaseAsset asset, Path dir, HttpClient http) throws Exception {
        Files.createDirectories(dir);
        Path target = dir.resolve(asset.name());
        HttpRequest request = HttpRequest.newBuilder(URI.create(asset.downloadUrl()))
                .header("User-Agent", "java-block-hud")
                .GET()
                .build();
        HttpResponse<Path> response = http.send(request, HttpResponse.BodyHandlers.ofFile(target));
        if (response.statusCode() != 200) {
            throw new IllegalStateException("다운로드 실패 " + response.statusCode());
        }
        return target;
    }

    /** SHA256 검증 (자산 digest 기준, 없으면 생략). */
    public boolean verify(Path file, ReleaseAsset asset) throws IOException {
        return Sha256.matches(file, asset.sha256());
    }
}
