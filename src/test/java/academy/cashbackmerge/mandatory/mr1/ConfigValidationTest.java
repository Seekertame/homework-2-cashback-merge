package academy.cashbackmerge.mandatory.mr1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import academy.cashbackmerge.config.CampaignConfig;
import academy.cashbackmerge.config.ConfigLoader;
import academy.cashbackmerge.config.RewardTier;
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
    private static final List<RewardTier> VALID_REWARDS = List.of(tier("16", "500"), tier("32", "2000"));
    private static final BigDecimal RATE = new BigDecimal("0.20");

    @Test
    @DisplayName("Валидный конфиг принимается")
    void validConfigIsAccepted() {
        CampaignConfig config = ConfigLoader.load(Path.of("config", "campaign.example.json"));

        assertThat(config.boardSize()).isEqualTo(4);
        assertThat(config.movesLimit()).isEqualTo(100);
        assertThat(config.spawnRules()).isEqualTo(VALID_RULES);
        assertThat(config.rewardTable()).hasSize(5);
        assertThat(config.maxRewardCashback()).isEqualTo(Cashback.THIRTY_TWO);
        assertThat(config.maxRewardAllowedRate()).isEqualByComparingTo("0.20");
    }

    @Test
    @DisplayName("Сумма вероятностей spawn-правил не равна 1 — конфиг отклоняется")
    void probabilitiesNotSummingToOneAreRejected() {
        List<SpawnRule> rules = List.of(rule("1", "0.85"), rule("2", "0.10"));

        assertThatThrownBy(() -> config(4, 100, rules))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Сумма вероятностей должна быть 1, сейчас 0.95");
    }

    @Test
    @DisplayName("movesLimit <= 0 — конфиг отклоняется")
    void nonPositiveMovesLimitIsRejected() {
        assertThatThrownBy(() -> config(4, 0, VALID_RULES))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("movesLimit");
        assertThatThrownBy(() -> config(4, -1, VALID_RULES))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("movesLimit");
    }

    @Test
    @DisplayName("Пустой список spawn-правил — конфиг отклоняется")
    void emptySpawnRulesAreRejected() {
        assertThatThrownBy(() -> config(4, 100, List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("spawnRules");
    }

    @Test
    @DisplayName("Сумма 1.00 с нулями после точки принимается")
    void sumWithTrailingZerosIsAccepted() {
        CampaignConfig config = config(4, 100, List.of(rule("1", "0.50"), rule("2", "0.50")));

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

        assertThatThrownBy(() -> config(4, 100, rules))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Уровень 1 встречается дважды");
    }

    @Test
    @DisplayName("Поле не 4 на 4 отклоняется")
    void otherBoardSizeIsRejected() {
        assertThatThrownBy(() -> config(5, 100, VALID_RULES))
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

    @Test
    @DisplayName("Пустая таблица наград отклоняется")
    void emptyRewardTableIsRejected() {
        assertThatThrownBy(() -> new CampaignConfig(4, 100, VALID_RULES, List.of(), Cashback.THIRTY_TWO, RATE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("rewardTable");
    }

    @Test
    @DisplayName("Уровень максимальной награды должен быть в таблице наград")
    void maxRewardCashbackMustBeInTable() {
        assertThatThrownBy(() -> new CampaignConfig(4, 100, VALID_RULES, VALID_REWARDS, Cashback.EIGHT, RATE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("maxRewardCashback = 8");
    }

    @Test
    @DisplayName("Допустимая доля больше 1 отклоняется")
    void allowedRateAboveOneIsRejected() {
        BigDecimal rate = new BigDecimal("1.5");

        assertThatThrownBy(() -> new CampaignConfig(4, 100, VALID_RULES, VALID_REWARDS, Cashback.THIRTY_TWO, rate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("maxRewardAllowedRate");
    }

    @Test
    @DisplayName("Отрицательная награда отклоняется")
    void negativeRewardIsRejected() {
        assertThatThrownBy(() -> tier("16", "-1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("отрицательной");
    }

    /** Конфиг с правильной таблицей наград. Тесты выше проверяют поле, лимит ходов и правила появления. */
    private static CampaignConfig config(int boardSize, int movesLimit, List<SpawnRule> rules) {
        return new CampaignConfig(boardSize, movesLimit, rules, VALID_REWARDS, Cashback.THIRTY_TWO, RATE);
    }

    private static RewardTier tier(String cashback, String reward) {
        return new RewardTier(Cashback.fromPercent(cashback), new BigDecimal(reward));
    }

    private static SpawnRule rule(String cashback, String probability) {
        return new SpawnRule(Cashback.fromPercent(cashback), new BigDecimal(probability));
    }
}
