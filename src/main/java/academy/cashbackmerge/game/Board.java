package academy.cashbackmerge.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Поле игры. Пустой клетки нет в tiles. Поле не меняется, каждое изменение создаёт новое поле. */
public record Board(Map<Position, Cashback> tiles) {

    public Board {
        tiles = Map.copyOf(tiles);
    }

    public static Board empty() {
        return new Board(Map.of());
    }

    public Optional<Cashback> tileAt(Position position) {
        return Optional.ofNullable(tiles.get(position));
    }

    /**
     * Пустые клетки по строкам сверху вниз, в строке слева направо. Порядок всегда один и тот же, поэтому выбор клетки
     * по seed воспроизводится.
     */
    public List<Position> emptyPositions() {
        List<Position> result = new ArrayList<>();
        for (int row = 0; row < GameRules.BOARD_SIZE; row++) {
            for (int column = 0; column < GameRules.BOARD_SIZE; column++) {
                Position position = new Position(row, column);
                if (!tiles.containsKey(position)) {
                    result.add(position);
                }
            }
        }
        return result;
    }

    /** Самая большая плитка на поле. По ней начисляется награда. */
    public Cashback maxTile() {
        if (tiles.isEmpty()) {
            throw new IllegalStateException("На поле нет плиток");
        }
        Cashback max = Cashback.ONE;
        for (Cashback tile : tiles.values()) {
            if (tile.percent() > max.percent()) {
                max = tile;
            }
        }
        return max;
    }

    /** Новое поле, на котором в пустую клетку поставлена плитка. */
    public Board withTile(Position position, Cashback cashback) {
        if (tiles.containsKey(position)) {
            throw new IllegalArgumentException("Клетка уже занята: " + position);
        }
        Map<Position, Cashback> newTiles = new HashMap<>(tiles);
        newTiles.put(position, cashback);
        return new Board(newTiles);
    }
}
