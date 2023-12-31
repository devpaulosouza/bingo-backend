package dev.paulosouza.bingo.dto.response.sse;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class DrawnNumberResponse extends SseEventResponse {

    private final int number;

    private final List<Integer> drawnNumbers;

    public DrawnNumberResponse(int number, List<Integer> drawnNumbers) {
        super(SseEventType.DRAWN_NUMBER);
        this.number = number;
        this.drawnNumbers = drawnNumbers;
    }

}
