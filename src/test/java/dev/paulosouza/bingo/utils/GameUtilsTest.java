package dev.paulosouza.bingo.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class GameUtilsTest {

    private static final int[][] VALID_NUMBERS = {
            {1, 16, 31, 46, 61},
            {2, 17, 32, 47, 62},
            {3, 18, -1, 48, 63},
            {4, 19, 34, 49, 64},
            {5, 20, 35, 50, 65}
    };

    @Test
    void wonBlackout() {
        // given

        // when
        boolean won = GameUtils.checkBlackoutWinner(
                this.buildBlackout(),
                VALID_NUMBERS,
                List.of(
                        1, 16, 31, 46, 61,
                        2, 17, 32, 47, 62,
                        3, 18, 0, 48, 63,
                        4, 19, 34, 49, 64,
                        5, 20, 35, 50, 65
                )
        );

        // then
        Assertions.assertTrue(won);
    }

    @Test
    void looseBlackout() {
        // given

        // when
        boolean won = GameUtils.checkBlackoutWinner(
                this.buildBlackout(),
                VALID_NUMBERS,
                List.of(
                        1, 16, 31, 46, 61,
                        2, 17, 32, 47, 62,
                        3, 18, 0, 48, 63,
                        4, 19, 34, 49, 64,
                        5, 20, 35, 50, 66
                )
        );

        // then
        Assertions.assertFalse(won);
    }

    @Test
    void wonByColumn() {
        // given

        // when
        boolean col0 = GameUtils.checkStandardWinner(this.buildMarkedCol(0), VALID_NUMBERS, List.of(3, 2, 1, 5, 4));
        boolean col1 = GameUtils.checkStandardWinner(this.buildMarkedCol(1), VALID_NUMBERS, List.of(18, 16, 17, 19, 20));
        boolean col2 = GameUtils.checkStandardWinner(this.buildMarkedCol(2), VALID_NUMBERS, List.of(35, 31, 34, 32));
        boolean col3 = GameUtils.checkStandardWinner(this.buildMarkedCol(3), VALID_NUMBERS, List.of(46, 47, 50, 48, 49));
        boolean col4 = GameUtils.checkStandardWinner(this.buildMarkedCol(4), VALID_NUMBERS, List.of(61, 62, 63, 64, 65));

        // then
        Assertions.assertTrue(col0);
        Assertions.assertTrue(col1);
        Assertions.assertTrue(col2);
        Assertions.assertTrue(col3);
        Assertions.assertTrue(col4);
    }

    @Test
    void wonByRow() {
        // given

        // when
        boolean col0 = GameUtils.checkStandardWinner(this.buildMarkedRow(0), VALID_NUMBERS, List.of(1, 31, 16, 46, 61));
        boolean col1 = GameUtils.checkStandardWinner(this.buildMarkedRow(1), VALID_NUMBERS, List.of(17, 2, 62, 32, 47));
        boolean col2 = GameUtils.checkStandardWinner(this.buildMarkedRow(2), VALID_NUMBERS, List.of(63, 48, 18, 3));
        boolean col3 = GameUtils.checkStandardWinner(this.buildMarkedRow(3), VALID_NUMBERS, List.of(19, 34, 49, 64, 4));
        boolean col4 = GameUtils.checkStandardWinner(this.buildMarkedRow(4), VALID_NUMBERS, List.of(5, 20, 35, 50, 65));

        // then
        Assertions.assertTrue(col0);
        Assertions.assertTrue(col1);
        Assertions.assertTrue(col2);
        Assertions.assertTrue(col3);
        Assertions.assertTrue(col4);
    }

    @Test
    void wonByMainDiagonal() {
        // given

        // when
        boolean col0 = GameUtils.checkStandardWinner(this.buildMarkedMainDiagonal(), VALID_NUMBERS, List.of(49, 65, 1, 17));

        // then
        Assertions.assertTrue(col0);
    }

    @Test
    void wonBySecondaryDiagonal() {
        // given

        // when
        boolean col0 = GameUtils.checkStandardWinner(this.buildMarkedSecondaryDiagonal(), VALID_NUMBERS, List.of(61, 47, 5, 19));

        // then
        Assertions.assertTrue(col0);
    }

    @Test
    void wrongMarkCol() {
        // given

        // when
        boolean col0 = GameUtils.checkStandardWinner(this.buildMarkedCol(2), VALID_NUMBERS, List.of(3, 2, 1, 5, 4));
        boolean col1 = GameUtils.checkStandardWinner(this.buildMarkedCol(3), VALID_NUMBERS, List.of(18, 16, 17, 19, 20));
        boolean col2 = GameUtils.checkStandardWinner(this.buildMarkedCol(4), VALID_NUMBERS, List.of(35, 31, 34, 32));
        boolean col3 = GameUtils.checkStandardWinner(this.buildMarkedCol(1), VALID_NUMBERS, List.of(46, 47, 50, 48, 49));
        boolean col4 = GameUtils.checkStandardWinner(this.buildMarkedCol(0), VALID_NUMBERS, List.of(61, 62, 63, 64, 65));

        // then
        Assertions.assertFalse(col0);
        Assertions.assertFalse(col1);
        Assertions.assertFalse(col2);
        Assertions.assertFalse(col3);
        Assertions.assertFalse(col4);
    }

    @Test
    void wrongMarkRow() {
        // given

        // when
        boolean col0 = GameUtils.checkStandardWinner(this.buildMarkedRow(4), VALID_NUMBERS, List.of(1, 31, 16, 46, 61));
        boolean col1 = GameUtils.checkStandardWinner(this.buildMarkedRow(3), VALID_NUMBERS, List.of(17, 2, 62, 32, 47));
        boolean col2 = GameUtils.checkStandardWinner(this.buildMarkedRow(1), VALID_NUMBERS, List.of(63, 48, 18, 3));
        boolean col3 = GameUtils.checkStandardWinner(this.buildMarkedRow(2), VALID_NUMBERS, List.of(19, 34, 49, 64, 4));
        boolean col4 = GameUtils.checkStandardWinner(this.buildMarkedRow(0), VALID_NUMBERS, List.of(5, 20, 35, 50, 65));

        // then
        Assertions.assertFalse(col0);
        Assertions.assertFalse(col1);
        Assertions.assertFalse(col2);
        Assertions.assertFalse(col3);
        Assertions.assertFalse(col4);
    }

    @Test
    void wrongMarkMainDiagonal() {
        // given

        // when
        boolean col0 = GameUtils.checkStandardWinner(this.buildMarkedSecondaryDiagonal(), VALID_NUMBERS, List.of(49, 65, 1, 17));

        // then
        Assertions.assertFalse(col0);
    }

    @Test
    void wrongMarkSecondaryDiagonal() {
        // given

        // when
        boolean col0 = GameUtils.checkStandardWinner(this.buildMarkedMainDiagonal(), VALID_NUMBERS, List.of(61, 47, 5, 19));

        // then
        Assertions.assertFalse(col0);
    }

    @Test
    void noneMatchCol() {
        // given

        // when
        boolean col0 = GameUtils.checkStandardWinner(this.buildMarkedCol(0), VALID_NUMBERS, List.of(3, 2, 1, 6, 4));

        // then
        Assertions.assertFalse(col0);
    }

    @Test
    void noneMatchRow() {
        // given

        // when
        boolean col0 = GameUtils.checkStandardWinner(this.buildMarkedRow(2), VALID_NUMBERS, List.of(1, 31, 16, 46, 62));

        // then
        Assertions.assertFalse(col0);
    }

    @Test
    void drawCardNumbers() {
        // given

        // when
        int[][] cardNumbers = GameUtils.drawCardNumbers();

        // then
        for (int i = 0; i < cardNumbers.length; i++) {
            for (int j = 0; j < cardNumbers[i].length; j++) {
                if (i == 2 && j == 2) {
                    continue;
                }
                Assertions.assertTrue(cardNumbers[j][i] > (15 * i));
                Assertions.assertTrue(cardNumbers[j][i] <= (15 * i ) + 15);
            }
        }
    }

    private boolean[][] buildMarkedCol(int col) {
        boolean[][] markedNumbers = new boolean[5][5];

        for (int i = 0; i < markedNumbers.length; i++) {
            markedNumbers[i][col] = true;
        }

        return markedNumbers;
    }

    private boolean[][] buildBlackout() {
        boolean[][] markedNumbers = new boolean[5][5];

        for (int i = 0; i < markedNumbers.length; i++) {
            for (int j = 0; j < markedNumbers.length; j++) {
                markedNumbers[i][j] = true;
            }
        }

        return markedNumbers;
    }

    private boolean[][] buildMarkedRow(int row) {
        boolean[][] markedNumbers = new boolean[5][5];

        for (int j = 0; j < markedNumbers.length; j++) {
            markedNumbers[row][j] = true;
        }

        return markedNumbers;
    }

    private boolean[][] buildMarkedMainDiagonal() {
        boolean[][] markedNumbers = new boolean[5][5];

        for (int i = 0; i < markedNumbers.length; i++) {
            markedNumbers[i][i] = true;
        }

        return markedNumbers;
    }

    private boolean[][] buildMarkedSecondaryDiagonal() {
        boolean[][] markedNumbers = new boolean[5][5];

        for (int i = 0; i < markedNumbers.length; i++) {
            markedNumbers[i][markedNumbers.length - 1 - i] = true;
        }

        return markedNumbers;
    }

}