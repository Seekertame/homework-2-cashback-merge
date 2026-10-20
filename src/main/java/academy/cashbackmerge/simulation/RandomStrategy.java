package academy.cashbackmerge.simulation;

import academy.cashbackmerge.game.Direction;
import academy.cashbackmerge.game.GameState;
import java.util.Random;

/** Выбирает направление наугад из четырёх. */
public final class RandomStrategy implements Strategy {

    @Override
    public String name() {
        return "random";
    }

    @Override
    public Direction chooseMove(GameState state, Random random) {
        Direction[] directions = Direction.values();
        return directions[random.nextInt(directions.length)];
    }
}
