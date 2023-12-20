package dev.paulosouza.bingo.game;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Card {

    private UUID id;

    private int[][] numbers;

    private final boolean[][] markedNumbers = new boolean[5][5];

    private List<Integer> numbersList;

    private Player player;

    public void setNumbers(int[][] numbers) {
        this.numbers = numbers;
        this.numbersList = Arrays.stream(numbers)
                .flatMapToInt(Arrays::stream)
                .boxed()
                .toList();
    }

}
