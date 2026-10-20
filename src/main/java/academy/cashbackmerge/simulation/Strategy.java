package academy.cashbackmerge.simulation;

import academy.cashbackmerge.game.Direction;
import academy.cashbackmerge.game.GameState;
import java.util.Random;

/** Как виртуальный игрок выбирает ход. Random передаёт симуляция, чтобы вся случайность шла от одного seed. */
public interface Strategy {

    String name();

    Direction chooseMove(GameState state, Random random);
}
