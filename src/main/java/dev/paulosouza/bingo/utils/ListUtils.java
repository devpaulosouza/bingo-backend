package dev.paulosouza.bingo.utils;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ListUtils {

    private static final Random random = new Random();

    private ListUtils() {

    }

    @SuppressWarnings("java:S6204")
    public static List<Integer> buildList(int startInclusive, int endInclusive) {
        return IntStream.rangeClosed(startInclusive, endInclusive)
                .boxed()
                .collect(Collectors.toList());
    }

    public static int chooseNumber(List<Integer> possibleNumbers) {
        return possibleNumbers.remove(ListUtils.random.nextInt(possibleNumbers.size()));
    }

    public static String chooseWord(List<String> possibleNumbers) {
        return possibleNumbers.remove(ListUtils.random.nextInt(possibleNumbers.size()));
    }

}
