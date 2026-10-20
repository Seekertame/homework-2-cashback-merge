package academy.cashbackmerge;

import academy.cashbackmerge.config.CampaignConfig;
import academy.cashbackmerge.config.ConfigLoader;
import academy.cashbackmerge.simulation.GreedyStrategy;
import academy.cashbackmerge.simulation.RandomStrategy;
import academy.cashbackmerge.simulation.Simulation;
import academy.cashbackmerge.simulation.SimulationReport;
import academy.cashbackmerge.simulation.SimulationResult;
import academy.cashbackmerge.simulation.Strategy;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import org.jspecify.annotations.Nullable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/** Запускает много партий и пишет отчёт. Без --output отчёт печатается в консоль. */
@Command(name = "simulate", description = "Симуляция кампании и отчёт в Markdown.")
public final class SimulateCommand implements Callable<Integer> {

    @Option(names = "--config", description = "Путь к конфигу кампании. По умолчанию ${DEFAULT-VALUE}.")
    private Path configPath = Main.DEFAULT_CONFIG;

    @Option(names = "--players", description = "Число игроков. По умолчанию ${DEFAULT-VALUE}.")
    private int players = Main.DEFAULT_PLAYERS;

    @Option(names = "--strategy", description = "Стратегия игрока, random или greedy. По умолчанию ${DEFAULT-VALUE}.")
    private String strategyName = "random";

    @Option(names = "--seed", description = "Seed. Если не указан, выбирается случайно и пишется в отчёт.")
    private long seed = new Random().nextInt(Integer.MAX_VALUE);

    @Option(names = "--output", description = "Файл для отчёта, например report.md.")
    private @Nullable Path output;

    @Override
    public Integer call() {
        Strategy strategy = strategyByName(strategyName);
        CampaignConfig config = ConfigLoader.load(configPath);
        SimulationResult result = Simulation.run(config, players, seed, strategy);
        String report = SimulationReport.toMarkdown(configPath, config, seed, strategy, result);
        if (output == null) {
            System.out.print(report);
            return 0;
        }
        try {
            Files.writeString(output, report, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalArgumentException("Не удалось записать отчёт в " + output, e);
        }
        System.out.println(
                "Отчёт записан в " + output + ". Статус " + result.status(config.maxRewardAllowedRate()) + ".");
        return 0;
    }

    private static Strategy strategyByName(String name) {
        for (Strategy strategy : List.of(new RandomStrategy(), new GreedyStrategy())) {
            if (strategy.name().equals(name)) {
                return strategy;
            }
        }
        throw new IllegalArgumentException("Неизвестная стратегия " + name + ". Есть random и greedy.");
    }
}
