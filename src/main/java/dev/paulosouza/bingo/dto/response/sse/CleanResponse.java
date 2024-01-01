package dev.paulosouza.bingo.dto.response.sse;

public class CleanResponse extends SseEventResponse {

    public CleanResponse() {
        super(SseEventType.CLEAN);
    }

}
