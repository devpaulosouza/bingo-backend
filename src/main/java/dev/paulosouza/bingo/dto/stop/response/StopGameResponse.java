package dev.paulosouza.bingo.dto.stop.response;

import dev.paulosouza.bingo.game.Player;
import dev.paulosouza.bingo.game.stop.StopGame;
import lombok.Data;

import java.util.List;

@Data
public class StopGameResponse {

    private List<StopGame> games;

    private List<Player> winners;

    private List<String> drawnWords;

    private Character letter;

    private boolean showingResults;

    private int validateWordCount;

}
