package dev.paulosouza.bingo.dto.bingo.response.sse;

public class KickResponse extends SseEventResponse {

    public KickResponse() {
        super(SseEventType.KICK);
    }

}
