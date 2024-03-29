package dev.paulosouza.bingo.dto.bingo.response.sse;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public class WinnerResponse extends SseEventResponse {

    private final UUID playerId;

    private final String playerName;


    public WinnerResponse(UUID playerId, String playerName) {
        super(SseEventType.WINNER);
        this.playerId = playerId;
        this.playerName = playerName;
    }

    public WinnerResponse() {
        super(SseEventType.WINNER);
        this.playerId = null;
        this.playerName = null;
    }

}
