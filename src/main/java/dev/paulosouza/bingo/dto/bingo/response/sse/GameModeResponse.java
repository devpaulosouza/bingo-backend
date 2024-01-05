package dev.paulosouza.bingo.dto.bingo.response.sse;

import dev.paulosouza.bingo.dto.bingo.request.BingoMode;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class GameModeResponse extends SseEventResponse {

    private BingoMode mode;

    public GameModeResponse(BingoMode mode) {
        super(SseEventType.GAME_MODE);
        this.mode = mode;
    }

}
