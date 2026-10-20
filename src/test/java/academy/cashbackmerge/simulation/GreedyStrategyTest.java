package academy.cashbackmerge.simulation;

import static org.assertj.core.api.Assertions.assertThat;

import academy.cashbackmerge.config.CampaignConfig;
import academy.cashbackmerge.config.ConfigLoader;
import academy.cashbackmerge.game.Board;
import academy.cashbackmerge.game.Cashback;
import academy.cashbackmerge.game.Direction;
import academy.cashbackmerge.game.GameState;
import academy.cashbackmerge.game.Position;
import java.nio.file.Path;
import java.util.Map;
import java.util.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Стратегия greedy")
class GreedyStrategyTest {

    private final GreedyStrategy greedy = new GreedyStrategy();

    @Test
    @DisplayName("Выбирает ход со слиянием")
    void choosesMergingMove() {
        // Пара 1% стоит в столбце. Очки дают только ходы вверх и вниз, первым по порядку идёт вверх.
        Board board = new Board(Map.of(new Position(1, 0), Cashback.ONE, new Position(2, 0), Cashback.ONE));

        Direction move = greedy.chooseMove(new GameState(board, 0, 10), new Random(1));

        assertThat(move).isEqualTo(Direction.UP);
    }

    @Test
    @DisplayName("Не выбирает ход, который не меняет поле")
    void skipsMoveWithoutChange() {
        // Плитка в левом верхнем углу. Ходы вверх и влево ничего не меняют.
        Board board = new Board(Map.of(new Position(0, 0), Cashback.ONE));

        Direction move = greedy.chooseMove(new GameState(board, 0, 10), new Random(1));

        assertThat(move).isIn(Direction.DOWN, Direction.RIGHT);
    }

    @Test
    @DisplayName("На одинаковом seed набирает больше очков, чем random")
    void scoresMoreThanRandomOnSameSeed() {
        CampaignConfig config = ConfigLoader.load(Path.of("config", "campaign.example.json"));

        SimulationResult greedyResult = Simulation.run(config, 300, 42, greedy);
        SimulationResult randomResult = Simulation.run(config, 300, 42, new RandomStrategy());

        assertThat(greedyResult.averageScore()).isGreaterThan(randomResult.averageScore());
    }
}
