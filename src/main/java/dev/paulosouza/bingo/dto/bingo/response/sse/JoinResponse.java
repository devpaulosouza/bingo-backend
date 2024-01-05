package dev.paulosouza.bingo.dto.bingo.response.sse;

import dev.paulosouza.bingo.game.bingo.Card;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class JoinResponse extends SseEventResponse {

    private Card card;

    public JoinResponse(Card card) {
        super(SseEventType.JOIN);
        this.card = card;
    }

}
