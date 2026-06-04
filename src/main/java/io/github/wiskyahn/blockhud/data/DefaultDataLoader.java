package io.github.wiskyahn.blockhud.data;

import io.github.wiskyahn.blockhud.data.dto.HudDataDto;
import io.github.wiskyahn.blockhud.domain.model.HudData;
import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 번들된 기본 데이터({@code /assets/default-items.json}) 로더.
 * 사용자 {@code items.json}이 없을 때 채워진 핫바를 보여주기 위함.
 */
public final class DefaultDataLoader {

    private static final Logger log = LoggerFactory.getLogger(DefaultDataLoader.class);
    private static final String RESOURCE = "/assets/default-items.json";

    private DefaultDataLoader() {
    }

    public static HudData load() {
        try (InputStream in = DefaultDataLoader.class.getResourceAsStream(RESOURCE)) {
            if (in == null) {
                return HudData.empty();
            }
            HudDataDto dto = JsonMappers.create().readValue(in, HudDataDto.class);
            return ItemMapper.toDomain(dto);
        } catch (Exception e) {
            log.warn("기본 데이터 로드 실패: {}", e.getMessage());
            return HudData.empty();
        }
    }
}
