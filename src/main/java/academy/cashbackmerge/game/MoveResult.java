package academy.cashbackmerge.game;

/** Итог хода. Если поле не изменилось, состояние прежнее: ход не потрачен, новой плитки нет. */
public record MoveResult(GameState state, boolean boardChanged) {}
