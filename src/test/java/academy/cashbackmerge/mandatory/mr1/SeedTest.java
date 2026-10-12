package academy.cashbackmerge.mandatory.mr1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import academy.cashbackmerge.config.CampaignConfig;
import academy.cashbackmerge.config.ConfigLoader;
import academy.cashbackmerge.game.GameState;
import academy.cashbackmerge.game.TileSpawner;
import java.nio.file.Path;
import java.util.Random;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Обязательные тесты: воспроизводимость по seed. */
@DisplayName("MR1. Воспроизводимость по seed")
class SeedTest {

    private static final CampaignConfig CONFIG = ConfigLoader.load(Path.of("config", "campaign.example.json"));

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
    @Disabled("MR3: реализуй тест и удали эту строку")
    @DisplayName("Одинаковый конфиг, seed и стратегия дают одинаковый отчёт симуляции")
    void sameInputsProduceSameSimulationReport() {
        fail("Тест не реализован");
    }

    private static GameState start(long seed) {
        TileSpawner spawner = new TileSpawner(CONFIG.spawnRules(), new Random(seed));
        return GameState.start(CONFIG.movesLimit(), spawner);
    }
}
