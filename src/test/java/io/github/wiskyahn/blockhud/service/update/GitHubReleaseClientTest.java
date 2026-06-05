package io.github.wiskyahn.blockhud.service.update;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.wiskyahn.blockhud.domain.update.Release;
import java.util.List;
import org.junit.jupiter.api.Test;

class GitHubReleaseClientTest {

    private static final String JSON = """
            [
              {
                "tag_name": "v1.2.0",
                "prerelease": false,
                "assets": [
                  {"name": "app_1.2.0.deb", "browser_download_url": "https://x/app.deb",
                   "size": 12345, "digest": "sha256:abc123"}
                ]
              },
              {
                "tag_name": "v1.3.0-rc1",
                "prerelease": true,
                "assets": []
              }
            ]
            """;

    @Test
    void parsesReleasesAndAssets() throws Exception {
        List<Release> releases = new GitHubReleaseClient("o", "r").parse(JSON);

        assertEquals(2, releases.size());
        Release first = releases.get(0);
        assertEquals("v1.2.0", first.tag());
        assertEquals(new io.github.wiskyahn.blockhud.domain.update.SemanticVersion(1, 2, 0),
                first.version());
        assertEquals(1, first.assets().size());
        assertEquals("app_1.2.0.deb", first.assets().get(0).name());
        assertEquals("sha256:abc123", first.assets().get(0).sha256());

        assertTrue(releases.get(1).prerelease());
    }

    @Test
    void emptyArrayYieldsNoReleases() throws Exception {
        assertTrue(new GitHubReleaseClient("o", "r").parse("[]").isEmpty());
    }
}
