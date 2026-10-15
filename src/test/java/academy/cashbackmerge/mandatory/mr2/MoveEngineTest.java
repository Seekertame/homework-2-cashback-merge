package academy.cashbackmerge.mandatory.mr2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;

import academy.cashbackmerge.game.Board;
import academy.cashbackmerge.game.Cashback;
import academy.cashbackmerge.game.Direction;
import academy.cashbackmerge.game.GameState;
import academy.cashbackmerge.game.MoveEngine;
import academy.cashbackmerge.game.MoveResult;
import academy.cashbackmerge.game.Position;
import academy.cashbackmerge.game.SpawnRule;
import academy.cashbackmerge.game.TileSpawner;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Обязательные тесты: движок ходов — что считается ходом и что после него происходит. */
@DisplayName("MR2. Движок ходов")
class MoveEngineTest {

    private static final List<SpawnRule> ONLY_ONE = List.of(new SpawnRule(Cashback.ONE, BigDecimal.ONE));

    private final TileSpawner spawner = new TileSpawner(ONLY_ONE, new Random(1));
    private final MoveEngine engine = new MoveEngine(spawner);

    @Test
    @DisplayName("Невалидный ход не меняет поле, не тратит ход и не создаёт плитку")
    void invalidMoveChangesNothing() {
        GameState state = new GameState(new Board(Map.of(new Position(0, 0), Cashback.ONE)), 0, 10);

        MoveResult result = engine.applyMove(state, Direction.LEFT);

        assertThat(result.boardChanged()).isFalse();
        assertThat(result.state()).isEqualTo(state);
    }

    @Test
    @DisplayName("После валидного хода появляется ровно одна новая плитка")
    void validMoveSpawnsExactlyOneTile() {
        GameState state = new GameState(new Board(Map.of(new Position(0, 3), Cashback.ONE)), 0, 10);

        MoveResult result = engine.applyMove(state, Direction.LEFT);

        assertThat(result.boardChanged()).isTrue();
        assertThat(result.state().board().tiles()).hasSize(2);
        assertThat(result.state().board().tileAt(new Position(0, 0))).contains(Cashback.ONE);
        assertThat(result.state().movesLeft()).isEqualTo(9);
    }

    @Test
    @DisplayName("После N валидных ходов игра завершена")
    void gameEndsAfterMovesLimit() {
        GameState state = GameState.start(3, spawner);
        int changingMoves = 0;

        while (!state.isOver()) {
            state = firstChangingMove(state).state();
            changingMoves++;
        }

        assertThat(changingMoves).isEqualTo(3);
        assertThat(state.movesLeft()).isZero();
        GameState finished = state;
        assertThatThrownBy(() -> engine.applyMove(finished, Direction.LEFT)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Достижение плитки 32% не заканчивает игру — доигрываем до лимита ходов")
    void reachingMaxTileDoesNotEndGame() {
        Board board = new Board(Map.of(new Position(0, 0), Cashback.SIXTEEN, new Position(0, 1), Cashback.SIXTEEN));

        GameState after =
                engine.applyMove(new GameState(board, 0, 5), Direction.LEFT).state();

        assertThat(after.board().tileAt(new Position(0, 0))).contains(Cashback.THIRTY_TWO);
        assertThat(after.movesLeft()).isEqualTo(4);
        assertThat(after.isOver()).isFalse();
    }

    @Test
    @DisplayName("Score считается по правилам и растёт только при слияниях")
    void scoreIsCalculatedOnMerges() {
        Board pairs = new Board(Map.of(
                new Position(0, 0), Cashback.ONE,
                new Position(0, 1), Cashback.ONE,
                new Position(1, 0), Cashback.TWO,
                new Position(1, 1), Cashback.TWO));
        Board single = new Board(Map.of(new Position(0, 3), Cashback.ONE));

        GameState merged =
                engine.applyMove(new GameState(pairs, 10, 5), Direction.LEFT).state();
        GameState moved =
                engine.applyMove(new GameState(single, 10, 5), Direction.LEFT).state();

        assertThat(merged.score()).isEqualTo(10 + 2 + 4);
        assertThat(moved.score()).isEqualTo(10);
    }

    @Test
    @DisplayName("Игра заканчивается раньше, если ни один ход не меняет поле")
    void gameEndsWhenNoMoveChangesBoard() {
        Map<Position, Cashback> tiles = new HashMap<>();
        for (int row = 0; row < 4; row++) {
            for (int column = 0; column < 4; column++) {
                tiles.put(new Position(row, column), (row + column) % 2 == 0 ? Cashback.ONE : Cashback.TWO);
            }
        }

        GameState state = new GameState(new Board(tiles), 0, 50);

        assertThat(state.isOver()).isTrue();
    }

    private MoveResult firstChangingMove(GameState state) {
        for (Direction direction : Direction.values()) {
            MoveResult result = engine.applyMove(state, direction);
            if (result.boardChanged()) {
                return result;
            }
        }
        return fail("Ни один ход не меняет поле");
    }
}
