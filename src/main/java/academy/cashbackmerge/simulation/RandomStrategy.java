package academy.cashbackmerge.simulation;

import academy.cashbackmerge.game.Direction;
import java.util.Random;

/** Стратегия random. Игрок выбирает направление наугад из четырёх. */
public final class RandomStrategy {

    public static final String NAME = "random";

    private final Random random;

    public RandomStrategy(Random random) {
        this.random = random;
    }

    public Direction chooseMove() {
        Direction[] directions = Direction.values();
        return directions[random.nextInt(directions.length)];
    }
}
