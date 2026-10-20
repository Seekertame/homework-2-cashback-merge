package academy.cashbackmerge.simulation;

import academy.cashbackmerge.game.Cashback;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/** Итог симуляции. maxTileCounts хранит, сколько игроков закончили партию с такой максимальной плиткой. */
public record SimulationResult(
        int players, Map<Cashback, Integer> maxTileCounts, long totalScore, int maxScore, int maxRewardPlayers) {

    /** Знаков после точки у долей и среднего в отчёте. */
    private static final int SCALE = 4;

    public SimulationResult {
        maxTileCounts = Map.copyOf(maxTileCounts);
    }

    public int countOf(Cashback maxTile) {
        return maxTileCounts.getOrDefault(maxTile, 0);
    }

    /** Доля игроков от общего числа, округлённая для отчёта. */
    public BigDecimal shareOf(int count) {
        return BigDecimal.valueOf(count).divide(BigDecimal.valueOf(players), SCALE, RoundingMode.HALF_UP);
    }

    public BigDecimal averageScore() {
        return BigDecimal.valueOf(totalScore).divide(BigDecimal.valueOf(players), SCALE, RoundingMode.HALF_UP);
    }

    public BigDecimal maxRewardRate() {
        return shareOf(maxRewardPlayers);
    }

    /** OK, если доля максимальной награды не больше допустимой. Сравнение без округления, доля 0.20004 даёт FAILED. */
    public String status(BigDecimal allowedRate) {
        BigDecimal allowedPlayers = allowedRate.multiply(BigDecimal.valueOf(players));
        return BigDecimal.valueOf(maxRewardPlayers).compareTo(allowedPlayers) <= 0 ? "OK" : "FAILED";
    }
}
