package io.github.wiskyahn.blockhud.service.update;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.wiskyahn.blockhud.domain.update.Release;
import io.github.wiskyahn.blockhud.domain.update.ReleaseAsset;
import io.github.wiskyahn.blockhud.domain.update.SemanticVersion;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * GitHub Releases API 클라이언트. 원본 GetVersionReleaseCatalog.ps1 대체. (DESIGN.md §8.6)
 *
 * <p>{@code GET /repos/{owner}/{repo}/releases?per_page=100&page=N} 페이지네이션.
 * JSON 파싱은 {@link #parse(String)}로 분리해 단위 테스트 가능.
 */
public final class GitHubReleaseClient {

    private static final int PER_PAGE = 100;
    private static final int MAX_PAGES = 5;

    private final String owner;
    private final String repo;
    private final HttpClient http;
    private final ObjectMapper mapper = new ObjectMapper();

    public GitHubReleaseClient(String owner, String repo) {
        this(owner, repo, HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build());
    }

    public GitHubReleaseClient(String owner, String repo, HttpClient http) {
        this.owner = owner;
        this.repo = repo;
        this.http = http;
    }

    public List<Release> fetchReleases() throws Exception {
        List<Release> all = new ArrayList<>();
        for (int page = 1; page <= MAX_PAGES; page++) {
            String url = String.format(
                    "https://api.github.com/repos/%s/%s/releases?per_page=%d&page=%d",
                    owner, repo, PER_PAGE, page);
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .header("Accept", "application/vnd.github+json")
                    .header("User-Agent", "java-block-hud")
                    .GET()
                    .build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new IllegalStateException("GitHub API 응답 " + response.statusCode());
            }
            List<Release> batch = parse(response.body());
            if (batch.isEmpty()) {
                break;
            }
            all.addAll(batch);
        }
        return all;
    }

    /** GitHub releases JSON 배열을 {@link Release} 목록으로 파싱. */
    public List<Release> parse(String json) throws Exception {
        JsonNode root = mapper.readTree(json);
        List<Release> releases = new ArrayList<>();
        if (!root.isArray()) {
            return releases;
        }
        for (JsonNode node : root) {
            String tag = node.path("tag_name").asText("");
            boolean prerelease = node.path("prerelease").asBoolean(false);
            List<ReleaseAsset> assets = new ArrayList<>();
            for (JsonNode a : node.path("assets")) {
                assets.add(new ReleaseAsset(
                        a.path("name").asText(""),
                        a.path("browser_download_url").asText(""),
                        a.path("size").asLong(0),
                        a.path("digest").asText("")));
            }
            releases.add(new Release(SemanticVersion.parse(tag), tag, prerelease, assets));
        }
        return releases;
    }
}
