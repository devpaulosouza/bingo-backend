package dev.paulosouza.bingo.dto.stop.sse.response;

import dev.paulosouza.bingo.dto.bingo.response.sse.SseEventResponse;
import dev.paulosouza.bingo.dto.bingo.response.sse.SseEventType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class StopValidateWordMessage extends SseEventResponse {

    private int count;

    public StopValidateWordMessage(int count) {
        super(SseEventType.STOP_VALIDATE_WORD);
        this.count = count;
    }

}
