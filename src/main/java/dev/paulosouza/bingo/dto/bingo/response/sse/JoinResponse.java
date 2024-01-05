package dev.paulosouza.bingo.dto.bingo.response.sse;

import dev.paulosouza.bingo.game.bingo.BingoCard;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class JoinResponse extends SseEventResponse {

    private BingoCard card;

    public JoinResponse(BingoCard card) {
        super(SseEventType.JOIN);
        this.card = card;
    }

}
