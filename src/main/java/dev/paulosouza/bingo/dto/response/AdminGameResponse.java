package dev.paulosouza.bingo.dto.response;

import dev.paulosouza.bingo.game.Card;
import dev.paulosouza.bingo.game.Player;
import lombok.Data;

import java.util.List;

@Data
public class AdminGameResponse {

    private List<Card> cards;

    private List<Integer> drawnNumbers;

    private List<Player> winners;

    private int number;

    private boolean isGameRunning;

}
