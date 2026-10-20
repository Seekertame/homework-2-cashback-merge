package academy.cashbackmerge;

import academy.cashbackmerge.config.CampaignConfig;
import academy.cashbackmerge.config.ConfigLoader;
import academy.cashbackmerge.game.Direction;
import academy.cashbackmerge.game.GameState;
import academy.cashbackmerge.game.MoveEngine;
import academy.cashbackmerge.game.MoveResult;
import academy.cashbackmerge.game.TileSpawner;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.Callable;
import org.jspecify.annotations.Nullable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * Партия в консоли. Игрок вводит направления, программа показывает поле после каждого хода. С --replay ходы берутся из
 * файла, а не с клавиатуры. Повтор идёт через тот же цикл, поэтому поля и итог совпадают с исходной партией.
 */
@Command(name = "play", description = "Сыграть партию в консоли.")
public final class PlayCommand implements Callable<Integer> {

    @Option(names = "--config", description = "Путь к конфигу кампании. По умолчанию ${DEFAULT-VALUE}.")
    private Path configPath = Main.DEFAULT_CONFIG;

    @Option(names = "--seed", description = "Seed. Если не указан, выбирается случайно и печатается.")
    private long seed = new Random().nextInt(Integer.MAX_VALUE);

    @Option(names = "--save", description = "Сохранить seed и ходы партии в файл.")
    private @Nullable Path save;

    @Option(names = "--replay", description = "Повторить партию из файла, который записал --save.")
    private @Nullable Path replay;

    @Override
    public Integer call() {
        Scanner input = replay == null ? new Scanner(System.in, StandardCharsets.UTF_8) : loadReplay(replay);
        CampaignConfig config = ConfigLoader.load(configPath);
        TileSpawner spawner = new TileSpawner(config.spawnRules(), new Random(seed));
        MoveEngine engine = new MoveEngine(spawner);
        GameState state = GameState.start(config.movesLimit(), spawner);
        StringBuilder moves = new StringBuilder();

        System.out.println("Seed: " + seed);
        show(state, config);
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
            if (replay != null) {
                System.out.println(text);
            }
            moves.append(text);
            MoveResult result = engine.applyMove(state, direction.get());
            if (!result.boardChanged()) {
                System.out.println("Плитки не сдвинулись. Ход не потрачен.");
            }
            state = result.state();
            show(state, config);
        }
        System.out.println(state.isOver() ? "Партия окончена." : "Партия прервана.");
        System.out.println("Итоговый счёт: " + state.score());
        System.out.println("Итоговая награда: " + reward(state, config));
        if (save != null) {
            saveGame(save, moves.toString());
        }
        return 0;
    }

    /** Файл партии. Первая строка seed, вторая строка ходы подряд, например wasd. */
    private void saveGame(Path file, String moves) {
        try {
            Files.writeString(file, seed + "\n" + moves + "\n", StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalArgumentException("Не удалось сохранить партию в " + file, e);
        }
        System.out.println("Партия сохранена в " + file);
    }

    /** Берёт seed из файла партии. Ходы отдаёт по одному в строке, как будто их вводят с клавиатуры. */
    private Scanner loadReplay(Path file) {
        List<String> lines;
        try {
            lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalArgumentException("Не удалось прочитать партию " + file, e);
        }
        if (lines.isEmpty()) {
            throw new IllegalArgumentException("Файл партии " + file + " пуст");
        }
        seed = Long.parseLong(lines.get(0).replace("\uFEFF", "").trim());
        String moves = lines.size() > 1 ? lines.get(1).trim() : "";
        return new Scanner(String.join("\n", moves.split("")));
    }

    private static void show(GameState state, CampaignConfig config) {
        System.out.print(BoardRenderer.render(state));
        System.out.println("Награда сейчас: " + reward(state, config));
    }

    private static String reward(GameState state, CampaignConfig config) {
        return config.rewardFor(state.board().maxTile()).toPlainString();
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
