package dev.paulosouza.bingo.dto.response.sse;

import dev.paulosouza.bingo.dto.request.GameMode;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class GameModeResponse extends SseEventResponse {

    private GameMode mode;

    public GameModeResponse(GameMode mode) {
        super(SseEventType.GAME_MODE);
        this.mode = mode;
    }

}
