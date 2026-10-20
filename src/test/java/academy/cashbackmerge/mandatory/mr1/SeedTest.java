package academy.cashbackmerge.mandatory.mr1;

import static org.assertj.core.api.Assertions.assertThat;

import academy.cashbackmerge.config.CampaignConfig;
import academy.cashbackmerge.config.ConfigLoader;
import academy.cashbackmerge.game.GameState;
import academy.cashbackmerge.game.TileSpawner;
import academy.cashbackmerge.simulation.RandomStrategy;
import academy.cashbackmerge.simulation.Simulation;
import academy.cashbackmerge.simulation.SimulationReport;
import academy.cashbackmerge.simulation.SimulationResult;
import academy.cashbackmerge.simulation.Strategy;
import java.nio.file.Path;
import java.util.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Обязательные тесты: воспроизводимость по seed. */
@DisplayName("MR1. Воспроизводимость по seed")
class SeedTest {

    private static final Path CONFIG_PATH = Path.of("config", "campaign.example.json");
    private static final CampaignConfig CONFIG = ConfigLoader.load(CONFIG_PATH);
    private static final Strategy RANDOM = new RandomStrategy();

    @Test
    @DisplayName("Одинаковый конфиг и seed дают одинаковое начальное поле")
    void sameConfigAndSeedProduceSameInitialBoard() {
        GameState first = start(42);
        GameState second = start(42);

        assertThat(first).isEqualTo(second);
        assertThat(first.board().tiles()).hasSize(2);
    }

    @Test
    @DisplayName("Разный seed даёт разное начальное поле")
    void differentSeedsProduceDifferentBoards() {
        assertThat(start(42)).isNotEqualTo(start(43));
    }

    @Test
    @DisplayName("Одинаковый конфиг, seed и стратегия дают одинаковый отчёт симуляции")
    void sameInputsProduceSameSimulationReport() {
        String first = report(42);
        String second = report(42);

        assertThat(first).isEqualTo(second);
        assertThat(first).isNotEqualTo(report(43));
    }

    private static String report(long seed) {
        SimulationResult result = Simulation.run(CONFIG, 200, seed, RANDOM);
        return SimulationReport.toMarkdown(CONFIG_PATH, CONFIG, seed, RANDOM, result);
    }

    private static GameState start(long seed) {
        TileSpawner spawner = new TileSpawner(CONFIG.spawnRules(), new Random(seed));
        return GameState.start(CONFIG.movesLimit(), spawner);
    }
}
