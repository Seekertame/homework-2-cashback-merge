package academy.cashbackmerge.game;

import java.util.List;
import java.util.Random;

/** Ставит новую плитку в случайную пустую клетку. Уровень плитки выбирается по вероятностям из правил. */
public final class TileSpawner {

    private final List<SpawnRule> rules;
    private final Random random;
    private final int scale;
    private final long totalWeight;

    public TileSpawner(List<SpawnRule> rules, Random random) {
        this.rules = List.copyOf(rules);
        this.random = random;
        int maxScale = 0;
        for (SpawnRule rule : rules) {
            maxScale = Math.max(maxScale, rule.probability().scale());
        }
        this.scale = maxScale;
        long total = 0;
        for (SpawnRule rule : rules) {
            total += weight(rule);
        }
        this.totalWeight = total;
    }

    /** Новое поле, на котором в одной случайной пустой клетке появилась плитка. */
    public Board spawn(Board board) {
        List<Position> empty = board.emptyPositions();
        if (empty.isEmpty()) {
            throw new IllegalStateException("На поле нет пустых клеток");
        }
        Position position = empty.get(random.nextInt(empty.size()));
        return board.withTile(position, randomCashback());
    }

    /**
     * Выбирает уровень без double. Для вероятностей 0.85 и 0.15 веса равны 85 и 15. Случайное число от 0 до 99 меньше
     * 85 даёт первое правило. Остальные 15 чисел дают второе.
     */
    private Cashback randomCashback() {
        long roll = random.nextLong(totalWeight);
        for (SpawnRule rule : rules) {
            long weight = weight(rule);
            if (roll < weight) {
                return rule.cashback();
            }
            roll -= weight;
        }
        throw new IllegalStateException("Случайное число вышло за сумму весов");
    }

    /** Вероятность как целое число. При scale 2 число 0.85 превращается в 85. */
    private long weight(SpawnRule rule) {
        return rule.probability().movePointRight(scale).longValueExact();
    }
}
