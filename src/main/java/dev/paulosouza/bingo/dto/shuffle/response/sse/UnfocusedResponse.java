package dev.paulosouza.bingo.dto.shuffle.response.sse;

import dev.paulosouza.bingo.dto.bingo.response.sse.SseEventResponse;
import dev.paulosouza.bingo.dto.bingo.response.sse.SseEventType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public class UnfocusedResponse extends SseEventResponse {

    private final UUID playerId;

    public UnfocusedResponse(UUID playerId) {
        super(SseEventType.UNFOCUSED);
        this.playerId = playerId;
    }

}
