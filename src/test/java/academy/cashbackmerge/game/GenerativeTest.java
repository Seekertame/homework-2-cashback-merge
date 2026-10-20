package academy.cashbackmerge.game;

import static org.assertj.core.api.Assertions.assertThat;

import academy.cashbackmerge.config.CampaignConfig;
import academy.cashbackmerge.config.ConfigLoader;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;

/** Каждый повтор строит своё случайное поле. Номер повтора служит seed, поэтому упавший случай легко повторить. */
@DisplayName("Генеративные тесты сдвига")
class GenerativeTest {

    private static final CampaignConfig CONFIG = ConfigLoader.load(Path.of("config", "campaign.example.json"));

    @RepeatedTest(300)
    @DisplayName("Сумма процентов на поле после сдвига не меняется")
    void slideKeepsSumOfTiles(RepetitionInfo info) {
        Board board = randomBoard(info.getCurrentRepetition());

        for (Direction direction : Direction.values()) {
            Board after = Slider.slide(board, direction).board();
            assertThat(sum(after)).as("Направление %s", direction).isEqualTo(sum(board));
        }
    }

    @RepeatedTest(300)
    @DisplayName("Плитка не сливается дважды, в линии остаётся не меньше половины плиток")
    void noTileMergesTwice(RepetitionInfo info) {
        Board board = randomBoard(info.getCurrentRepetition());

        for (Direction direction : Direction.values()) {
            Board after = Slider.slide(board, direction).board();
            for (int line = 0; line < 4; line++) {
                int before = tilesInLine(board, direction, line);
                // Плитка сливается не больше раза, поэтому в линии остаётся хотя бы половина плиток.
                assertThat(tilesInLine(after, direction, line))
                        .as("Направление %s, линия %d", direction, line)
                        .isGreaterThanOrEqualTo((before + 1) / 2);
            }
        }
    }

    @RepeatedTest(50)
    @DisplayName("Тот же seed даёт ту же партию до конца")
    void sameSeedGivesSameGame(RepetitionInfo info) {
        long seed = info.getCurrentRepetition();

        assertThat(playWholeGame(seed)).isEqualTo(playWholeGame(seed));
    }

    /** Примерно половина клеток занята плитками случайных уровней. */
    private static Board randomBoard(long seed) {
        Random random = new Random(seed);
        Cashback[] levels = Cashback.values();
        Map<Position, Cashback> tiles = new HashMap<>();
        for (int row = 0; row < 4; row++) {
            for (int column = 0; column < 4; column++) {
                if (random.nextBoolean()) {
                    tiles.put(new Position(row, column), levels[random.nextInt(levels.length)]);
                }
            }
        }
        return new Board(tiles);
    }

    private static int sum(Board board) {
        int sum = 0;
        for (Cashback tile : board.tiles().values()) {
            sum += tile.percent();
        }
        return sum;
    }

    /** Для LEFT и RIGHT линия это строка, для UP и DOWN это столбец. */
    private static int tilesInLine(Board board, Direction direction, int line) {
        boolean row = direction == Direction.LEFT || direction == Direction.RIGHT;
        int count = 0;
        for (int i = 0; i < 4; i++) {
            Position position = row ? new Position(line, i) : new Position(i, line);
            if (board.tileAt(position).isPresent()) {
                count++;
            }
        }
        return count;
    }

    private static GameState playWholeGame(long seed) {
        Random random = new Random(seed);
        TileSpawner spawner = new TileSpawner(CONFIG.spawnRules(), random);
        MoveEngine engine = new MoveEngine(spawner);
        Direction[] directions = Direction.values();
        GameState state = GameState.start(CONFIG.movesLimit(), spawner);
        while (!state.isOver()) {
            state = engine.applyMove(state, directions[random.nextInt(directions.length)])
                    .state();
        }
        return state;
    }
}
