package academy.cashbackmerge.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.io.IOException;
import java.nio.file.Path;

public final class ConfigLoader {

    // Поле _note и настройки наград для MR3 пока не нужны. Jackson их пропускает.
    private static final JsonMapper MAPPER = JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();

    private ConfigLoader() {}

    public static CampaignConfig load(Path path) {
        try {
            return MAPPER.readValue(path.toFile(), CampaignConfig.class);
        } catch (IOException e) {
            throw new IllegalArgumentException(
                    "Конфиг " + path + " не загружен: " + reason(e).getMessage(), e);
        }
    }

    /** Если сработала проверка в нашем конструкторе, Jackson оборачивает её исключение. Тогда нужно именно оно. */
    private static Exception reason(IOException e) {
        if (e.getCause() instanceof IllegalArgumentException check) {
            return check;
        }
        return e;
    }
}
