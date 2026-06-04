package io.github.wiskyahn.blockhud.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/** 프로젝트 공용 Jackson {@link ObjectMapper} 설정. */
public final class JsonMappers {

    private JsonMappers() {
    }

    public static ObjectMapper create() {
        return new ObjectMapper()
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
}
