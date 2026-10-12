package academy.cashbackmerge.game;

import java.math.BigDecimal;

/** Правило появления плитки: с какой вероятностью новая плитка будет этого уровня. */
public record SpawnRule(Cashback cashback, BigDecimal probability) {

    public SpawnRule {
        if (cashback == null || probability == null) {
            throw new IllegalArgumentException("В правиле появления нужны cashback и probability");
        }
        if (probability.signum() <= 0) {
            throw new IllegalArgumentException("Вероятность должна быть больше нуля, сейчас " + probability);
        }
    }
}
