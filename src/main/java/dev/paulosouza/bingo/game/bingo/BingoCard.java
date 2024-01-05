package dev.paulosouza.bingo.game.bingo;

import dev.paulosouza.bingo.game.Player;
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
public class BingoCard {

    private UUID id;

    private int[][] numbers;

    private boolean[][] markedNumbers = new boolean[5][5];

    private List<Integer> numbersList;

    private Player player;

    public void setNumbers(int[][] numbers) {
        this.numbers = numbers;
        this.numbersList = Arrays.stream(numbers)
                .flatMapToInt(Arrays::stream)
                .boxed()
                .toList();
        this.markedNumbers = new boolean[5][5];
    }

}
