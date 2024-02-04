package dev.paulosouza.bingo.game.shuffle;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShufflePlayer {

    private UUID id;

    private String username;

    private String name;

    private String[] words;

    private boolean focused;

    private SseEmitter emitter;

}
