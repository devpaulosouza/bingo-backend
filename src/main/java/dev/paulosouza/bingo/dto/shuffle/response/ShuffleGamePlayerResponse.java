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
public class ShuffleGamePlayerResponse {

    private String[] words;

    private String[] shuffledWords;

    private List<ShufflePlayer> winners;

    private boolean isGameRunning;

    private boolean isWinner;

    private boolean focused;

}
