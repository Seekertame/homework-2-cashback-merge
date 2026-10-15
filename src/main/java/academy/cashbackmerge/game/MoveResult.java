package academy.cashbackmerge.game;

/** Итог хода. Если поле не изменилось, состояние прежнее. Ход не потрачен, новой плитки нет. */
public record MoveResult(GameState state, boolean boardChanged) {}
