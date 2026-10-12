package academy.cashbackmerge.game;

/** Клетка поля. Строки нумеруются сверху вниз, столбцы слева направо, начиная с нуля. */
public record Position(int row, int column) {

    public Position {
        if (!isInside(row) || !isInside(column)) {
            throw new IllegalArgumentException("Клетка вне поля: строка " + row + ", столбец " + column);
        }
    }

    private static boolean isInside(int index) {
        return index >= 0 && index < GameRules.BOARD_SIZE;
    }
}
