package academy.cashbackmerge.simulation;

import academy.cashbackmerge.game.Direction;
import academy.cashbackmerge.game.GameState;
import academy.cashbackmerge.game.SlideResult;
import academy.cashbackmerge.game.Slider;
import java.util.Random;

/**
 * Пробует все ходы и выбирает тот, что даёт больше очков. Ход, который не меняет поле, не выбирается никогда. При
 * равных очках берётся первый по порядку. Случайность этой стратегии не нужна.
 */
public final class GreedyStrategy implements Strategy {

    @Override
    public String name() {
        return "greedy";
    }

    @Override
    public Direction chooseMove(GameState state, Random random) {
        Direction best = Direction.UP;
        int bestScore = -1;
        for (Direction direction : Direction.values()) {
            SlideResult slide = Slider.slide(state.board(), direction);
            boolean changes = !slide.board().equals(state.board());
            if (changes && slide.score() > bestScore) {
                best = direction;
                bestScore = slide.score();
            }
        }
        return best;
    }
}
