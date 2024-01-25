package dev.paulosouza.bingo.dto.bingo.response;

import dev.paulosouza.bingo.dto.bingo.request.BingoMode;
import dev.paulosouza.bingo.game.bingo.BingoCard;
import lombok.Data;

import java.util.List;

@Data
public class BingoGameResponse {

    private BingoCard card;

    private List<Integer> drawnNumbers;

    private int number;

    private boolean isGameRunning;

    private BingoMode mode;

    private int playersCount;

}
