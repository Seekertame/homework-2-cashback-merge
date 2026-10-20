package academy.cashbackmerge.config;

import academy.cashbackmerge.game.Cashback;
import academy.cashbackmerge.game.GameRules;
import academy.cashbackmerge.game.SpawnRule;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Настройки кампании. Невалидный конфиг создать нельзя, конструктор бросит исключение. */
public record CampaignConfig(
        int boardSize,
        int movesLimit,
        List<SpawnRule> spawnRules,
        List<RewardTier> rewardTable,
        Cashback maxRewardCashback,
        BigDecimal maxRewardAllowedRate) {

    public CampaignConfig {
        if (boardSize != GameRules.BOARD_SIZE) {
            throw new IllegalArgumentException(
                    "Поддерживается только поле " + GameRules.BOARD_SIZE + ", в конфиге boardSize = " + boardSize);
        }
        if (movesLimit <= 0) {
            throw new IllegalArgumentException("movesLimit должен быть больше нуля, сейчас " + movesLimit);
        }
        checkSpawnRules(spawnRules);
        checkRewardTable(rewardTable, maxRewardCashback);
        checkAllowedRate(maxRewardAllowedRate);
        spawnRules = List.copyOf(spawnRules);
        rewardTable = List.copyOf(rewardTable);
    }

    /** Награда за самый высокий уровень таблицы, которого достигла плитка. Ниже всех уровней награда 0. */
    public BigDecimal rewardFor(Cashback maxTile) {
        BigDecimal reward = BigDecimal.ZERO;
        int bestPercent = 0;
        for (RewardTier tier : rewardTable) {
            int percent = tier.cashback().percent();
            if (percent <= maxTile.percent() && percent > bestPercent) {
                bestPercent = percent;
                reward = tier.reward();
            }
        }
        return reward;
    }

    public boolean isMaxReward(Cashback maxTile) {
        return maxTile.percent() >= maxRewardCashback.percent();
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

    private static void checkRewardTable(List<RewardTier> rewardTable, Cashback maxRewardCashback) {
        if (rewardTable == null || rewardTable.isEmpty()) {
            throw new IllegalArgumentException("Таблица rewardTable пуста");
        }
        if (maxRewardCashback == null) {
            throw new IllegalArgumentException("В конфиге нет maxRewardCashback");
        }
        Set<Cashback> levels = new HashSet<>();
        for (RewardTier tier : rewardTable) {
            if (!levels.add(tier.cashback())) {
                throw new IllegalArgumentException(
                        "Уровень " + tier.cashback().percent() + " встречается в таблице наград дважды");
            }
        }
        if (!levels.contains(maxRewardCashback)) {
            throw new IllegalArgumentException(
                    "Уровня maxRewardCashback = " + maxRewardCashback.percent() + " нет в таблице наград");
        }
    }

    private static void checkAllowedRate(BigDecimal rate) {
        if (rate == null || rate.signum() < 0 || rate.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException("maxRewardAllowedRate должна быть от 0 до 1, сейчас " + rate);
        }
    }
}
