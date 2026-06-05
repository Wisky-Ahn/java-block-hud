package io.github.wiskyahn.blockhud.data;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.wiskyahn.blockhud.app.AppPaths;
import io.github.wiskyahn.blockhud.domain.settings.Settings;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/** 설정 JSON 영속화(settings.json). 원본 SettingsPersistence 대체. (DESIGN.md §8.3) */
public final class SettingsRepository {

    private final Path file;
    private final ObjectMapper mapper = JsonMappers.create();

    public SettingsRepository(Path file) {
        this.file = file;
    }

    public static SettingsRepository atDefaultLocation() {
        return new SettingsRepository(AppPaths.settingsFile());
    }

    public Settings load() throws IOException {
        if (!Files.exists(file)) {
            return Settings.withDefaults();
        }
        Map<String, String> stored = mapper.readValue(
                Files.readAllBytes(file), new TypeReference<Map<String, String>>() { });
        // 기본값 위에 저장값 덮어쓰기
        Settings settings = Settings.withDefaults();
        stored.forEach(settings::set);
        return settings;
    }

    public void save(Settings settings) throws IOException {
        Path parent = file.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        mapper.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), settings.asMap());
    }
}
