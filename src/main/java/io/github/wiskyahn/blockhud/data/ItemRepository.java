package io.github.wiskyahn.blockhud.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.wiskyahn.blockhud.app.AppPaths;
import io.github.wiskyahn.blockhud.data.dto.HudDataDto;
import io.github.wiskyahn.blockhud.domain.model.HudData;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 핫바/인벤토리 아이템의 JSON 영속화. 원본 Lua {@code ItemDataRepository} 대체. (DESIGN.md §4)
 */
public final class ItemRepository {

    private final Path file;
    private final ObjectMapper mapper;

    public ItemRepository(Path file) {
        this.file = file;
        this.mapper = JsonMappers.create();
    }

    /** 기본 위치({@link AppPaths#itemsFile()})를 사용하는 저장소. */
    public static ItemRepository atDefaultLocation() {
        return new ItemRepository(AppPaths.itemsFile());
    }

    /** 파일이 없으면 빈 데이터 반환. */
    public HudData load() throws IOException {
        if (!Files.exists(file)) {
            return HudData.empty();
        }
        HudDataDto dto = mapper.readValue(Files.readAllBytes(file), HudDataDto.class);
        return ItemMapper.toDomain(dto);
    }

    public void save(HudData data) throws IOException {
        Path parent = file.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        HudDataDto dto = ItemMapper.toDto(data);
        mapper.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), dto);
    }
}
