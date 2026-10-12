package academy.cashbackmerge.game;

import com.fasterxml.jackson.annotation.JsonCreator;

/** Уровень кэшбэка на плитке. Две плитки одного уровня объединяются в следующий уровень. */
public enum Cashback {
    ONE(1),
    TWO(2),
    FOUR(4),
    EIGHT(8),
    SIXTEEN(16),
    THIRTY_TWO(32);

    private final int percent;

    Cashback(int percent) {
        this.percent = percent;
    }

    public int percent() {
        return percent;
    }

    /** Максимальный уровень дальше не объединяется. */
    public boolean isMax() {
        return this == THIRTY_TWO;
    }

    /** Уровень, который получится после объединения двух плиток этого уровня. */
    public Cashback next() {
        for (Cashback level : values()) {
            if (level.percent == percent * 2) {
                return level;
            }
        }
        throw new IllegalStateException("У максимального уровня нет следующего");
    }

    /** Находит уровень по записи из конфига, например "16". */
    @JsonCreator
    public static Cashback fromPercent(String text) {
        for (Cashback level : values()) {
            if (String.valueOf(level.percent).equals(text)) {
                return level;
            }
        }
        throw new IllegalArgumentException("Неизвестный уровень кэшбэка: " + text + ". Допустимы 1, 2, 4, 8, 16, 32");
    }
}
