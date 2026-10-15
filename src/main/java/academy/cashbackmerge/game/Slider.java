package academy.cashbackmerge.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Сдвиг и слияние плиток. Здесь нет случайности, новые плитки не появляются. */
public final class Slider {

    private Slider() {}

    public static SlideResult slide(Board board, Direction direction) {
        Map<Position, Cashback> tiles = new HashMap<>();
        int score = 0;
        for (int line = 0; line < GameRules.BOARD_SIZE; line++) {
            MergedLine merged = mergeLine(tilesInLine(board, direction, line));
            for (int step = 0; step < merged.tiles().size(); step++) {
                tiles.put(cell(direction, line, step), merged.tiles().get(step));
            }
            score += merged.score();
        }
        return new SlideResult(new Board(tiles), score);
    }

    /** Плитки линии по порядку от края, к которому идёт сдвиг. Пустые клетки пропускаются. */
    private static List<Cashback> tilesInLine(Board board, Direction direction, int line) {
        List<Cashback> tiles = new ArrayList<>();
        for (int step = 0; step < GameRules.BOARD_SIZE; step++) {
            board.tileAt(cell(direction, line, step)).ifPresent(tile -> tiles.add(tile));
        }
        return tiles;
    }

    /**
     * Сливает соседние одинаковые плитки. Сравниваются только исходные плитки, а не результат слияния. Поэтому плитка
     * сливается не больше одного раза за ход: [1, 1, 2] даёт [2, 2], а не [4].
     */
    private static MergedLine mergeLine(List<Cashback> tiles) {
        List<Cashback> result = new ArrayList<>();
        int score = 0;
        int i = 0;
        while (i < tiles.size()) {
            Cashback current = tiles.get(i);
            boolean pair = i + 1 < tiles.size() && tiles.get(i + 1) == current && !current.isMax();
            if (pair) {
                Cashback merged = current.next();
                result.add(merged);
                score += merged.percent();
                i += 2;
            } else {
                result.add(current);
                i++;
            }
        }
        return new MergedLine(result, score);
    }

    /** Клетка линии. line это номер строки или столбца. step это шаг от края, к которому идёт сдвиг. */
    private static Position cell(Direction direction, int line, int step) {
        int last = GameRules.BOARD_SIZE - 1;
        return switch (direction) {
            case LEFT -> new Position(line, step);
            case RIGHT -> new Position(line, last - step);
            case UP -> new Position(step, line);
            case DOWN -> new Position(last - step, line);
        };
    }

    private record MergedLine(List<Cashback> tiles, int score) {}
}
