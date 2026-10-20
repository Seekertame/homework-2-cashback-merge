package academy.cashbackmerge.config;

import academy.cashbackmerge.game.Cashback;
import java.math.BigDecimal;

/** Строка таблицы наград. Уровень кэшбэка и награда за него. */
public record RewardTier(Cashback cashback, BigDecimal reward) {

    public RewardTier {
        if (cashback == null || reward == null) {
            throw new IllegalArgumentException("В таблице наград нужны cashback и reward");
        }
        if (reward.signum() < 0) {
            throw new IllegalArgumentException("Награда не может быть отрицательной, сейчас " + reward);
        }
    }
}
