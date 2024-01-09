package dev.paulosouza.bingo.dto.stop.sse;

import dev.paulosouza.bingo.dto.bingo.response.sse.SseEventResponse;
import dev.paulosouza.bingo.dto.bingo.response.sse.SseEventType;

public class StopCanStopMessage extends SseEventResponse {

    public StopCanStopMessage() {
        super(SseEventType.CAN_STOP);
    }

}
