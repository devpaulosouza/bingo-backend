package dev.paulosouza.bingo.utils;

import java.util.List;

public class GameUtils {

    private GameUtils() {

    }

    @SuppressWarnings("java:S3776")
    public static boolean checkWinner(boolean[][] markedNumbers, int[][] numbers, List<Integer> drawnNumbers) {
        for (int i = 0; i < markedNumbers.length; i++) {
            boolean wonByRow = true;
            boolean wonByCol = true;
            boolean wonByMainDiagonal = true;
            boolean wonBySecondaryDiagonal = true;

            for (int j = 0; j < markedNumbers[0].length; j++) {
                if (i == 2 && j == 2) {
                    continue;
                }
                wonByRow &= markedNumbers[i][j] && drawnNumbers.contains(numbers[i][j]);
                wonByCol &= markedNumbers[j][i] && drawnNumbers.contains(numbers[j][i]);

                if (i == j) {
                    wonByMainDiagonal &= markedNumbers[i][j] && drawnNumbers.contains(numbers[i][j]);
                }

                if (i + j == 4) {
                    wonBySecondaryDiagonal &= markedNumbers[i][j] && drawnNumbers.contains(numbers[i][j]);
                }

                if (
                        (i == numbers.length - 1 || j == numbers.length - 1)
                        && (wonByRow || wonByCol)
                ) {
                    return true;
                }

                if (
                        (i == numbers.length - 1 && j == numbers.length - 1)
                        && (wonByMainDiagonal || wonBySecondaryDiagonal)
                ) {
                    return true;
                }
            }
        }

        return false;
    }

}
