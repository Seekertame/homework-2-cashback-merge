package academy.cashbackmerge;

import static org.assertj.core.api.Assertions.assertThat;

import academy.cashbackmerge.support.CliRunner;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Команда play")
class PlayCommandTest {

    @Test
    @DisplayName("Ход вверх сливает две плитки 2% у верхнего края")
    void moveUpMergesTiles() {
        // При seed 42 на старте две плитки 2% стоят в правом столбце в двух нижних клетках.
        CliRunner.Result result = CliRunner.runWithInput("w\nq\n", "play", "--seed", "42");

        assertThat(result.exitCode()).as(result.toString()).isZero();
        assertThat(result.stdout())
                .contains("Seed: 42", "  4%", "Счёт: 4", "Осталось ходов: 99", "Партия прервана.", "Итоговый счёт: 4");
    }

    @Test
    @DisplayName("Ход без изменений не тратит ход")
    void moveWithoutChangeIsNotCounted() {
        // При seed 42 обе плитки уже у правого края, ход вправо ничего не меняет.
        CliRunner.Result result = CliRunner.runWithInput("d\nq\n", "play", "--seed", "42");

        assertThat(result.stdout())
                .contains("Плитки не сдвинулись. Ход не потрачен.")
                .doesNotContain("Осталось ходов: 99");
    }

    @Test
    @DisplayName("Символ BOM в начале ввода не мешает первому ходу")
    void byteOrderMarkIsIgnored() {
        CliRunner.Result result = CliRunner.runWithInput("﻿w\nq\n", "play", "--seed", "42");

        assertThat(result.stdout()).contains("Счёт: 4").doesNotContain("Не понял ход");
    }

    @Test
    @DisplayName("Непонятный ввод не ломает игру")
    void unknownInputIsReported() {
        CliRunner.Result result = CliRunner.runWithInput("ц\nq\n", "play", "--seed", "42");

        assertThat(result.exitCode()).isZero();
        assertThat(result.stdout()).contains("Не понял ход");
    }

    @Test
    @DisplayName("Конец ввода завершает игру без ошибки")
    void endOfInputEndsGame() {
        CliRunner.Result result = CliRunner.runWithInput("", "play", "--seed", "42");

        assertThat(result.exitCode()).as(result.toString()).isZero();
        assertThat(result.stdout()).contains("Итоговый счёт: 0");
    }
}
