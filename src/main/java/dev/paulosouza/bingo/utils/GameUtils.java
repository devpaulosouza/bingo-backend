package dev.paulosouza.bingo.utils;

import dev.paulosouza.bingo.exception.UnprocessableEntityException;

import java.util.List;

public class GameUtils {

    private GameUtils() {

    }

    @SuppressWarnings("java:S3776")
    public static boolean checkWinner(boolean[][] markedNumbers, int[][] numbers, List<Integer> drawnNumbers) {
        boolean wonByMainDiagonal = true;
        boolean wonBySecondaryDiagonal = true;

        for (int i = 0; i < markedNumbers.length; i++) {
            boolean wonByRow = true;
            boolean wonByCol = true;

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

                if (j == numbers.length - 1 && wonByCol) {
                    return true;
                }

                if (j == numbers.length - 1 && wonByRow) {
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

    public static int[][] drawCardNumbers() {
        int[][] cardNumbers = new int[5][5];

        List<Integer> possibleB = ListUtils.buildList(1, 15);
        List<Integer> possibleI = ListUtils.buildList(16, 30);
        List<Integer> possibleN = ListUtils.buildList(31, 45);
        List<Integer> possibleG = ListUtils.buildList(46, 60);
        List<Integer> possibleO = ListUtils.buildList(61, 75);

        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                switch (j) {
                    case 0 -> cardNumbers[i][j] = ListUtils.chooseNumber(possibleB);
                    case 1 -> cardNumbers[i][j] = ListUtils.chooseNumber(possibleI);
                    case 2 -> {
                        if (i == 2) {
                            cardNumbers[i][j] = -1;
                        } else {
                            cardNumbers[i][j] = ListUtils.chooseNumber(possibleN);
                        }
                    }
                    case 3 -> cardNumbers[i][j] = ListUtils.chooseNumber(possibleG);
                    case 4 -> cardNumbers[i][j] = ListUtils.chooseNumber(possibleO);
                    default -> throw new UnprocessableEntityException("Unexpected range");
                }
            }
        }

        return cardNumbers;
    }

}
