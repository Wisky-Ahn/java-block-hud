package io.github.wiskyahn.blockhud.domain.update;

import java.util.List;

/** GitHub 릴리스. (DESIGN.md §8.6) */
public record Release(SemanticVersion version, String tag, boolean prerelease,
                      List<ReleaseAsset> assets) {

    public Release {
        assets = List.copyOf(assets);
    }
}
