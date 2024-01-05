package dev.paulosouza.bingo.dto.bingo.response;

import dev.paulosouza.bingo.dto.bingo.request.BingoMode;
import dev.paulosouza.bingo.game.bingo.Card;
import dev.paulosouza.bingo.game.bingo.Player;
import lombok.Data;

import java.util.List;

@Data
public class AdminGameResponse {

    private List<Card> cards;

    private List<Integer> drawnNumbers;

    private List<Player> winners;

    private int number;

    private boolean isGameRunning;

    private BingoMode mode;

}
