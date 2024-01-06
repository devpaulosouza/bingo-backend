package dev.paulosouza.bingo.dto.stop.sse.response;

import dev.paulosouza.bingo.dto.bingo.response.sse.SseEventResponse;
import dev.paulosouza.bingo.dto.bingo.response.sse.SseEventType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class StopStoppedMessage extends SseEventResponse {

    private String playerName;

    private boolean stopped = true;

    public StopStoppedMessage(String playerName) {
        super(SseEventType.STOP_STOPPED);
        this.playerName = playerName;
    }

}
