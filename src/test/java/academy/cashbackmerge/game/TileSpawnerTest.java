package academy.cashbackmerge.game;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Появление плиток")
class TileSpawnerTest {

    private static final List<SpawnRule> ONLY_TWO = List.of(new SpawnRule(Cashback.TWO, BigDecimal.ONE));

    @Test
    @DisplayName("Появляется ровно одна плитка, старые остаются на месте")
    void spawnAddsExactlyOneTile() {
        Board before = Board.empty().withTile(new Position(0, 0), Cashback.ONE);

        Board after = new TileSpawner(ONLY_TWO, new Random(1)).spawn(before);

        assertThat(after.tiles()).hasSize(2);
        assertThat(after.tileAt(new Position(0, 0))).contains(Cashback.ONE);
    }

    @Test
    @DisplayName("Правило с вероятностью 1 всегда даёт свой уровень")
    void ruleWithProbabilityOneAlwaysWins() {
        Board board = fullBoard();

        assertThat(board.tiles().values()).containsOnly(Cashback.TWO);
    }

    @Test
    @DisplayName("На заполненное поле плитку поставить нельзя")
    void fullBoardIsRejected() {
        TileSpawner spawner = new TileSpawner(ONLY_TWO, new Random(1));
        Board board = fullBoard();

        assertThatThrownBy(() -> spawner.spawn(board)).isInstanceOf(IllegalStateException.class);
    }

    private static Board fullBoard() {
        TileSpawner spawner = new TileSpawner(ONLY_TWO, new Random(1));
        Board board = Board.empty();
        for (int i = 0; i < 16; i++) {
            board = spawner.spawn(board);
        }
        return board;
    }
}
