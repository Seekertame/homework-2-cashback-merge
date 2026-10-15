package academy.cashbackmerge.mandatory.mr2;

import static org.assertj.core.api.Assertions.assertThat;

import academy.cashbackmerge.game.Board;
import academy.cashbackmerge.game.Cashback;
import academy.cashbackmerge.game.Direction;
import academy.cashbackmerge.game.Position;
import academy.cashbackmerge.game.Slider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/** Обязательные тесты: правила слияния плиток. */
@DisplayName("MR2. Слияние плиток")
class MergeRulesTest {

    @ParameterizedTest
    @CsvSource({
        "'1,1,.,.', '2,.,.,.'",
        "'1,1,1,.', '2,1,.,.'",
        "'1,1,1,1', '2,2,.,.'",
        "'2,2,4,4', '4,8,.,.'",
    })
    @DisplayName("Сдвиг влево: {0} -> {1}")
    void rowShiftsLeftAndMerges(String before, String expected) {
        assertThat(slideRow(before, Direction.LEFT)).isEqualTo(expected);
    }

    @Test
    @DisplayName("Одна плитка не сливается дважды за ход: [1,1,2,.] -> [2,2,.,.]")
    void tileDoesNotMergeTwiceInOneMove() {
        assertThat(slideRow("1,1,2,.", Direction.LEFT)).isEqualTo("2,2,.,.");
    }

    @Test
    @DisplayName("Слияние работает во всех четырёх направлениях")
    void mergeWorksInAllDirections() {
        Board horizontal = new Board(Map.of(new Position(1, 1), Cashback.ONE, new Position(1, 2), Cashback.ONE));
        Board vertical = new Board(Map.of(new Position(1, 1), Cashback.ONE, new Position(2, 1), Cashback.ONE));

        assertThat(Slider.slide(horizontal, Direction.LEFT).board()).isEqualTo(twoAt(1, 0));
        assertThat(Slider.slide(horizontal, Direction.RIGHT).board()).isEqualTo(twoAt(1, 3));
        assertThat(Slider.slide(vertical, Direction.UP).board()).isEqualTo(twoAt(0, 1));
        assertThat(Slider.slide(vertical, Direction.DOWN).board()).isEqualTo(twoAt(3, 1));
    }

    @ParameterizedTest
    @CsvSource({
        "'1,.,1,.', '2,.,.,.'",
        "'.,.,.,1', '1,.,.,.'",
        "'1,2,1,2', '1,2,1,2'",
        "'32,32,.,.', '32,32,.,.'",
    })
    @DisplayName("Сдвиг влево, другие случаи: {0} -> {1}")
    void otherLeftShifts(String before, String expected) {
        assertThat(slideRow(before, Direction.LEFT)).isEqualTo(expected);
    }

    @Test
    @DisplayName("Сдвиг вправо сливает пару у правого края: [1,1,1,.] -> [.,.,1,2]")
    void rightShiftMergesFromRightEdge() {
        assertThat(slideRow("1,1,1,.", Direction.RIGHT)).isEqualTo(".,.,1,2");
    }

    @Test
    @DisplayName("Очки за сдвиг равны сумме новых плиток")
    void slideScoreIsSumOfMergedTiles() {
        assertThat(Slider.slide(row("2,2,4,4"), Direction.LEFT).score()).isEqualTo(12);
    }

    /** Ставит строку вида "1,1,.,." в верхний ряд поля, сдвигает и возвращает верхний ряд тем же текстом. */
    private static String slideRow(String cells, Direction direction) {
        Board after = Slider.slide(row(cells), direction).board();
        List<String> result = new ArrayList<>();
        for (int column = 0; column < 4; column++) {
            result.add(after.tileAt(new Position(0, column))
                    .map(level -> String.valueOf(level.percent()))
                    .orElse("."));
        }
        return String.join(",", result);
    }

    private static Board row(String cells) {
        String[] values = cells.split(",");
        Map<Position, Cashback> tiles = new HashMap<>();
        for (int column = 0; column < values.length; column++) {
            if (!".".equals(values[column])) {
                tiles.put(new Position(0, column), Cashback.fromPercent(values[column]));
            }
        }
        return new Board(tiles);
    }

    private static Board twoAt(int row, int column) {
        return new Board(Map.of(new Position(row, column), Cashback.TWO));
    }
}
