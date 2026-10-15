package academy.cashbackmerge;

import academy.cashbackmerge.config.CampaignConfig;
import academy.cashbackmerge.config.ConfigLoader;
import academy.cashbackmerge.game.Direction;
import academy.cashbackmerge.game.GameState;
import academy.cashbackmerge.game.MoveEngine;
import academy.cashbackmerge.game.MoveResult;
import academy.cashbackmerge.game.TileSpawner;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/** Команда play: партия в консоли. Игрок вводит направления, программа показывает поле после каждого хода. */
@Command(name = "play", description = "Сыграть партию в консоли.")
public final class PlayCommand implements Callable<Integer> {

    @Option(names = "--config", description = "Путь к конфигу кампании. По умолчанию ${DEFAULT-VALUE}.")
    private Path configPath = Main.DEFAULT_CONFIG;

    @Option(names = "--seed", description = "Seed. Если не указан, выбирается случайно и печатается.")
    private long seed = new Random().nextInt(Integer.MAX_VALUE);

    @Override
    public Integer call() {
        CampaignConfig config = ConfigLoader.load(configPath);
        TileSpawner spawner = new TileSpawner(config.spawnRules(), new Random(seed));
        MoveEngine engine = new MoveEngine(spawner);
        GameState state = GameState.start(config.movesLimit(), spawner);
        Scanner input = new Scanner(System.in, StandardCharsets.UTF_8);

        System.out.println("Seed: " + seed);
        System.out.print(BoardRenderer.render(state));
        while (!state.isOver()) {
            System.out.println("Ход: w вверх, s вниз, a влево, d вправо, q выход.");
            if (!input.hasNextLine()) {
                break;
            }
            // PowerShell 5.1 и Блокнот ставят невидимый символ BOM в начало ввода.
            String text = input.nextLine().replace("\uFEFF", "").trim();
            if ("q".equals(text)) {
                break;
            }
            Optional<Direction> direction = parseDirection(text);
            if (direction.isEmpty()) {
                System.out.println("Не понял ход. Нужна английская буква w, a, s или d.");
                continue;
            }
            MoveResult result = engine.applyMove(state, direction.get());
            if (!result.boardChanged()) {
                System.out.println("Плитки не сдвинулись. Ход не потрачен.");
            }
            state = result.state();
            System.out.print(BoardRenderer.render(state));
        }
        System.out.println(state.isOver() ? "Партия окончена." : "Партия прервана.");
        System.out.println("Итоговый счёт: " + state.score());
        return 0;
    }

    private static Optional<Direction> parseDirection(String text) {
        return switch (text) {
            case "w" -> Optional.of(Direction.UP);
            case "s" -> Optional.of(Direction.DOWN);
            case "a" -> Optional.of(Direction.LEFT);
            case "d" -> Optional.of(Direction.RIGHT);
            default -> Optional.empty();
        };
    }
}
