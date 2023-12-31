package dev.paulosouza.bingo.dto.response.sse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SseEventResponse {

    private SseEventType type;

}
