package dev.paulosouza.bingo.dto.response;

import dev.paulosouza.bingo.dto.request.GameMode;
import dev.paulosouza.bingo.game.Card;
import lombok.Data;

import java.util.List;

@Data
public class GameResponse {

    private Card card;

    private List<Integer> drawnNumbers;

    private int number;

    private boolean isGameRunning;

    private GameMode mode;

}
