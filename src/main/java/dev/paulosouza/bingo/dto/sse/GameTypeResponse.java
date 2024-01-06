package dev.paulosouza.bingo.dto.sse;

import dev.paulosouza.bingo.dto.bingo.response.sse.SseEventResponse;
import dev.paulosouza.bingo.dto.bingo.response.sse.SseEventType;
import dev.paulosouza.bingo.dto.request.GameType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class GameTypeResponse extends SseEventResponse {

    private final GameType gameType;

    public GameTypeResponse(GameType type) {
        super(SseEventType.GAME_TYPE);
        this.gameType = type;
    }

}
