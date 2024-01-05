package dev.paulosouza.bingo.dto.bingo.response.sse;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public class MarkedResponse extends SseEventResponse {

    private UUID playerId;

    private int i;

    private int j;

    private boolean marked;

    public MarkedResponse(UUID playerId, int i, int j, boolean marked) {
        super(SseEventType.MARK);
        this.playerId = playerId;
        this.i = i;
        this.j = j;
        this.marked = marked;
    }

}
