package academy.cashbackmerge.game;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Поле")
class BoardTest {

    @Test
    @DisplayName("Клетку за границей поля создать нельзя")
    void positionOutsideBoardIsRejected() {
        assertThatThrownBy(() -> new Position(4, 0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Position(0, -1)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Пустые клетки идут по строкам сверху вниз и слева направо")
    void emptyPositionsHaveFixedOrder() {
        List<Position> empty = Board.empty().emptyPositions();

        assertThat(empty).hasSize(16);
        assertThat(empty.get(0)).isEqualTo(new Position(0, 0));
        assertThat(empty.get(1)).isEqualTo(new Position(0, 1));
        assertThat(empty.get(15)).isEqualTo(new Position(3, 3));
    }

    @Test
    @DisplayName("Плитка ставится на новое поле, старое не меняется")
    void withTileReturnsNewBoard() {
        Board before = Board.empty();
        Board after = before.withTile(new Position(1, 2), Cashback.TWO);

        assertThat(after.tileAt(new Position(1, 2))).contains(Cashback.TWO);
        assertThat(before.tiles()).isEmpty();
    }

    @Test
    @DisplayName("В занятую клетку плитку поставить нельзя")
    void occupiedCellIsRejected() {
        Board board = Board.empty().withTile(new Position(0, 0), Cashback.ONE);

        assertThatThrownBy(() -> board.withTile(new Position(0, 0), Cashback.TWO))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
