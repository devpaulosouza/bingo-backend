package dev.paulosouza.bingo.dto.response.sse;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class StartedResponse extends SseEventResponse {

    private boolean started;

    public StartedResponse(boolean started) {
        super(SseEventType.START);
        this.started = started;
    }

}
