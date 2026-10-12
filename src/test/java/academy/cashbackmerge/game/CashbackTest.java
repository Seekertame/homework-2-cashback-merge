package academy.cashbackmerge.game;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Уровни кэшбэка")
class CashbackTest {

    @Test
    @DisplayName("Следующий уровень вдвое больше")
    void nextLevelIsDouble() {
        assertThat(Cashback.ONE.next()).isEqualTo(Cashback.TWO);
        assertThat(Cashback.SIXTEEN.next()).isEqualTo(Cashback.THIRTY_TWO);
    }

    @Test
    @DisplayName("У 32% нет следующего уровня")
    void maxLevelHasNoNext() {
        assertThat(Cashback.THIRTY_TWO.isMax()).isTrue();
        assertThat(Cashback.SIXTEEN.isMax()).isFalse();
        assertThatThrownBy(Cashback.THIRTY_TWO::next).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Уровень находится по тексту из конфига")
    void levelIsFoundByText() {
        assertThat(Cashback.fromPercent("16")).isEqualTo(Cashback.SIXTEEN);
    }
}
