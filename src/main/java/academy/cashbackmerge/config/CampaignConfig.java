package academy.cashbackmerge.config;

import academy.cashbackmerge.game.Cashback;
import academy.cashbackmerge.game.GameRules;
import academy.cashbackmerge.game.SpawnRule;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Настройки кампании. Невалидный конфиг создать нельзя, конструктор бросит исключение. */
public record CampaignConfig(int boardSize, int movesLimit, List<SpawnRule> spawnRules) {

    public CampaignConfig {
        if (boardSize != GameRules.BOARD_SIZE) {
            throw new IllegalArgumentException(
                    "Поддерживается только поле " + GameRules.BOARD_SIZE + ", в конфиге boardSize = " + boardSize);
        }
        if (movesLimit <= 0) {
            throw new IllegalArgumentException("movesLimit должен быть больше нуля, сейчас " + movesLimit);
        }
        checkSpawnRules(spawnRules);
        spawnRules = List.copyOf(spawnRules);
    }

    private static void checkSpawnRules(List<SpawnRule> spawnRules) {
        if (spawnRules == null || spawnRules.isEmpty()) {
            throw new IllegalArgumentException("Список spawnRules пуст");
        }
        Set<Cashback> levels = new HashSet<>();
        BigDecimal sum = BigDecimal.ZERO;
        for (SpawnRule rule : spawnRules) {
            if (!levels.add(rule.cashback())) {
                throw new IllegalArgumentException("Уровень " + rule.cashback().percent() + " встречается дважды");
            }
            sum = sum.add(rule.probability());
        }
        if (sum.compareTo(BigDecimal.ONE) != 0) {
            throw new IllegalArgumentException("Сумма вероятностей должна быть 1, сейчас " + sum);
        }
    }
}
