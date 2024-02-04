package dev.paulosouza.bingo.dto.shuffle.response;

import dev.paulosouza.bingo.game.shuffle.ShufflePlayer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShuffleGameResponse {

    private List<ShufflePlayer> players;

    private List<ShufflePlayer> winners;

    private String[] shuffledWords;

    private String[] words;

    private boolean isGameRunning;

    private int playersCount;

}
