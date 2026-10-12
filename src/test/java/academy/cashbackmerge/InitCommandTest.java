package academy.cashbackmerge;

import static org.assertj.core.api.Assertions.assertThat;

import academy.cashbackmerge.support.CliRunner;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@DisplayName("Команда init")
class InitCommandTest {

    @Test
    @DisplayName("Печатает seed, поле, счёт и оставшиеся ходы")
    void initPrintsInitialState() {
        CliRunner.Result result = CliRunner.run("init", "--seed", "42");

        assertThat(result.exitCode()).as(result.toString()).isZero();
        assertThat(result.stdout()).contains("Seed: 42", "Счёт: 0", "Осталось ходов: 100");
    }

    @Test
    @DisplayName("Ошибка в конфиге печатается понятным текстом с кодом 1")
    void badConfigGivesReadableError(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("campaign.json");
        Files.writeString(file, """
                { "boardSize": 4, "movesLimit": 0, "spawnRules": [{ "cashback": "1", "probability": "1" }] }
                """);

        CliRunner.Result result = CliRunner.run("init", "--config", file.toString(), "--seed", "1");

        assertThat(result.exitCode()).as(result.toString()).isEqualTo(1);
        assertThat(result.stderr())
                .contains("movesLimit должен быть больше нуля")
                .doesNotContain("Exception");
    }
}
