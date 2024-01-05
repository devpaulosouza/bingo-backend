package dev.paulosouza.bingo.dto.bingo.response.sse;

public class PingResponse extends SseEventResponse {

    public PingResponse() {
        super(SseEventType.PING);
    }

}
