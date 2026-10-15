package academy.cashbackmerge.game;

/** Ход целиком. Сдвиг со слиянием, счёт, лимит ходов и новая плитка. */
public final class MoveEngine {

    private final TileSpawner spawner;

    public MoveEngine(TileSpawner spawner) {
        this.spawner = spawner;
    }

    public MoveResult applyMove(GameState state, Direction direction) {
        if (state.movesLeft() == 0) {
            throw new IllegalStateException("Ходы закончились");
        }
        SlideResult slide = Slider.slide(state.board(), direction);
        if (slide.board().equals(state.board())) {
            return new MoveResult(state, false);
        }
        Board board = spawner.spawn(slide.board());
        GameState next = new GameState(board, state.score() + slide.score(), state.movesLeft() - 1);
        return new MoveResult(next, true);
    }
}
