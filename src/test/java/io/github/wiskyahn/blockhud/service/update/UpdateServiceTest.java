package io.github.wiskyahn.blockhud.service.update;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.wiskyahn.blockhud.domain.update.Release;
import io.github.wiskyahn.blockhud.domain.update.ReleaseAsset;
import io.github.wiskyahn.blockhud.domain.update.SemanticVersion;
import io.github.wiskyahn.blockhud.platform.Platforms;
import java.util.List;
import org.junit.jupiter.api.Test;

class UpdateServiceTest {

    private static Release release(String tag, boolean pre, ReleaseAsset... assets) {
        return new Release(SemanticVersion.parse(tag), tag, pre, List.of(assets));
    }

    private static ReleaseAsset asset(String name) {
        return new ReleaseAsset(name, "https://x/" + name, 100, "");
    }

    @Test
    void detectsUpdateAvailable() {
        var svc = new UpdateService(SemanticVersion.parse("1.0.0"), Platforms.Os.LINUX);
        var result = svc.check(List.of(release("1.0.0", false), release("1.2.0", false)));
        assertEquals(UpdateService.Status.UPDATE_AVAILABLE, result.status());
        assertEquals(new SemanticVersion(1, 2, 0), result.latest());
    }

    @Test
    void detectsUpToDate() {
        var svc = new UpdateService(SemanticVersion.parse("1.2.0"), Platforms.Os.LINUX);
        var result = svc.check(List.of(release("1.0.0", false), release("1.2.0", false)));
        assertEquals(UpdateService.Status.UP_TO_DATE, result.status());
    }

    @Test
    void ignoresPrereleases() {
        var svc = new UpdateService(SemanticVersion.parse("1.0.0"), Platforms.Os.LINUX);
        var result = svc.check(List.of(release("1.0.0", false), release("2.0.0", true)));
        assertEquals(UpdateService.Status.UP_TO_DATE, result.status());
    }

    @Test
    void noReleasesWhenEmpty() {
        var svc = new UpdateService(SemanticVersion.parse("1.0.0"), Platforms.Os.LINUX);
        assertEquals(UpdateService.Status.NO_RELEASES, svc.check(List.of()).status());
    }

    @Test
    void selectsAssetByOsExtension() {
        Release r = release("1.0.0", false,
                asset("app_1.0.0.msi"), asset("app_1.0.0.dmg"), asset("app_1.0.0.deb"));

        assertTrue(new UpdateService(SemanticVersion.parse("0.1"), Platforms.Os.WINDOWS)
                .selectAsset(r).orElseThrow().name().endsWith(".msi"));
        assertTrue(new UpdateService(SemanticVersion.parse("0.1"), Platforms.Os.MAC)
                .selectAsset(r).orElseThrow().name().endsWith(".dmg"));
        assertTrue(new UpdateService(SemanticVersion.parse("0.1"), Platforms.Os.LINUX)
                .selectAsset(r).orElseThrow().name().endsWith(".deb"));
    }
}
