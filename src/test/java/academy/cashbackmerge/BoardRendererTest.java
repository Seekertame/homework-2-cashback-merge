package academy.cashbackmerge;

import static org.assertj.core.api.Assertions.assertThat;

import academy.cashbackmerge.game.Board;
import academy.cashbackmerge.game.Cashback;
import academy.cashbackmerge.game.GameState;
import academy.cashbackmerge.game.Position;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Текстовый вывод")
class BoardRendererTest {

    @Test
    @DisplayName("Поле, счёт и оставшиеся ходы выводятся текстом")
    void stateIsRenderedAsText() {
        Board board = Board.empty()
                .withTile(new Position(0, 0), Cashback.ONE)
                .withTile(new Position(3, 3), Cashback.THIRTY_TWO);
        GameState state = new GameState(board, 12, 7);

        String expected = """
                  1%   .   .   .
                   .   .   .   .
                   .   .   .   .
                   .   .   . 32%
                Счёт: 12
                Осталось ходов: 7
                """;
        assertThat(BoardRenderer.render(state)).isEqualTo(expected);
    }
}
