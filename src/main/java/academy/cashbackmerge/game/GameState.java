package academy.cashbackmerge.game;

/** Состояние партии: поле, набранный score и сколько ходов осталось. */
public record GameState(Board board, int score, int movesLeft) {

    /** Начало партии: на пустое поле ставятся первые плитки. */
    public static GameState start(int movesLimit, TileSpawner spawner) {
        Board board = Board.empty();
        for (int i = 0; i < GameRules.INITIAL_TILES; i++) {
            board = spawner.spawn(board);
        }
        return new GameState(board, 0, movesLimit);
    }

    /** Партия окончена, когда ходы закончились или ни один ход не меняет поле. */
    public boolean isOver() {
        return movesLeft == 0 || !canMove();
    }

    private boolean canMove() {
        for (Direction direction : Direction.values()) {
            if (!Slider.slide(board, direction).board().equals(board)) {
                return true;
            }
        }
        return false;
    }
}
