package academy.cashbackmerge;

import static org.assertj.core.api.Assertions.assertThat;

import academy.cashbackmerge.support.CliRunner;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@DisplayName("Команда play")
class PlayCommandTest {

    @Test
    @DisplayName("Повтор сохранённой партии проходит через те же поля и даёт тот же итог")
    void replayRepeatsSavedGame(@TempDir Path dir) throws IOException {
        Path game = dir.resolve("game.txt");

        CliRunner.Result played =
                CliRunner.runWithInput("w\na\ns\nd\nw\nq\n", "play", "--seed", "42", "--save", game.toString());
        CliRunner.Result replayed = CliRunner.run("play", "--replay", game.toString());

        assertThat(Files.readString(game, StandardCharsets.UTF_8)).isEqualTo("42\nwasdw\n");
        assertThat(replayed.exitCode()).as(replayed.toString()).isZero();
        assertThat(states(replayed.stdout())).isEqualTo(states(played.stdout()));
    }

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
    @DisplayName("Награда показывается по максимальной плитке")
    void currentRewardIsShown() {
        // До хода максимальная плитка 2%, награда 0. После хода вверх плитка 4%, награда 50.
        CliRunner.Result result = CliRunner.runWithInput("w\nq\n", "play", "--seed", "42");

        assertThat(result.stdout()).contains("Награда сейчас: 0", "Награда сейчас: 50", "Итоговая награда: 50");
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
        CliRunner.Result result = CliRunner.runWithInput("\uFEFFw\nq\n", "play", "--seed", "42");

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

    /** Строки поля, счёт, ходы и награды. Подсказки и эхо ввода сюда не входят. */
    private static List<String> states(String stdout) {
        return stdout.lines()
                .filter(line -> line.startsWith(" ")
                        || line.startsWith("Счёт")
                        || line.startsWith("Осталось")
                        || line.startsWith("Награда")
                        || line.startsWith("Итог"))
                .toList();
    }
}
