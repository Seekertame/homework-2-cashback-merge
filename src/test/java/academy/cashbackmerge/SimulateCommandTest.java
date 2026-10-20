package academy.cashbackmerge;

import static org.assertj.core.api.Assertions.assertThat;

import academy.cashbackmerge.support.CliRunner;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@DisplayName("Команда simulate")
class SimulateCommandTest {

    @Test
    @DisplayName("Отчёт записывается в файл из --output")
    void reportIsWrittenToFile(@TempDir Path dir) throws IOException {
        Path report = dir.resolve("report.md");

        CliRunner.Result result = CliRunner.run(
                "simulate", "--players", "100", "--strategy", "random", "--seed", "42", "--output", report.toString());

        assertThat(result.exitCode()).as(result.toString()).isZero();
        assertThat(result.stdout()).contains("Отчёт записан в", "Статус");
        assertThat(Files.readString(report, StandardCharsets.UTF_8))
                .startsWith("# Отчёт симуляции Cashback Merge")
                .contains("| Игроков | 100 |", "| Seed | 42 |", "| Стратегия | random |");
    }

    @Test
    @DisplayName("Без --output отчёт печатается в консоль")
    void reportIsPrintedWithoutOutput() {
        CliRunner.Result result = CliRunner.run("simulate", "--players", "50", "--seed", "1");

        assertThat(result.exitCode()).as(result.toString()).isZero();
        assertThat(result.stdout()).contains("| Игроков | 50 |", "| Статус |");
    }

    @Test
    @DisplayName("Стратегия greedy выбирается опцией и пишется в отчёт")
    void greedyStrategyIsUsed() {
        CliRunner.Result result = CliRunner.run("simulate", "--players", "50", "--strategy", "greedy", "--seed", "1");

        assertThat(result.exitCode()).as(result.toString()).isZero();
        assertThat(result.stdout()).contains("| Стратегия | greedy |");
    }

    @Test
    @DisplayName("Неизвестная стратегия отклоняется понятным текстом")
    void unknownStrategyIsRejected() {
        CliRunner.Result result = CliRunner.run("simulate", "--players", "10", "--strategy", "smart");

        assertThat(result.exitCode()).isEqualTo(1);
        assertThat(result.stderr()).contains("Неизвестная стратегия smart");
    }

    @Test
    @DisplayName("Число игроков ноль отклоняется")
    void zeroPlayersIsRejected() {
        CliRunner.Result result = CliRunner.run("simulate", "--players", "0");

        assertThat(result.exitCode()).isEqualTo(1);
        assertThat(result.stderr()).contains("Число игроков должно быть больше нуля");
    }
}
