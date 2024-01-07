package dev.paulosouza.bingo.dto.stop.sse.response;

import dev.paulosouza.bingo.dto.bingo.response.sse.SseEventResponse;
import dev.paulosouza.bingo.dto.bingo.response.sse.SseEventType;

public class StopRestartMessage extends SseEventResponse {

    public StopRestartMessage() {
        super(SseEventType.STOP_RESTART);
    }

}
