package dev.paulosouza.bingo.dto.bingo.response.sse;

import dev.paulosouza.bingo.game.bingo.BingoCard;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class JoinResponse extends SseEventResponse {

    private BingoCard card;

    private int playersCount;

    public JoinResponse(BingoCard card, int playersCount) {
        super(SseEventType.JOIN);
        this.card = card;
        this.playersCount = playersCount;
    }

}
