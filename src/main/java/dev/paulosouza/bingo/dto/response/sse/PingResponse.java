package dev.paulosouza.bingo.dto.response.sse;

public class PingResponse extends SseEventResponse {

    public PingResponse() {
        super(SseEventType.PING);
    }

}
