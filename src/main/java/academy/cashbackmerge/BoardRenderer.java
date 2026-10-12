package academy.cashbackmerge;

import academy.cashbackmerge.game.GameRules;
import academy.cashbackmerge.game.GameState;
import academy.cashbackmerge.game.Position;

/** Текстовый вид партии для консоли. */
public final class BoardRenderer {

    private BoardRenderer() {}

    public static String render(GameState state) {
        StringBuilder text = new StringBuilder();
        for (int row = 0; row < GameRules.BOARD_SIZE; row++) {
            for (int column = 0; column < GameRules.BOARD_SIZE; column++) {
                String cell = state.board()
                        .tileAt(new Position(row, column))
                        .map(level -> level.percent() + "%")
                        .orElse(".");
                text.append("%4s".formatted(cell));
            }
            text.append('\n');
        }
        text.append("Счёт: ").append(state.score()).append('\n');
        text.append("Осталось ходов: ").append(state.movesLeft()).append('\n');
        return text.toString();
    }
}
