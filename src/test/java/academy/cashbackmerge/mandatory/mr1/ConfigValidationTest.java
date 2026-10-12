package academy.cashbackmerge.mandatory.mr1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import academy.cashbackmerge.config.CampaignConfig;
import academy.cashbackmerge.config.ConfigLoader;
import academy.cashbackmerge.game.Cashback;
import academy.cashbackmerge.game.SpawnRule;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Обязательные тесты: конфиг кампании. */
@DisplayName("MR1. Конфиг кампании")
class ConfigValidationTest {

    private static final List<SpawnRule> VALID_RULES = List.of(rule("1", "0.85"), rule("2", "0.15"));

    @Test
    @DisplayName("Валидный конфиг принимается")
    void validConfigIsAccepted() {
        CampaignConfig config = ConfigLoader.load(Path.of("config", "campaign.example.json"));

        assertThat(config.boardSize()).isEqualTo(4);
        assertThat(config.movesLimit()).isEqualTo(100);
        assertThat(config.spawnRules()).isEqualTo(VALID_RULES);
    }

    @Test
    @DisplayName("Сумма вероятностей spawn-правил не равна 1 — конфиг отклоняется")
    void probabilitiesNotSummingToOneAreRejected() {
        List<SpawnRule> rules = List.of(rule("1", "0.85"), rule("2", "0.10"));

        assertThatThrownBy(() -> new CampaignConfig(4, 100, rules))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Сумма вероятностей должна быть 1, сейчас 0.95");
    }

    @Test
    @DisplayName("movesLimit <= 0 — конфиг отклоняется")
    void nonPositiveMovesLimitIsRejected() {
        assertThatThrownBy(() -> new CampaignConfig(4, 0, VALID_RULES))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("movesLimit");
        assertThatThrownBy(() -> new CampaignConfig(4, -1, VALID_RULES))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("movesLimit");
    }

    @Test
    @DisplayName("Пустой список spawn-правил — конфиг отклоняется")
    void emptySpawnRulesAreRejected() {
        assertThatThrownBy(() -> new CampaignConfig(4, 100, List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("spawnRules");
    }

    @Test
    @DisplayName("Сумма 1.00 с нулями после точки принимается")
    void sumWithTrailingZerosIsAccepted() {
        CampaignConfig config = new CampaignConfig(4, 100, List.of(rule("1", "0.50"), rule("2", "0.50")));

        assertThat(config.spawnRules()).hasSize(2);
    }

    @Test
    @DisplayName("Нулевая вероятность отклоняется")
    void zeroProbabilityIsRejected() {
        assertThatThrownBy(() -> rule("1", "0"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("больше нуля");
    }

    @Test
    @DisplayName("Повтор уровня в правилах отклоняется")
    void repeatedLevelIsRejected() {
        List<SpawnRule> rules = List.of(rule("1", "0.5"), rule("1", "0.5"));

        assertThatThrownBy(() -> new CampaignConfig(4, 100, rules))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Уровень 1 встречается дважды");
    }

    @Test
    @DisplayName("Поле не 4 на 4 отклоняется")
    void otherBoardSizeIsRejected() {
        assertThatThrownBy(() -> new CampaignConfig(5, 100, VALID_RULES))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("boardSize = 5");
    }

    @Test
    @DisplayName("Неизвестный уровень в файле отклоняется с понятным текстом")
    void unknownLevelInFileIsRejected(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("campaign.json");
        Files.writeString(file, """
                {
                  "boardSize": 4,
                  "movesLimit": 100,
                  "spawnRules": [{ "cashback": "3", "probability": "1" }]
                }
                """);

        assertThatThrownBy(() -> ConfigLoader.load(file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageEndingWith("не загружен: Неизвестный уровень кэшбэка: 3. Допустимы 1, 2, 4, 8, 16, 32");
    }

    private static SpawnRule rule(String cashback, String probability) {
        return new SpawnRule(Cashback.fromPercent(cashback), new BigDecimal(probability));
    }
}
