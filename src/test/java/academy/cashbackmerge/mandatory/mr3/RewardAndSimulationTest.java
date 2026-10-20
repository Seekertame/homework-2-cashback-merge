package academy.cashbackmerge.mandatory.mr3;

import static org.assertj.core.api.Assertions.assertThat;

import academy.cashbackmerge.config.CampaignConfig;
import academy.cashbackmerge.config.ConfigLoader;
import academy.cashbackmerge.config.RewardTier;
import academy.cashbackmerge.game.Board;
import academy.cashbackmerge.game.Cashback;
import academy.cashbackmerge.game.Position;
import academy.cashbackmerge.game.TileSpawner;
import academy.cashbackmerge.simulation.Simulation;
import academy.cashbackmerge.simulation.SimulationReport;
import academy.cashbackmerge.simulation.SimulationResult;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Обязательные тесты: награды, симуляция и отчёт. */
@DisplayName("MR3. Награды и симуляция")
class RewardAndSimulationTest {

    private static final Path CONFIG_PATH = Path.of("config", "campaign.example.json");
    private static final CampaignConfig CONFIG = ConfigLoader.load(CONFIG_PATH);

    @Test
    @DisplayName("Максимальная плитка 16% даёт награду соответствующего tier")
    void rewardMatchesHighestTile() {
        Board board = new Board(Map.of(
                new Position(0, 0), Cashback.ONE,
                new Position(1, 1), Cashback.SIXTEEN,
                new Position(2, 2), Cashback.FOUR));

        assertThat(board.maxTile()).isEqualTo(Cashback.SIXTEEN);
        assertThat(CONFIG.rewardFor(board.maxTile())).isEqualByComparingTo("500");
    }

    @Test
    @DisplayName("Weighted random учитывает вероятности из конфига")
    void spawnRespectsConfiguredProbabilities() {
        TileSpawner spawner = new TileSpawner(CONFIG.spawnRules(), new Random(7));
        int tiles = 10_000;
        int ones = 0;

        for (int i = 0; i < tiles; i++) {
            if (spawner.spawn(Board.empty()).tiles().containsValue(Cashback.ONE)) {
                ones++;
            }
        }

        // Ожидаем 0.85. Допуск 0.02 покрывает случайный разброс на 10000 плитках.
        assertThat((double) ones / tiles).isBetween(0.83, 0.87);
    }

    @Test
    @DisplayName("Отчёт содержит распределение максимальных плиток и наград")
    void reportContainsDistributions() {
        String report = report(42);

        assertThat(report)
                .contains("## Распределение максимальных плиток и наград")
                .contains("| Максимальная плитка | Награда | Игроков | Доля |")
                .contains("| 16% | 500 |", "| 32% | 2000 |");
    }

    @Test
    @DisplayName("Отчёт содержит долю игроков с максимальной наградой")
    void reportContainsMaxRewardRate() {
        assertThat(report(42)).contains("| Доля максимальной награды |", "| Игроков с максимальной наградой |");
    }

    @Test
    @DisplayName("Статус OK, если actualMaxRewardRate <= maxRewardAllowedRate, иначе FAILED")
    void reportStatusReflectsBusinessConstraint() {
        BigDecimal allowed = new BigDecimal("0.20");

        assertThat(result(100, 20).status(allowed)).isEqualTo("OK");
        assertThat(result(100, 21).status(allowed)).isEqualTo("FAILED");
        assertThat(result(10_000, 2001).status(allowed)).isEqualTo("FAILED");
        assertThat(report(42)).containsPattern("\\| Статус \\| (OK|FAILED) \\|");
    }

    @Test
    @DisplayName("Плитка между уровнями таблицы получает награду ближайшего уровня ниже")
    void tileBetweenTiersGetsLowerTier() {
        List<RewardTier> table = List.of(
                new RewardTier(Cashback.FOUR, new BigDecimal("50")),
                new RewardTier(Cashback.THIRTY_TWO, new BigDecimal("2000")));
        CampaignConfig config =
                new CampaignConfig(4, 100, CONFIG.spawnRules(), table, Cashback.THIRTY_TWO, new BigDecimal("0.20"));

        assertThat(config.rewardFor(Cashback.SIXTEEN)).isEqualByComparingTo("50");
        assertThat(config.rewardFor(Cashback.TWO)).isEqualByComparingTo("0");
    }

    @Test
    @DisplayName("Каждый игрок попадает ровно в одну строку распределения")
    void everyPlayerIsCountedOnce() {
        SimulationResult result = Simulation.run(CONFIG, 300, 42);

        int counted = 0;
        for (Cashback level : Cashback.values()) {
            counted += result.countOf(level);
        }

        assertThat(counted).isEqualTo(300);
        assertThat(result.maxRewardPlayers()).isEqualTo(result.countOf(Cashback.THIRTY_TWO));
    }

    private static String report(long seed) {
        SimulationResult result = Simulation.run(CONFIG, 200, seed);
        return SimulationReport.toMarkdown(CONFIG_PATH, CONFIG, seed, result);
    }

    private static SimulationResult result(int players, int maxRewardPlayers) {
        return new SimulationResult(players, Map.of(), 0, 0, maxRewardPlayers);
    }
}
