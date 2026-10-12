package academy.cashbackmerge;

import academy.cashbackmerge.config.CampaignConfig;
import academy.cashbackmerge.config.ConfigLoader;
import academy.cashbackmerge.game.GameState;
import academy.cashbackmerge.game.TileSpawner;
import java.nio.file.Path;
import java.util.Random;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/** Команда init: создаёт начальное состояние по конфигу и seed и печатает его. */
@Command(name = "init", description = "Создать начальное поле по конфигу и seed и вывести его.")
public final class InitCommand implements Callable<Integer> {

    @Option(names = "--config", description = "Путь к конфигу кампании. По умолчанию ${DEFAULT-VALUE}.")
    private Path configPath = Path.of("config", "campaign.example.json");

    @Option(names = "--seed", required = true, description = "Seed. Одинаковый seed даёт одинаковое поле.")
    private long seed;

    @Override
    public Integer call() {
        CampaignConfig config = ConfigLoader.load(configPath);
        TileSpawner spawner = new TileSpawner(config.spawnRules(), new Random(seed));
        GameState state = GameState.start(config.movesLimit(), spawner);
        System.out.println("Seed: " + seed);
        System.out.print(BoardRenderer.render(state));
        return 0;
    }
}
