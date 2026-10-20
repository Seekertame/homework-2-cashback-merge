package academy.cashbackmerge.simulation;

import academy.cashbackmerge.config.CampaignConfig;
import academy.cashbackmerge.game.Cashback;
import academy.cashbackmerge.game.SpawnRule;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Отчёт симуляции в Markdown. */
public final class SimulationReport {

    private SimulationReport() {}

    public static String toMarkdown(
            Path configPath, CampaignConfig config, long seed, Strategy strategy, SimulationResult result) {
        return "# Отчёт симуляции Cashback Merge\n\n"
                + "Оценочный результат одного запуска с этими параметрами.\n\n"
                + parameters(configPath, config, seed, strategy, result)
                + distribution(config, result)
                + summary(config, result);
    }

    private static String parameters(
            Path configPath, CampaignConfig config, long seed, Strategy strategy, SimulationResult result) {
        List<String> spawnRules = new ArrayList<>();
        for (SpawnRule rule : config.spawnRules()) {
            spawnRules.add(rule.cashback().percent() + "% с вероятностью "
                    + rule.probability().toPlainString());
        }
        return "## Параметры\n\n"
                + "| Параметр | Значение |\n"
                + "|---|---|\n"
                + row("Конфиг", configPath)
                + row("Игроков", result.players())
                + row("Стратегия", strategy.name())
                + row("Seed", seed)
                + row("Лимит ходов", config.movesLimit())
                + row("Правила появления", String.join(", ", spawnRules))
                + row(
                        "Максимальная награда",
                        "плитка " + config.maxRewardCashback().percent() + "%")
                + row(
                        "Допустимая доля максимальной награды",
                        config.maxRewardAllowedRate().toPlainString())
                + "\n";
    }

    private static String distribution(CampaignConfig config, SimulationResult result) {
        StringBuilder text = new StringBuilder();
        text.append("## Распределение максимальных плиток и наград\n\n");
        text.append("| Максимальная плитка | Награда | Игроков | Доля |\n");
        text.append("|---|---|---|---|\n");
        for (Cashback level : Cashback.values()) {
            int count = result.countOf(level);
            text.append("| %d%% | %s | %d | %s |\n"
                    .formatted(
                            level.percent(),
                            config.rewardFor(level).toPlainString(),
                            count,
                            result.shareOf(count).toPlainString()));
        }
        return text.append('\n').toString();
    }

    private static String summary(CampaignConfig config, SimulationResult result) {
        return "## Итог\n\n"
                + "| Показатель | Значение |\n"
                + "|---|---|\n"
                + row("Средний score", result.averageScore().toPlainString())
                + row("Максимальный score", result.maxScore())
                + row("Игроков с максимальной наградой", result.maxRewardPlayers())
                + row("Доля максимальной награды", result.maxRewardRate().toPlainString())
                + row("Статус", result.status(config.maxRewardAllowedRate()));
    }

    private static String row(String name, Object value) {
        return "| " + name + " | " + value + " |\n";
    }
}
